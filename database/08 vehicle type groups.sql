ALTER TABLE vehicle_type
  ADD COLUMN groep character varying(255);

  
update vehicle_type set groep = 'OV' WHERE nummer in (1,2,6,7,71);
update vehicle_type set groep ='Hulpdiensten' WHERE nummer in (3,4,5,69,70);