alter table activation_point drop column location;
alter table roadside_equipment drop column location;
select addGeometryColumn('data','activation_point','location',28992,'POINT',2);
select addGeometryColumn('data','roadside_equipment','location',28992,'POINT',2);
