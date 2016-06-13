
    create table activation_point (
        id  bigserial not null,
        label varchar(255),
        location geometry not null,
        nummer int4 not null,
        roadside_equipment int8 not null,
        primary key (id)
    );

    create table activation_point_signal (
        id  bigserial not null,
        distance_till_stop_line int4,
        kar_command_type int4 not null,
        signal_group_number int4,
        trigger_type varchar(255),
        virtual_local_loop_number int4,
        primary key (id)
    );

    create table activation_point_signal_vehicle_types (
        activation_point_signal int8 not null,
        vehicle_types int4 not null
    );

    create table data_owner (
        code varchar(255) not null,
        classificatie varchar(255),
        company_number int4,
        omschrijving varchar(255),
        primary key (code)
    );

    create table gebruiker (
        id  serial not null,
        email varchar(255),
        fullname varchar(255),
        passwordhash varchar(255),
        passwordsalt varchar(255),
        phone varchar(255),
        position varchar(255),
        username varchar(255) not null unique,
        primary key (id)
    );

    create table gebruiker_data_owner_rights (
        editable bool not null,
        validatable bool not null,
        gebruiker int4 not null,
        data_owner varchar(255) not null,
        primary key (gebruiker, data_owner)
    );

    create table gebruiker_roles (
        gebruiker int4 not null,
        role int4 not null,
        primary key (gebruiker, role)
    );

    create table movement (
        id  bigserial not null,
        nummer int4,
        roadside_equipment int8 not null,
        primary key (id)
    );

    create table movement_activation_point (
        id  bigserial not null,
        begin_end_or_activation varchar(255),
        movement int8 not null,
        point int8,
        signal int8,
        primary key (id)
    );

    create table movement_points (
        movement int8 not null,
        point int8 not null,
        list_index int4 not null,
        primary key (movement, list_index)
    );

    create table roadside_equipment (
        id  bigserial not null,
        crossing_code varchar(255),
        description varchar(255),
        kar_address int4,
        location geometry,
        town varchar(50),
        type varchar(255),
        valid_from date,
        valid_until date,
        data_owner varchar(255) not null,
        primary key (id)
    );

    create table roadside_equipment_kar_attributes (
        roadside_equipment int8 not null,
        command_type int4 not null,
        service_type varchar(255) not null,
        used_attributes_mask int4 not null,
        list_index int4 not null,
        primary key (roadside_equipment, list_index)
    );

    create table role (
        id int4 not null,
        role varchar(255),
        primary key (id)
    );

    create table vehicle_type (
        nummer int4 not null,
        omschrijving varchar(255),
        primary key (nummer)
    );

    alter table activation_point 
        add constraint FK4A49912758628730 
        foreign key (roadside_equipment) 
        references roadside_equipment;

    alter table activation_point_signal_vehicle_types 
        add constraint FKCB1C4C67860074FF 
        foreign key (vehicle_types) 
        references vehicle_type;

    alter table activation_point_signal_vehicle_types 
        add constraint FKCB1C4C6715878535 
        foreign key (activation_point_signal) 
        references activation_point_signal;

    alter table gebruiker_data_owner_rights 
        add constraint FK5AA945AFECAEC3F 
        foreign key (gebruiker) 
        references gebruiker;

    alter table gebruiker_data_owner_rights 
        add constraint FK5AA945AFFF3C629A 
        foreign key (data_owner) 
        references data_owner;

    alter table gebruiker_roles 
        add constraint FK693FF394ECAEC3F 
        foreign key (gebruiker) 
        references gebruiker;

    alter table gebruiker_roles 
        add constraint FK693FF394D9339979 
        foreign key (role) 
        references role;

    alter table movement 
        add constraint FKF9D200AF58628730 
        foreign key (roadside_equipment) 
        references roadside_equipment;

    alter table movement_activation_point 
        add constraint FK3D7A86B78E238B6B 
        foreign key (movement) 
        references movement;

    alter table movement_activation_point 
        add constraint FK3D7A86B76382F4FD 
        foreign key (point) 
        references activation_point;

    alter table movement_activation_point 
        add constraint FK3D7A86B7D4C8631D 
        foreign key (signal) 
        references activation_point_signal;

    alter table movement_points 
        add constraint FK1FB806738E238B6B 
        foreign key (movement) 
        references movement;

    alter table movement_points 
        add constraint FK1FB806737F1D554E 
        foreign key (point) 
        references movement_activation_point;

    alter table roadside_equipment 
        add constraint FK5FFC3C6FF3C629A 
        foreign key (data_owner) 
        references data_owner;

    alter table roadside_equipment_kar_attributes 
        add constraint FKD4E0453358628730 
        foreign key (roadside_equipment) 
        references roadside_equipment;
