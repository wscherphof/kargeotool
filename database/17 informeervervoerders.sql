
    create table inform_message (
        id  serial not null,
        afzender int4 not null,
        rseq int8 not null,
        vervoerder int4 not null,
        primary key (id)
    );

    alter table inform_message 
        add constraint FK6F882D3152985217 
        foreign key (rseq) 
        references roadside_equipment;

    alter table inform_message 
        add constraint FK6F882D31A81DF4DC 
        foreign key (afzender) 
        references gebruiker;

    alter table inform_message 
        add constraint FK6F882D31E28C1611 
        foreign key (vervoerder) 
        references gebruiker;