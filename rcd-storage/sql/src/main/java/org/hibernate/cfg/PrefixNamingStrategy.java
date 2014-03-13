package org.hibernate.cfg;

import org.hibernate.internal.util.StringHelper;

/**
 * Класс <class>PrefixNamingStrategy</class> реализует стратегию преобразования объектов в SQL представление
 *
 * @author Nazin Alexander
 */
public class PrefixNamingStrategy extends ImprovedNamingStrategy {

    private static final long serialVersionUID = 6409251078204039930L;

    /** Префикс названия таблицы */
    private static final String TABLE_PREFIX = "t_";

    @Override
    public String classToTableName(String className) {
        String tableName = StringHelper.unqualify(className).toLowerCase();
        return tableName.startsWith(TABLE_PREFIX) ? tableName : TABLE_PREFIX + tableName;
    }
}
