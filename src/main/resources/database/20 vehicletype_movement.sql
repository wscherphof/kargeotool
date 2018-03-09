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


set session authorization geo_ov;
CREATE TABLE upload
(
  id serial NOT NULL,
  data_owner bigint,
  filename character varying(255),
  uploaddate timestamp without time zone,
  user_ integer,
  description character varying(255),
  rseq bigint,
  CONSTRAINT prim_key_upload_id PRIMARY KEY (id),
  CONSTRAINT fk_data_owner FOREIGN KEY (data_owner)
      REFERENCES data_owner (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE upload
  OWNER TO geo_ov;


CREATE TABLE dxf_features
(
  id serial NOT NULL,
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
  upload bigint,
  CONSTRAINT prim_key_dxf_features_id PRIMARY KEY (id),
  CONSTRAINT fk_upload FOREIGN KEY (upload)
      REFERENCES upload (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE dxf_features
  OWNER TO geo_ov;



select addgeometrycolumn('data','gemeente', 'geom_simplified', 28992, 'MULTIPOLYGON',2)

select addgeometrycolumn('data','dxf_features', 'the_geom', 28992, 'GEOMETRY',2)

update gemeente set geom_simplified = st_multi(ST_SimplifyPreserveTopology(geom, 100));

CREATE INDEX gemeente_geom_simplified_idx
  ON gemeente
  USING gist
  (geom_simplified);


   update deelgebied set geom = st_multi(geom)