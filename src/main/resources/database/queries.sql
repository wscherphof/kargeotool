	select r.id, r.description ,
	m.nummer, m.vehicle_type,
	map.id as mapid, map.begin_end_or_activation,

	case aps.kar_command_type when 1 THEN 'Inmeldpunt'  when 2 then 'Uitmeldpunt'  else  begin_end_or_activation end, 
	aps.signal_group_number, array(select nummer /*omschrijving */from activation_point_signal_vehicle_types apsvt inner join vehicle_type vt on vt.nummer = apsvt.vehicle_types where activation_point_signal = aps.id) as vehicletypes,
	aps.trigger_type,
	ap.label, ap.nummer, ap.id
	from roadside_equipment r
	left join movement m on m.roadside_equipment = r.id

	left join movement_activation_point map on m.id = map.movement
	left join movement_points mp on m.id = m.id and map.id = mp.point
	left join activation_point_signal aps on aps.id = map.signal
	left join activation_point ap on ap.id = map.point
	where r.id = 64

	order by r.id, m.nummer,mp.list_index



/*
update activation_point_signal set trigger_type = '1' where id in 
(select aps.id 
from movement m
join movement_activation_point map on map.movement = m .id
join activation_point_signal aps on aps.id = map.signal
where 
m.vehicle_type = 'Hulpdiensten' and
--roadside_equipment = 3092
 roadside_equipment in(select id from roadside_equipment  where data_owner in (121,158,19,20,113)))



 
/*select * 
from movement m
join movement_activation_point map on map.movement = m .id
join activation_point_signal aps on aps.id = map.signal
where 
m.vehicle_type = 'Hulpdiensten' and
--roadside_equipment = 3092
 roadside_equipment in(select id from roadside_equipment  where data_owner = 121)
*/

--select * from roadside_equipment  where kar_address = 4139
--(select id from data_owner where omschrijving ilike '%Doetinchem%')*/

/*

update activation_point_signal set trigger_type = 'STANDARD'  where id in 
(select map.signal
from movement m
join movement_activation_point map on map.movement = m .id
where 
 --m.roadside_equipment = 64 and
m.vehicle_type = 'OV' and trigger_type = '1')
*/

/*


select * from activation_point ap
inner join
movement_activation_point map on ap.id = map.point

where map.signal not in (select activation_point_signal from activation_point_signal_vehicle_types)
limit 10



*/




delete from activation_point_signal where id in ('39229','39233','39235','417789','417790','417792','417791');
delete from movement_points where point  in ('15228','15228','15228','93282','93283','93285','93286');
delete from movement_activation_point where id  in ('15228','15228','15228','93282','93283','93285','93286');
delete from activation_point where id in ('15228','15228','15228','93282','93283','93285','93286');
delete from movement where id not in (select movement from movement_activation_point );




/* daarna opslaan en query runnen: 
	select r.id, r.description ,
	m.id as mid, m.nummer, m.vehicle_type,
	map.id as mapid, map.begin_end_or_activation,

	case aps.kar_command_type when 1 THEN 'Inmeldpunt'  when 2 then 'Uitmeldpunt'  else  begin_end_or_activation end, 
	aps.signal_group_number, array(select nummer from activation_point_signal_vehicle_types apsvt inner join vehicle_type vt on vt.nummer = apsvt.vehicle_types where activation_point_signal = aps.id) as vehicletypes,
	aps.trigger_type,
	ap.label, ap.nummer, ap.id
	from roadside_equipment r
	left join movement m on m.roadside_equipment = r.id

	left join movement_activation_point map on m.id = map.movement
	left join movement_points mp on m.id = m.id and map.id = mp.point
	left join activation_point_signal aps on aps.id = map.signal
	left join activation_point ap on ap.id = map.point
	where (r.id = 1703 or r.id = 4291) and begin_end_or_activation <> 'END' and m.vehicle_type is null


	order by r.id, m.nummer,mp.list_index
	mid opnemen in volgende queries
	*/
delete from movement_points where movement in ('196712','196713')
delete from movement_activation_point where movement in ('196712','196713')
delete from movement where id in ('196712','196713')