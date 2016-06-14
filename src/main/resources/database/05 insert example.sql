create function find_rseq2_id(rseq_kar_address integer) returns bigint 
as 'select id from roadside_equipment2 where kar_address = $1'
language sql immutable;

create function find_mvmt_id(rseq_kar_address integer,nummer integer) returns bigint 
as 'select id from movement where nummer = $2 and roadside_equipment = find_rseq2_id($1)'
language sql immutable;

create function find_ap2_id(rseq_kar_address integer,label varchar) returns bigint 
as 'select id from activation_point2 where label = $2 and roadside_equipment = find_rseq2_id($1)'
language sql immutable;

insert into roadside_equipment2(crossing_code, description, kar_address, town, "type", location, valid_from, data_owner) values
  ('VB123', 'Kruispunt Amsterdamsestraatweg / Marnixlaan', 9999, 'Utrecht', 'CROSSING', geomfromtext('POINT(134790 457481)',28992),'2012-01-16', 'B3P');

insert into roadside_equipment2_kar_attributes(roadside_equipment2,command_type,service_type,used_attributes_mask,list_index) values
 (find_rseq2_id(9999),1,'PT',1+2+4+32+64,0),
 (find_rseq2_id(9999),2,'PT',1+64,1);

insert into activation_point2(label, nummer, location, roadside_equipment) values
  ('A',   1, geomfromtext('POINT(134844 457431)',28992),find_rseq2_id(9999)),
  ('B',   2, geomfromtext('POINT(134750 457439)',28992),find_rseq2_id(9999)),
  ('C',   3, geomfromtext('POINT(134753 457515)',28992),find_rseq2_id(9999)),
  ('D',   4, geomfromtext('POINT(134850 457557)',28992),find_rseq2_id(9999)),
  ('K1', 10, geomfromtext('POINT(134802 457471)',28992),find_rseq2_id(9999)),
  ('K2', 11, geomfromtext('POINT(134803 457476)',28992),find_rseq2_id(9999)),
  ('L',  12, geomfromtext('POINT(134781 457469)',28992),find_rseq2_id(9999)),
  ('M7', 13, geomfromtext('POINT(134773.1 457490.1)',28992),find_rseq2_id(9999)),
  ('M8', 14, geomfromtext('POINT(134775.2 457492.0)',28992),find_rseq2_id(9999)),
  ('N10',15, geomfromtext('POINT(134800.3 457499.3)',28992),find_rseq2_id(9999)),
  ('N11',16, geomfromtext('POINT(134793.8 457502.5)',28992),find_rseq2_id(9999)),
  ('W',  20, geomfromtext('POINT(134855.1 457417.6)',28992),find_rseq2_id(9999)),
  ('X',  21, geomfromtext('POINT(134747.4 457447.9)',28992),find_rseq2_id(9999)),
  ('Y',  22, geomfromtext('POINT(134743.8 457528.5)',28992),find_rseq2_id(9999)),
  ('Z',  23, geomfromtext('POINT(134832.4 457523.7)',28992),find_rseq2_id(9999));
  
insert into movement(nummer, roadside_equipment) select
  unnest(ARRAY[1,2,3,4,5,6,7,8,9,10,11,12]), (find_rseq2_id(9999));
  
-- XXX list_index aparte tabel

insert into movement_activation_point(begin_end_or_activation, movement, point,list_index) values
  ('ACTIVATION',find_mvmt_id(9999,1),find_ap2_id(9999,'A'),0),
  ('ACTIVATION',find_mvmt_id(9999,1),find_ap2_id(9999,'K2'),1),
  (       'END',find_mvmt_id(9999,1),find_ap2_id(9999,'Z'),2),
  ('ACTIVATION',find_mvmt_id(9999,2),find_ap2_id(9999,'A'),0),
  ('ACTIVATION',find_mvmt_id(9999,2),find_ap2_id(9999,'K2'),1),
  (       'END',find_mvmt_id(9999,2),find_ap2_id(9999,'Y'),2),
  ('ACTIVATION',find_mvmt_id(9999,3),find_ap2_id(9999,'A'),0),
  ('ACTIVATION',find_mvmt_id(9999,3),find_ap2_id(9999,'K1'),1),
  (       'END',find_mvmt_id(9999,3),find_ap2_id(9999,'X'),2);
  
insert into activation_point_signal(distance_till_stop_line,kar_command_type,trigger_type,signal_group_number) values
  (200,1,'STANDARD',1);
update movement_activation_point set signal = lastval() where movement = find_mvmt_id(9999,1) and point = find_ap2_id(9999,'A');
insert into activation_point_signal(distance_till_stop_line,kar_command_type,trigger_type,signal_group_number) values
  (-5,2,'STANDARD',1);
update movement_activation_point set signal = lastval() where movement = find_mvmt_id(9999,1) and point = find_ap2_id(9999,'K2');
insert into activation_point_signal(distance_till_stop_line,kar_command_type,trigger_type,signal_group_number) values
  (200,1,'STANDARD',1);
update movement_activation_point set signal = lastval() where movement = find_mvmt_id(9999,2) and point = find_ap2_id(9999,'A');
insert into activation_point_signal(distance_till_stop_line,kar_command_type,trigger_type,signal_group_number) values
  (-5,2,'STANDARD',1);
update movement_activation_point set signal = lastval() where movement = find_mvmt_id(9999,2) and point = find_ap2_id(9999,'K2');
insert into activation_point_signal(distance_till_stop_line,kar_command_type,trigger_type,signal_group_number) values
  (200,1,'STANDARD',2);
update movement_activation_point set signal = lastval() where movement = find_mvmt_id(9999,3) and point = find_ap2_id(9999,'A');
insert into activation_point_signal(distance_till_stop_line,kar_command_type,trigger_type,signal_group_number) values
  (-5,2,'STANDARD',2);
update movement_activation_point set signal = lastval() where movement = find_mvmt_id(9999,3) and point = find_ap2_id(9999,'K1');

insert into activation_point_signal_vehicle_types select id, 1 from activation_point_signal;
