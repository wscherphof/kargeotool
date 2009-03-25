CREATE TABLE karpunt
(
   id serial, 
   description character varying(50), 
    PRIMARY KEY (id)
) WITH (OIDS=FALSE)
;
ALTER TABLE karpunt OWNER TO kar_gis;

select addGeometryColumn('public','karpunt','the_geom',28992,'POINT',2);