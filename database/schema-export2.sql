
    create table activation_point2 (
        id  bigserial not null,
        label varchar(255),
        location bytea not null,
        nummer int4 not null,
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

    create table data_owner2 (
        code varchar(255) not null,
        classificatie varchar(255),
        company_number int4,
        omschrijving varchar(255),
        primary key (code)
    );

    create table movement (
        id  bigserial not null,
        nummer int4,
        roadside_equipment int8,
        primary key (id)
    );

    create table movement_activation_point (
        id  bigserial not null,
        begin_end_or_activation varchar(255),
        movement int8,
        point int8,
        signal int8,
        primary key (id)
    );

    create table roadside_equipment2 (
        id  bigserial not null,
        crossing_code varchar(255),
        description varchar(255),
        kar_address int4,
        location bytea,
        town varchar(50),
        type varchar(255),
        valid_from date,
        valid_until date,
        data_owner varchar(255) not null,
        primary key (id)
    );

    create table roadside_equipment2_kar_attributes (
        roadside_equipment2 int8 not null,
        command_type int4 not null,
        service_type varchar(255) not null,
        used_attributes_mask int4 not null,
        list_index int4 not null,
        primary key (roadside_equipment2, list_index)
    );

    create table vehicle_type (
        nummer int4 not null,
        omschrijving varchar(255),
        primary key (nummer)
    );

    alter table activation_point_signal_vehicle_types 
        add constraint FKCB1C4C67860074FF 
        foreign key (vehicle_types) 
        references vehicle_type;

    alter table activation_point_signal_vehicle_types 
        add constraint FKCB1C4C6715878535 
        foreign key (activation_point_signal) 
        references activation_point_signal;

    alter table movement 
        add constraint FKF9D200AFFFF56DCE 
        foreign key (roadside_equipment) 
        references roadside_equipment2;

    alter table movement_activation_point 
        add constraint FK3D7A86B78E238B6B 
        foreign key (movement) 
        references movement;

    alter table movement_activation_point 
        add constraint FK3D7A86B74DCDA3F5 
        foreign key (point) 
        references activation_point2;

    alter table movement_activation_point 
        add constraint FK3D7A86B7D4C8631D 
        foreign key (signal) 
        references activation_point_signal;

    alter table roadside_equipment2 
        add constraint FKB9F8B52C9E8AF0D4 
        foreign key (data_owner) 
        references data_owner2;

    alter table roadside_equipment2_kar_attributes 
        add constraint FKE3E59B0DB3EE5F34 
        foreign key (roadside_equipment2) 
        references roadside_equipment2;

