package ru.hflabs.rcd.connector.files.dataset.xml;

import com.google.common.base.Charsets;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import ru.hflabs.rcd.connector.files.FilesByExtensionFilter;
import ru.hflabs.rcd.connector.files.dataset.FilesConsumer;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Класс <class>XmlConsumer</class> реализует формирование XML файлов на основе {@link org.dbunit.dataset.IDataSet таблиц}
 *
 * @author Nazin Alexander
 */
public class XmlConsumer extends FilesConsumer<Document> implements DictionaryTags {

    public XmlConsumer(File targetDirectory) {
        super(targetDirectory, FilesByExtensionFilter.XML);
    }

    @Override
    protected Document createWriter() throws IOException {
        InputStream xsdInputStream = getClass().getClassLoader().getResourceAsStream(DictionaryTags.XSD_SCHEMA);
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new StreamSource(xsdInputStream));
                documentBuilderFactory.setSchema(schema);
                documentBuilderFactory.setNamespaceAware(true);
            }
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.newDocument();
        } catch (SAXException | ParserConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected void closeWriter(Document writer) {
        // do nothing
    }

    @Override
    protected void writeHeader() throws IOException {
        Document document = currentWriter;
        // Dictionary
        Element dictionary = document.createElementNS(XSD_NAMESPACE, DICTIONARY);
        document.appendChild(dictionary);
        // MetaFields
        for (String columnName : currentHeaders) {
            Element metaField = document.createElement(META_FIELD);
            metaField.setAttribute(NAME, columnName);
            dictionary.appendChild(metaField);
        }
    }

    @Override
    public void endTable() throws DataSetException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            {
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
                transformer.setOutputProperty(OutputKeys.ENCODING, Charsets.UTF_8.name());
            }

            DOMSource source = new DOMSource(currentWriter);
            StreamResult result = new StreamResult(currentFile);

            transformer.transform(source, result);
        } catch (TransformerException ex) {
            throw new DataSetException(ex);
        } finally {
            super.endTable();
        }
    }

    @Override
    public void row(Object[] values) throws DataSetException {
        Document document = currentWriter;
        // Dictionary
        Element dictionary = document.getDocumentElement();
        // Record
        Element record = document.createElement(RECORD);
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            Element field = document.createElement(FIELD);
            field.setAttribute(NAME, currentHeaders[i]);
            field.setTextContent(value != null ? String.valueOf(value) : null);
            record.appendChild(field);
        }
        dictionary.appendChild(record);
    }

    public static List<File> write(IDataSet dataSet, File directory) throws DataSetException {
        return new XmlConsumer(directory).write(dataSet);
    }

    public static List<File> write(ITable table, File directory) throws DataSetException {
        return write(new DefaultDataSet(table), directory);
    }
}
