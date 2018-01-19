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

CREATE OR REPLACE VIEW v_gemeente AS 
 SELECT g.gid,
    g.id,
    g.gml_id,
    g.code,
    g.gemeentena,
    g.geom,
    (select r.id as aao from roadside_equipment r where g.geom && r.location and st_intersects(g.geom, r.location) limit 1) is not null as hasrseqs
   FROM gemeente as g;

ALTER TABLE v_gemeente
  OWNER TO geo_ov;
