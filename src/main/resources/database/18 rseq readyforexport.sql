ALTER TABLE roadside_equipment
  ADD COLUMN ready_for_export boolean default false;

update roadside_equipment set ready_for_export = false where ready_for_export is null;