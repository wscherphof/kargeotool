ALTER TABLE movement
  ADD COLUMN vehicle_type text;



CREATE INDEX roadside_equipment_geom_idx
  ON roadside_equipment
  USING gist
  (location);

set session authorization geo_ov;

CREATE OR REPLACE VIEW v_provincie AS 
 SELECT p.gid,
    p.id,
    p.gml_id,
    p.provincien,
    p.geom,
    (select r.id as aao from roadside_equipment r where p.geom && r.location and st_intersects(p.geom, r.location) limit 1) is not null as hasrseqs
   FROM provincie p;
