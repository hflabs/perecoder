package ru.hflabs.rcd;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * Класс <class>Version</class> содержит описание версии приложения
 */
public final class Version {

    public static final String VERSION = "version";
    public static final String REVISION = "revision";

    /** Регулярное вырежение выделения номера из snapshot-версии */
    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("-SNAPSHOT", Pattern.CASE_INSENSITIVE);

    private Version() {
        // private constructor
    }

    /**
     * @return Возвращает текущую версию приложения
     */
    public static String getCurrentVersion() {
        return "[UNKNOWN]";
    }

    /**
     * @return Возвращает текущую версию приложения с удалением суффиксов и префиксов
     */
    public static String getVersion() {
        return SNAPSHOT_PATTERN.matcher(getCurrentVersion()).replaceAll(StringUtils.EMPTY);
    }

    /**
     * @return Возвращает текущую ревизию приложения
     */
    public static String getRevision() {
        return "[UNKNOWN]";
    }
}
