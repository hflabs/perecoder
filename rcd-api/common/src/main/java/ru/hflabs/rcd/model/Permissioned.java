package ru.hflabs.rcd.model;

/**
 * Интерфейс <class>Permissioned</class> декларирует методы объекта, который обладает правами безопасности
 *
 * @see <a href="http://en.wikipedia.org/wiki/Umask">Umask</a>
 */
public interface Permissioned {

    /*
     * Доступные права
     */
    int PERMISSION_READ = 0b01;
    String PERMISSION_READ_NAME = "readable";
    int PERMISSION_WRITE = 0b11;
    String PERMISSION_WRITE_NAME = "writable";

    int PERMISSION_ALL = PERMISSION_READ | PERMISSION_WRITE;

    /**
     * @return Возвращает текущий набор прав
     */
    int getPermissions();

    /**
     * Устанавливает набор прав
     *
     * @param permissions набор
     */
    void setPermissions(int permissions);
}
