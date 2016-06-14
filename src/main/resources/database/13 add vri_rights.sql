	--set session authorization geo_ov;

	 
		create table gebruikervrirights (
			id  bigserial not null,
			editable bool not null,
			readable bool not null,
			gebruiker int4,
			roadside_equipment int8,
			primary key (id)
		);

		alter table gebruikervrirights 
			add constraint FK284C9BAEECAEC3F 
			foreign key (gebruiker) 
			references gebruiker;

		alter table gebruikervrirights 
			add constraint FK284C9BAE58628730 
			foreign key (roadside_equipment) 
			references roadside_equipment;
