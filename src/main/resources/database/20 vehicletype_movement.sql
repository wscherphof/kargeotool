ALTER TABLE movement
  ADD COLUMN vehicle_type text;



CREATE INDEX roadside_equipment_geom_idx
  ON roadside_equipment
  USING gist
  (location);

set session authorization geo_ov;

select addgeometrycolumn('data','gemeente', 'geom_simplified', 28992, 'MULTIPOLYGON',2);

CREATE OR REPLACE VIEW v_provincie AS 
 SELECT p.gid, p.id, p.gml_id, p.provincien, p.geom, (( SELECT r.id AS aao
           FROM roadside_equipment r
      JOIN data_owner dao ON dao.id = r.data_owner
     WHERE ('CBSPV00'::text || p.id::text) = dao.code::text
    LIMIT 1)) IS NOT NULL AS hasrseqs
   FROM provincie p;

ALTER TABLE v_provincie
  OWNER TO geo_ov;

CREATE OR REPLACE VIEW v_gemeente AS 
 SELECT g.gid, g.id, g.gml_id, g.code, g.gemeentena, g.geom_simplified AS geom, (( SELECT r.id AS aao
           FROM roadside_equipment r
      JOIN data_owner dao ON dao.id = r.data_owner
     WHERE ('CBSGM'::text || g.code::text) = dao.code::text
    LIMIT 1)) IS NOT NULL AS hasrseqs
   FROM gemeente g;

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



select addgeometrycolumn('data','dxf_features', 'the_geom', 28992, 'GEOMETRY',2);

update gemeente set geom_simplified = st_multi(ST_SimplifyPreserveTopology(geom, 100));

CREATE INDEX gemeente_geom_simplified_idx
  ON gemeente
  USING gist
  (geom_simplified);


update deelgebied set geom = st_multi(geom);

update movement set nummer = 6 where id = 140999;
update movement set nummer = 7 where id = 138019;
update movement set nummer = 8 where id = 138020;

update movement set nummer = 7 where id = 137990;
update movement set nummer = 8 where id = 140836;
update movement set nummer = 9 where id = 7609;

update movement set nummer = 3 where id = 138007;

  CREATE OR REPLACE VIEW dxf_features_view AS 
 SELECT dxf_features.id, dxf_features.layer, dxf_features.name, dxf_features.text, dxf_features.textposhorizontal, dxf_features.textposvertical, dxf_features.textheight, dxf_features.textrotation, dxf_features.color, dxf_features.linetype, dxf_features.thickness, dxf_features.visible, dxf_features.linenumber, dxf_features.error, dxf_features.upload, dxf_features.the_geom, (('#'::text || to_hex(split_part(dxf_features.color::text, ' '::text, 1)::integer)) || to_hex(split_part(dxf_features.color::text, ' '::text, 2)::integer)) || to_hex(split_part(dxf_features.color::text, ' '::text, 3)::integer) AS hexcolor
   FROM dxf_features;

ALTER TABLE dxf_features_view
  OWNER TO geo_ov;
