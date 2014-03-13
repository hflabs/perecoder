-- -----------------------------------------------------
-- Group table
-- -----------------------------------------------------
ALTER TABLE t_group MODIFY name NVARCHAR2(255);
ALTER TABLE t_group MODIFY description NVARCHAR2(1000);
ALTER TABLE t_group ADD owner NVARCHAR2(255) NULL;
ALTER TABLE t_group ADD permissions INTEGER;
UPDATE t_group set permissions = 3;
COMMIT;
ALTER TABLE t_group MODIFY permissions INTEGER NOT NULL;

-- -----------------------------------------------------
-- Dictionaries table
-- -----------------------------------------------------
ALTER TABLE t_dictionary ADD code NVARCHAR2(255) NULL;
ALTER TABLE t_dictionary MODIFY name NVARCHAR2(255);
ALTER TABLE t_dictionary MODIFY description NVARCHAR2(1000);
ALTER TABLE t_dictionary ADD version NVARCHAR2(100) NULL;
CREATE UNIQUE INDEX uk_dictionary_version ON t_dictionary (id, version);

-- -----------------------------------------------------
-- MetaField table
-- -----------------------------------------------------
ALTER TABLE t_metafield MODIFY name  NVARCHAR2(255);
ALTER TABLE t_metafield MODIFY description NVARCHAR2(1000);

-- -----------------------------------------------------
-- Field table
-- -----------------------------------------------------
ALTER TABLE t_field ADD value_lob CLOB;
UPDATE t_field SET value_lob = value, value = null;
COMMIT;
ALTER TABLE t_field DROP COLUMN value;
ALTER TABLE t_field RENAME COLUMN value_lob TO value;

-- -----------------------------------------------------
-- Notification table
-- -----------------------------------------------------
ALTER TABLE t_notification MODIFY from_group_name NVARCHAR2(255);
ALTER TABLE t_notification MODIFY from_dictionary_name  NVARCHAR2(255);
ALTER TABLE t_notification MODIFY to_group_name NVARCHAR2(255);
ALTER TABLE t_notification MODIFY to_dictionary_name NVARCHAR2(255);
ALTER TABLE t_notification ADD from_value_lob CLOB;
UPDATE t_notification SET from_value_lob = from_value, from_value = null;
COMMIT;
ALTER TABLE t_notification DROP COLUMN from_value;
ALTER TABLE t_notification RENAME COLUMN from_value_lob TO from_value;

-- -----------------------------------------------------
-- Task descriptor table
-- -----------------------------------------------------
CREATE TABLE t_taskdescriptor (
  id          NVARCHAR2(36)   NOT NULL,
  history_id  NVARCHAR2(36)   NOT NULL,
  name        NVARCHAR2(255)  NOT NULL,
  description NVARCHAR2(1000) NOT NULL,
  cron        NVARCHAR2(100)  NULL,
  parameters  BLOB            NULL
);

CREATE UNIQUE INDEX pk_taskdescriptor ON t_taskdescriptor (id);

ALTER TABLE t_taskdescriptor ADD CONSTRAINT pk_taskdescriptor            PRIMARY KEY (id)         USING INDEX pk_taskdescriptor;
ALTER TABLE t_taskdescriptor ADD CONSTRAINT fk_taskdescriptor_history_id FOREIGN KEY (history_id) REFERENCES t_history (id) INITIALLY DEFERRED;

-- -----------------------------------------------------
-- Task result table
-- -----------------------------------------------------
CREATE TABLE t_taskresult (
  id                NVARCHAR2(36)   NOT NULL,
  descriptor_id     NVARCHAR2(36)   NOT NULL,
  owner             NVARCHAR2(32)   NOT NULL,
  author            NVARCHAR2(50)   NOT NULL,
  registration_date TIMESTAMP       NOT NULL,
  start_date        TIMESTAMP       NOT NULL,
  end_date          TIMESTAMP       NOT NULL,
  status            NVARCHAR2(20)   NOT NULL,
  error_message     NVARCHAR2(2000) NULL,
  parameters        BLOB            NULL,
  content           BLOB            NULL
);

CREATE UNIQUE INDEX pk_taskresult ON t_taskresult (id);
CREATE UNIQUE INDEX uk_taskresult ON t_taskresult (id, registration_date);

ALTER TABLE t_taskresult ADD CONSTRAINT fk_taskresult_descriptor_id FOREIGN KEY (descriptor_id) REFERENCES t_taskdescriptor (id) INITIALLY DEFERRED;

-- -----------------------------------------------------
-- Default tasks
-- -----------------------------------------------------
-- ESNSI sync task
INSERT INTO t_history (id, target_id, target_type, event_type, event_date, event_author)
  VALUES ('cnsisynctaskperformer_hid', 'cnsisynctaskperformer_id', 'TASKDESCRIPTOR','CREATE', SYSDATE, 'HFLabs-RCD');
INSERT INTO t_taskdescriptor (id, history_id, name, description)
  VALUES ('cnsisynctaskperformer_id', 'cnsisynctaskperformer_hid', 'cnsiSyncTaskPerformer', 'Синхронизация с ЕСНСИ');
-- Dummy task
INSERT INTO t_history (id, target_id, target_type, event_type, event_date, event_author)
  VALUES ('dummytaskperformer_hid', 'dummytaskperformer_id', 'TASKDESCRIPTOR','CREATE', SYSDATE, 'HFLabs-RCD');
INSERT INTO t_taskdescriptor (id, history_id, name, description)
  VALUES ('dummytaskperformer_id', 'dummytaskperformer_hid', 'dummyTaskPerformer', 'Тестовая задача');

COMMIT;
