package ru.hflabs.rcd.connector.files;

import java.io.File;
import java.util.Comparator;
import java.util.List;

/**
 * Класс <class>FilesComparator</class> реализует сервисы сравнения названий файлов
 *
 * @author Nazin Alexander
 */
public abstract class FilesComparator {

    /**
     * Класс <class>ByName</class> реализует сервис сравнения файлов по их имени
     *
     * @see java.io.File#getName()
     */
    public static class ByName implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    /**
     * Класс <class>ByOrderingList</class> реализует сервис сравнения файлов по предопределенному списку
     *
     * @see java.io.File#getName()
     */
    public static class ByOrderingList implements Comparator<File> {

        /** Путь к файлу со списком */
        private final List<String> ordering;

        public ByOrderingList(List<String> ordering) {
            this.ordering = ordering;
        }

        @Override
        public int compare(File o1, File o2) {
            Integer o1index = ordering.indexOf(o1.getName());
            Integer o2index = ordering.indexOf(o2.getName());
            return o1index.compareTo(o2index);
        }
    }
}
