package ru.hflabs.rcd.connector.files.dataset;

import org.apache.commons.io.FileUtils;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.ITable;

import java.io.File;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public abstract class DataSetTest {

    /*
     * Ожидаемый результат
     */
    protected static final String[] COLUMNS = new String[]{"HID", "NAME", "DESCRIPTION column"};
    protected static final String[] ROW_1 = new String[]{"1", "ONE>", "Без разделителей и кавычек"};
    protected static final String[] ROW_2 = new String[]{"2", "ON;E", "С разделителем, без кавычек"};
    protected static final String[] ROW_3 = new String[]{"3", "O \"N\" E", "Без разделителя, c кавычками"};
    protected static final String[] ROW_4 = new String[]{"4", "O \"N;\" E", "C разделителем и кавычками"};
    protected static final String[] ROW_5 = new String[]{"5", null, "Пустое значение"};
    protected static final String[][] ROWS = new String[][]{
            ROW_1, ROW_2, ROW_3, ROW_4, ROW_5
    };

    /** Директория теста */
    protected File workingDirectory;

    public DataSetTest() {
        workingDirectory = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        assertTrue(workingDirectory.exists() || workingDirectory.mkdir());
    }

    /**
     * Выполняет проверку таблицы
     *
     * @param table целевая таблица
     */
    protected static void assertTable(ITable table) throws Exception {
        // Колонки
        Column[] columns = table.getTableMetaData().getColumns();
        assertEquals(columns.length, COLUMNS.length);
        for (int i = 0; i < columns.length; i++) {
            assertEquals(columns[i].getColumnName(), COLUMNS[i]);
        }
        // Строки
        assertEquals(table.getRowCount(), ROWS.length);
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int column = 0; column < columns.length; column++) {
                assertEquals(table.getValue(row, columns[column].getColumnName()), ROWS[row][column]);
            }
        }
    }
}
