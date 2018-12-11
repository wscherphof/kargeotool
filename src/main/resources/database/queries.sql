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
	where r.id = 3901

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

/*update activation_point_signal set trigger_type = 1  where id in 
(select map.signal
from movement m
join movement_activation_point map on map.movement = m .id
where 
m.vehicle_type = 'OV' and kar_command_type = 2 )
*/