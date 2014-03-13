package ru.hflabs.rcd.connector.files;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Класс <class>FilesByExtensionFilter</class> реализует сервис фильтрации файлов по из расширению на основе {@link Pattern регулярного выражения}
 *
 * @author Nazin Alexander
 */
public class FilesByExtensionFilter implements FileFilter {

    /*
     * Поддерживаемые типы файлов
     */
    public static final String CSV = "csv";
    public static final String XML = "xml";
    public static final String XLS = "xls";

    /** Статический экземпляры класса */
    public static final FileFilter CSV_FILTER = new FilesByExtensionFilter(CSV);
    public static final FileFilter XML_FILTER = new FilesByExtensionFilter(XML);
    public static final FileFilter XLS_FILTER = new FilesByExtensionFilter(XLS);

    /** Расширение файла */
    private final Pattern pattern;

    public FilesByExtensionFilter(String extension) {
        this.pattern = Pattern.compile(String.format(".*\\.(%s)", extension), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean accept(File pathName) {
        return pathName.isFile() && pattern.matcher(pathName.getName()).matches();
    }
}
