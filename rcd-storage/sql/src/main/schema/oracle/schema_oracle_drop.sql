-- -----------------------------------------------------
-- DROP USER & TABLESPACE
-- -----------------------------------------------------
DROP USER ${storage.jdbc.username} CASCADE;

DROP TABLESPACE ${storage.jdbc.username} INCLUDING CONTENTS AND DATAFILES CASCADE CONSTRAINTS;
