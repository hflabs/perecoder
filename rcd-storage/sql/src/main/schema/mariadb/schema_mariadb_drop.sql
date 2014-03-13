-- -----------------------------------------------------
-- DROP USER & TABLESPACE
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS ${storage.jdbc.username};

DROP USER ${storage.jdbc.username};
