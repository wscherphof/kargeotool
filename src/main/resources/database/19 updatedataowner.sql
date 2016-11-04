alter table data_owner add column id bigserial ;
alter table roadside_equipment drop constraint fkb9f8b52c9e8af0d4;
alter table gebruiker_data_owner_rights drop constraint fk5aa945af9e8af0d4;
alter table data_owner drop constraint data_owner2_pkey;

alter table data_owner add constraint data_owner_pkey2 primary key (id) ;



alter table roadside_equipment add column data_owner_tmp bigint;
update roadside_equipment set data_owner_tmp = (select id from data_owner where code = data_owner);



alter table gebruiker_data_owner_rights add column data_owner_tmp bigint;
update gebruiker_data_owner_rights set data_owner_tmp = (select id from data_owner where code = data_owner);



alter table roadside_equipment drop column data_owner;
alter table roadside_equipment rename column data_owner_tmp to data_owner;

alter table gebruiker_data_owner_rights drop column data_owner;
alter table gebruiker_data_owner_rights rename column data_owner_tmp to data_owner;


alter table roadside_equipment add foreign key (data_owner) references data_owner(id);
alter table gebruiker_data_owner_rights add foreign key (data_owner) references data_owner(id);

update data_owner set code = 'CBSPV0020' where code = 'CBSPV0001';
update data_owner set code = 'CBSPV0021' where code = 'CBSPV0002';
update data_owner set code = 'CBSPV0022' where code = 'CBSPV0003';
update data_owner set code = 'CBSPV0023' where code = 'CBSPV0004';
update data_owner set code = 'CBSPV0024' where code = 'CBSPV0012';
update data_owner set code = 'CBSPV0025' where code = 'CBSPV0005';
update data_owner set code = 'CBSPV0026' where code = 'CBSPV0006';
update data_owner set code = 'CBSPV0027' where code = 'CBSPV0007';
update data_owner set code = 'CBSPV0028' where code = 'CBSPV0008';
update data_owner set code = 'CBSPV0029' where code = 'CBSPV0009';
update data_owner set code = 'CBSPV0030' where code = 'CBSPV0010';
update data_owner set code = 'CBSPV0031' where code = 'CBSPV0011';