
    create table activation (
        id serial not null,
        activation_group int4 not null,
        index int4 not null,
        valid_from timestamp,
        kar_usage_type varchar(255) not null,
        type varchar(255) not null,
        command_type int4 not null,
        kar_distance_till_stop_line float8,
        kar_time_till_stop_line float8,
        kar_radio_power float8,
        meters_before_roadside_equipment_location float8,
        angle_to_north float8,
        updater varchar(255),
        update_time timestamp,
        validator varchar(255),
        validation_time timestamp,
        primary key (id),
        unique (activation_group, index, valid_from)
    );

    create table activation_group (
        id serial not null,
        roadside_equipment int4 not null,
        kar_signal_group int4 not null,
        valid_from timestamp not null,
        type varchar(255) not null,
        direction_at_intersection int4 not null,
        meters_before_roadside_equipment_location int4,
        inactive_from timestamp,
        angle_to_north float8,
        follow_direction bool,
        description text,
        updater varchar(255),
        update_time timestamp,
        validator varchar(255),
        validation_time timestamp,
        primary key (id),
        unique (roadside_equipment, kar_signal_group, valid_from)
    );

    create table data_owner (
        code varchar(255) not null,
        type varchar(255) not null,
        name varchar(255) not null,
        description varchar(255),
        primary key (code)
    );

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

    create table karpunt (
        id serial not null,
        description varchar(255),
        primary key (id)
    );

    create table roadside_equipment (
        id serial not null,
        data_owner varchar(255),
        unit_number int4,
        valid_from timestamp,
        type varchar(255) not null,
        radio_address varchar(255),
        description text,
        supplier varchar(255),
        primary key (id),
        unique (data_owner, unit_number, valid_from)
    );

    create table role (
        id serial not null,
        role varchar(255),
        primary key (id)
    );

    alter table activation 
        add constraint FK79AA811687EE0591 
        foreign key (activation_group) 
        references activation_group;

    alter table activation_group 
        add constraint FK49CC3216C584CE8F 
        foreign key (roadside_equipment) 
        references roadside_equipment;

    alter table gebruiker_roles 
        add constraint FK693FF394ECAEC3F 
        foreign key (gebruiker) 
        references gebruiker;

    alter table gebruiker_roles 
        add constraint FK693FF394D9339979 
        foreign key (role) 
        references role;

    alter table roadside_equipment 
        add constraint FK5FFC3C6686F78F9 
        foreign key (data_owner) 
        references data_owner;
-- create sequence activation_group_id_seq;
-- create sequence activation_id_seq;
-- create sequence gebruiker_id_seq;
-- create sequence karpunt_id_seq;
-- create sequence roadside_equipment_id_seq;
-- create sequence role_id_seq;
