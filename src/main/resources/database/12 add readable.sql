ALTER TABLE gebruiker_data_owner_rights
  DROP COLUMN validatable;
ALTER TABLE gebruiker_data_owner_rights
  ADD COLUMN readable boolean;
ALTER TABLE gebruiker_data_owner_rights
  DROP CONSTRAINT fk5aa945afecaec3f;
ALTER TABLE gebruiker_data_owner_rights
  DROP CONSTRAINT fk5aa945afff3c629a;
ALTER TABLE gebruiker_data_owner_rights
  ADD CONSTRAINT fk5aa945afecaec3f FOREIGN KEY (gebruiker)
      REFERENCES gebruiker (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE gebruiker_data_owner_rights
  ADD CONSTRAINT fk5aa945afff3c629a FOREIGN KEY (data_owner)
      REFERENCES data_owner (code) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
