
    create table gebruiker (
        id serial not null,
        username varchar(255),
        passwordsalt varchar(255),
        passwordhash varchar(255),
        fullname varchar(255),
        email varchar(255),
        phone varchar(255),
        position varchar(255),
        primary key (id)
    );

    create table gebruiker_roles (
        gebruiker int4 not null,
        role int4 not null,
        primary key (gebruiker, role)
    );

    create table role (
        id serial not null,
        role varchar(255),
        primary key (id)
    );

    alter table gebruiker_roles 
        add constraint FK693FF394ECAEC3F 
        foreign key (gebruiker) 
        references gebruiker;

    alter table gebruiker_roles 
        add constraint FK693FF394D9339979 
        foreign key (role) 
        references role;
-- create sequence gebruiker_id_seq;
-- create sequence role_id_seq;
