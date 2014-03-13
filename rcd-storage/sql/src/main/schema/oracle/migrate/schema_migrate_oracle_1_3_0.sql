-- -----------------------------------------------------
-- Make ALL FOREIGN KEY DEFERRED
-- -----------------------------------------------------
ALTER TABLE t_dictionary DROP CONSTRAINT fk_group_id;
ALTER TABLE t_dictionary ADD CONSTRAINT  fk_group_id FOREIGN KEY (group_id) REFERENCES t_group (id) INITIALLY DEFERRED;

ALTER TABLE t_metafield DROP CONSTRAINT fk_dictionary_id;
ALTER TABLE t_metafield ADD CONSTRAINT  fk_dictionary_id FOREIGN KEY (dictionary_id) REFERENCES t_dictionary (id) INITIALLY DEFERRED;

ALTER TABLE t_field DROP CONSTRAINT fk_metafield_id;
ALTER TABLE t_field ADD CONSTRAINT  fk_metafield_id FOREIGN KEY (meta_field_id) REFERENCES t_metafield (id) INITIALLY DEFERRED;

ALTER TABLE t_recoderuleset DROP CONSTRAINT fk_from_meta_field_id;
ALTER TABLE t_recoderuleset ADD CONSTRAINT  fk_from_meta_field_id FOREIGN KEY (from_field_id)    REFERENCES t_metafield (id) INITIALLY DEFERRED;
ALTER TABLE t_recoderuleset DROP CONSTRAINT fk_to_meta_field_id;
ALTER TABLE t_recoderuleset ADD CONSTRAINT  fk_to_meta_field_id   FOREIGN KEY (to_field_id)      REFERENCES t_metafield (id) INITIALLY DEFERRED;
ALTER TABLE t_recoderuleset DROP CONSTRAINT fk_default_field_id;
ALTER TABLE t_recoderuleset ADD CONSTRAINT  fk_default_field_id   FOREIGN KEY (default_field_id) REFERENCES t_field     (id) INITIALLY DEFERRED;

ALTER TABLE t_recoderule DROP CONSTRAINT fk_recode_rule_set_id;
ALTER TABLE t_recoderule ADD CONSTRAINT  fk_recode_rule_set_id FOREIGN KEY (recode_rule_set_id) REFERENCES t_recoderuleset (id) INITIALLY DEFERRED;
ALTER TABLE t_recoderule DROP CONSTRAINT fk_from_field_id;
ALTER TABLE t_recoderule ADD CONSTRAINT  fk_from_field_id      FOREIGN KEY (from_field_id)      REFERENCES t_field (id) INITIALLY DEFERRED;
ALTER TABLE t_recoderule DROP CONSTRAINT fk_to_field_id;
ALTER TABLE t_recoderule ADD CONSTRAINT  fk_to_field_id        FOREIGN KEY (to_field_id)        REFERENCES t_field (id) INITIALLY DEFERRED;
ALTER TABLE t_recoderule DROP CONSTRAINT uk_recoderuleset;
ALTER TABLE t_recoderule ADD CONSTRAINT  uk_recoderuleset      UNIQUE      (recode_rule_set_id, from_field_id) INITIALLY DEFERRED;

-- -----------------------------------------------------
-- change VARCHAR2 to NVARCHAR2
-- -----------------------------------------------------
BEGIN
  -- disable constraints
  FOR c IN (SELECT c.table_name, c.constraint_name FROM user_constraints c, user_tables t WHERE c.table_name = t.table_name AND c.status = 'ENABLED' ORDER BY c.constraint_type DESC) LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE '|| c.table_name ||' DISABLE CONSTRAINT '|| c.constraint_name ||'';
  END LOOP;
  -- modify columns
  FOR x IN (SELECT table_name, column_name, data_length FROM user_tab_columns WHERE data_type = 'VARCHAR2') LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE '|| x.table_name ||' MODIFY '|| x.column_name ||' NVARCHAR2('|| x.data_length ||')';
  END LOOP;
  -- enable constraints
  FOR c IN (SELECT c.table_name, c.constraint_name FROM user_constraints c, user_tables t WHERE c.table_name = t.table_name AND c.status = 'DISABLED' ORDER BY c.constraint_type) LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE '|| c.table_name ||' ENABLE CONSTRAINT '|| c.constraint_name ||'';
  END LOOP;
END;
/

-- -----------------------------------------------------
-- t_metafield
-- -----------------------------------------------------
ALTER TABLE t_metafield ADD description NVARCHAR2(100) NULL;
