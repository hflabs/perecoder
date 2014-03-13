-- -----------------------------------------------------
-- USE SCHEMA ${storage.jdbc.username}
-- -----------------------------------------------------
ALTER SESSION SET current_schema = ${storage.jdbc.username};

-- -----------------------------------------------------
-- Store LOBs separately
-- -----------------------------------------------------
ALTER TABLE t_field          MOVE LOB (value)      STORE AS (CACHE ENABLE STORAGE IN ROW);
ALTER TABLE t_notification   MOVE LOB (from_value) STORE AS (CACHE ENABLE STORAGE IN ROW);
ALTER TABLE t_taskdescriptor MOVE LOB (parameters) STORE AS (CACHE ENABLE STORAGE IN ROW);
ALTER TABLE t_taskresult     MOVE LOB (parameters) STORE AS (CACHE ENABLE STORAGE IN ROW);
ALTER TABLE t_taskresult     MOVE LOB (content)    STORE AS (CACHE ENABLE STORAGE IN ROW);
