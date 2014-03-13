package ru.hflabs.rcd.connector.files.dataset.xml;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.springframework.util.Assert;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import ru.hflabs.rcd.connector.files.FilesByExtensionFilter;
import ru.hflabs.rcd.connector.files.FilesComparator;
import ru.hflabs.rcd.connector.files.dataset.FilesProducer;
import ru.hflabs.util.core.collection.ArrayUtil;
import ru.hflabs.util.io.IOUtils;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.*;

/**
 * Класс <class>XmlProducer</class> реализует сервис формирования {@link org.dbunit.dataset.IDataSet таблиц} на основе XML файла
 *
 * @author Nazin Alexander
 */
public class XmlProducer extends FilesProducer {

    public XmlProducer(File targetFile) {
        this(new FilesComparator.ByName(), targetFile);
    }

    public XmlProducer(Comparator<File> fileComparator, File targetFile) {
        super(FilesByExtensionFilter.XML_FILTER, fileComparator, targetFile);
    }

    @Override
    protected void readTable(String tableName, File file) throws DataSetException, IOException {
        InputStream xsdInputStream = getClass().getClassLoader().getResourceAsStream(DictionaryTags.XSD_SCHEMA);
        InputStream xmlInputStream = Channels.newInputStream(new FileInputStream(file).getChannel());
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new StreamSource(xsdInputStream));
                saxParserFactory.setSchema(schema);
                saxParserFactory.setNamespaceAware(true);
            }
            XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
            {
                DictionaryReader dictionaryHandler = new DictionaryReader(tableName, consumer);
                xmlReader.setErrorHandler(dictionaryHandler);
                xmlReader.setContentHandler(dictionaryHandler);
            }
            xmlReader.parse(new InputSource(xmlInputStream));
        } catch (SAXException | ParserConfigurationException ex) {
            throw new IOException(ex);
        } finally {
            IOUtils.closeQuietly(xsdInputStream);
            IOUtils.closeQuietly(xmlInputStream);
        }
    }

    /**
     * Класс <class>DictionaryReader</class> сервис чтения XML документа
     *
     * @author Nazin Alexander
     */
    private static class DictionaryReader extends DefaultHandler implements DictionaryTags {

        /** Текущая строка */
        private String currentFieldName;
        private String currentFieldValue;
        private Map<String, String> currentRecord;

        /** Коллекция колонок */
        private Map<String, Column> columns;
        /** Коллекция уникальных названий колонок в нижнем регистре */
        private final Set<String> columnNames;
        /** Коллекция строк */
        private Collection<Map<String, String>> rows;
        /** Название таблицы */
        private final String tableName;
        /** Сервис управления таблицами */
        private final IDataSetConsumer consumer;

        private DictionaryReader(String tableName, IDataSetConsumer consumer) {
            this.columnNames = new LinkedHashSet<>();
            this.tableName = tableName;
            this.consumer = consumer;
        }

        private static Object[] createRow(Set<String> columns, Map<String, String> record) {
            Collection<Object> result = new ArrayList<>(columns.size());
            for (String columnName : columns) {
                result.add(record.get(columnName));
            }
            return ArrayUtil.toArray(Object.class, result);
        }

        @Override
        public void error(SAXParseException ex) throws SAXException {
            throw ex;
        }

        @Override
        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if (currentFieldName != null) {
                currentFieldValue = new String(ch, start, length);
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case DICTIONARY: {
                    columns = new LinkedHashMap<>();
                    rows = new ArrayList<>();
                    break;
                }
                case META_FIELD: {
                    String columnName = attributes.getValue(NAME);
                    Assert.isTrue(
                            columnNames.add(columnName.toLowerCase()),
                            String.format("Duplicate meta field with name '%s'", columnName)
                    );
                    columns.put(columnName, new Column(columnName, DataType.VARCHAR, Column.NO_NULLS));
                    break;
                }
                case RECORD: {
                    currentRecord = new HashMap<>();
                    break;
                }
                case FIELD: {
                    currentFieldName = attributes.getValue(NAME);
                    Assert.isTrue(
                            columns.containsKey(currentFieldName),
                            String.format("Meta field with name '%s' not defined", currentFieldName)
                    );
                    currentFieldValue = null;
                    break;
                }
                default: {
                    // do nothing
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            try {
                switch (qName) {
                    case DICTIONARY: {
                        consumer.startTable(new DefaultTableMetaData(tableName, ArrayUtil.toArray(Column.class, columns.values())));
                        for (Map<String, String> row : rows) {
                            consumer.row(createRow(columns.keySet(), row));
                        }
                        consumer.endTable();
                        columns = null;
                        rows = null;
                        break;
                    }
                    case RECORD: {
                        rows.add(currentRecord);
                        currentRecord = null;
                        break;
                    }
                    case FIELD: {
                        currentRecord.put(currentFieldName, currentFieldValue);
                        currentFieldName = null;
                        currentFieldValue = null;
                        break;
                    }
                    default: {
                        // do nothing
                    }
                }
            } catch (DataSetException ex) {
                throw new SAXException(ex);
            }
        }
    }
}
