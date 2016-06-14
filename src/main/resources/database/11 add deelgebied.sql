
    create table deelgebied (
        id  serial not null,
        geom geometry not null,
        name varchar(255),
        gebruiker int4 not null,
        primary key (id)
    );
