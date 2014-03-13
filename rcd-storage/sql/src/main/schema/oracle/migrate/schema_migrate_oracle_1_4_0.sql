-- -----------------------------------------------------
-- Database schema version history
-- -----------------------------------------------------
CREATE TABLE schema_version (
  version            NVARCHAR2(20)  NOT NULL,
  description        NVARCHAR2(100) NULL,
  type               NVARCHAR2(10)  NOT NULL,
  script             NVARCHAR2(200) NOT NULL,
  checksum           INTEGER        NULL,
  installed_by       NVARCHAR2(30)  NOT NULL,
  installed_on       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
  execution_time     INTEGER        NULL,
  state              NVARCHAR2(15)  NOT NULL,
  current_version    NUMBER(1)      NOT NULL
);

CREATE UNIQUE INDEX pk_schema_version        ON schema_version (version);
CREATE UNIQUE INDEX uk_schema_version_script ON schema_version (script);
CREATE        INDEX idx_schema_version_cv    ON schema_version (current_version);

ALTER TABLE schema_version ADD CONSTRAINT pk_schema_version PRIMARY KEY (version) USING INDEX pk_schema_version;

-- -----------------------------------------------------
-- Fill initial schema version
-- -----------------------------------------------------
INSERT INTO schema_version (version, type, script, installed_by, installed_on, state, current_version)
  VALUES('1.4.z', 'SQL', 'initial', '${storage.jdbc.username}', SYSDATE, 'SUCCESS', 1);
  
COMMIT;

-- -----------------------------------------------------
-- History table
-- -----------------------------------------------------
CREATE TABLE t_history (
  id           NVARCHAR2(36) NOT NULL,
  target_id    NVARCHAR2(36) NOT NULL,
  target_type  NVARCHAR2(20) NOT NULL,
  event_type   NVARCHAR2(20) NOT NULL,
  event_date   TIMESTAMP     NOT NULL,
  event_author NVARCHAR2(50) NOT NULL
);

CREATE UNIQUE INDEX pk_history         ON t_history (id);
CREATE        INDEX idx_history_target ON t_history (target_id, event_type);

ALTER TABLE t_history ADD CONSTRAINT pk_history PRIMARY KEY (id) USING INDEX pk_history;

-- -----------------------------------------------------
-- History ID for document tables - all tables must be empty
-- Remove conflict with history indexes
-- -----------------------------------------------------
ALTER TABLE t_group ADD history_id NVARCHAR2(36) NOT NULL;
ALTER TABLE t_group ADD CONSTRAINT fk_group_history_id FOREIGN KEY (history_id) REFERENCES t_history (id) INITIALLY DEFERRED;
DROP INDEX uk_group_name;

ALTER TABLE t_dictionary ADD history_id NVARCHAR2(36) NOT NULL;
ALTER TABLE t_dictionary ADD CONSTRAINT fk_dictionary_history_id FOREIGN KEY (history_id) REFERENCES t_history (id) INITIALLY DEFERRED;
DROP INDEX uk_dictionary_name;

ALTER TABLE t_metafield ADD history_id NVARCHAR2(36) NOT NULL;
ALTER TABLE t_metafield ADD CONSTRAINT fk_metafield_history_id FOREIGN KEY (history_id) REFERENCES t_history (id) INITIALLY DEFERRED;
DROP INDEX uk_meta_name;

ALTER TABLE t_field ADD history_id NVARCHAR2(36) NOT NULL;
ALTER TABLE t_field ADD CONSTRAINT fk_field_history_id FOREIGN KEY (history_id) REFERENCES t_history (id) INITIALLY DEFERRED;
DROP INDEX uk_field_value;

ALTER TABLE t_recoderuleset ADD history_id NVARCHAR2(36) NOT NULL;
ALTER TABLE t_recoderuleset ADD CONSTRAINT fk_recoderuleset_history_id FOREIGN KEY (history_id) REFERENCES t_history (id) INITIALLY DEFERRED;
DROP INDEX uk_meta_field_id;

ALTER TABLE t_recoderule ADD history_id NVARCHAR2(36) NOT NULL;
ALTER TABLE t_recoderule ADD CONSTRAINT fk_recoderule_history_id FOREIGN KEY (history_id) REFERENCES t_history (id) INITIALLY DEFERRED;
ALTER TABLE t_recoderule DROP CONSTRAINT uk_recoderuleset;
DROP INDEX uk_recoderuleset;

ALTER TABLE t_notification ADD history_id NVARCHAR2(36) NOT NULL;
ALTER TABLE t_notification ADD CONSTRAINT fk_notification_history_id FOREIGN KEY (history_id) REFERENCES t_history (id) INITIALLY DEFERRED;
