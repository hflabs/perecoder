package org.hibernate.dialect;

import java.sql.Types;

/**
 * Класс <class>HSQLOracleDialect</class> реализует симуляцию диалекта ORACLE для HSQLDB
 *
 * @author Nazin Alexander
 */
public class HSQLOracleDialect extends HSQLDialect {

    public HSQLOracleDialect() {
        registerColumnType(Types.BOOLEAN, "INTEGER(1)");
        registerColumnType(Types.VARCHAR, "nvarchar2($l)");
    }
}
