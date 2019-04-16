-- Index: aps_vehicletype

 DROP INDEX data.aps_vehicletype;

CREATE INDEX aps_vehicletype
    ON data.activation_point_signal_vehicle_types USING btree
    (vehicle_types)
    TABLESPACE pg_default;

-- Index: rseq_ap

-- DROP INDEX data.rseq_ap;

CREATE INDEX rseq_ap
    ON data.activation_point USING btree
    (roadside_equipment)
    TABLESPACE pg_default;
-- Index: aps_vehicletype

-- DROP INDEX data.aps_vehicletype;

CREATE INDEX aps_aps
    ON data.activation_point_signal_vehicle_types USING btree
    (activation_point_signal)
    TABLESPACE pg_default;
-- Index: map_movement

 DROP INDEX data.map_movement;

CREATE INDEX map_movement
    ON data.movement_activation_point USING btree
    (movement)
    TABLESPACE pg_default;
-- Index: map_point

 DROP INDEX data.map_point;

CREATE INDEX map_point
    ON data.movement_activation_point USING btree
    (point)
    TABLESPACE pg_default;
-- Index: map_signal

 DROP INDEX data.map_signal;

CREATE INDEX map_signal
    ON data.movement_activation_point USING btree
    (signal)
    TABLESPACE pg_default;
