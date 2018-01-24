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


-- Table: dxf

-- DROP TABLE dxf;

CREATE TABLE dxf
(
  id serial NOT NULL,
  the_geom geometry(Geometry,28992),
  layer character varying(255),
  name character varying(255),
  text character varying(255),
  textposhorizontal character varying(255),
  textposvertical character varying(255),
  textheight double precision,
  textrotation double precision,
  color character varying(255),
  linetype character varying(255),
  thickness double precision,
  visible integer,
  linenumber integer,
  error character varying(255),
  data_owner bigint,
  filename character varying(255),
  uploaddate timestamp without time zone,
  user_ integer,
  description character varying(255),
  rseq bigint,
  CONSTRAINT prim_key_dxf_id PRIMARY KEY (id),
  CONSTRAINT fk_data_owner FOREIGN KEY (data_owner)
      REFERENCES data_owner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE dxf
  OWNER TO geo_ov;
