alter table activation_group add column
	leave_announcement bool not null default false;
	
update activation_group set leave_announcement = coalesce(meters_after_stop_line = 0, false);


alter table activation_group drop column meters_after_stop_line;