-- -----------------------------------------------------
-- CREATE USER & TABLESPACE
-- -----------------------------------------------------
CREATE SCHEMA ${storage.jdbc.username} DEFAULT CHARACTER SET = UTF8;

-- -----------------------------------------------------
-- USER PRIVILEGES
-- -----------------------------------------------------
GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,ALTER,DROP,INDEX,LOCK TABLES,CREATE VIEW,EXECUTE
    ON ${storage.jdbc.username}.*
    TO '${storage.jdbc.username}' IDENTIFIED BY '${storage.jdbc.password}';
