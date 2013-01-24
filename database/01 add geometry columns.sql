alter table activation_point2 drop column location;
alter table roadside_equipment2 drop column location;
select addGeometryColumn('data','activation_point2','location',28992,'POINT',2);
select addGeometryColumn('data','roadside_equipment2','location',28992,'POINT',2);
