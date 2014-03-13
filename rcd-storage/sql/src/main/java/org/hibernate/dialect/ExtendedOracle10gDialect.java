package org.hibernate.dialect;

import java.sql.Types;

/**
 * Класс <class>ExtendedOracle10gDialect</class> реализует специфику диалекта для СУБД oracle
 *
 * @author Nazin Alexander
 */
public class ExtendedOracle10gDialect extends Oracle10gDialect {

    @Override
    protected void registerCharacterTypeMappings() {
        super.registerCharacterTypeMappings();
        registerColumnType(Types.VARCHAR, 2000, "nvarchar2($l)");
    }
}
