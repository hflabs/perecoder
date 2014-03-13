-- -----------------------------------------------------
-- Notification table
-- -----------------------------------------------------
ALTER TABLE t_notification ADD rule_set_name NVARCHAR2(255) NULL;

-- -----------------------------------------------------
-- RuleSet table
-- -----------------------------------------------------
ALTER TABLE t_recoderuleset ADD name NVARCHAR2(255);
UPDATE t_recoderuleset SET name = id;
COMMIT;
ALTER TABLE t_recoderuleset MODIFY name NOT NULL;

-- -----------------------------------------------------
-- Task result table
-- -----------------------------------------------------
ALTER TABLE t_taskresult ADD CONSTRAINT pk_taskresult PRIMARY KEY (id) USING INDEX pk_taskresult;
