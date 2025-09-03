ALTER DATABASE iot_platform SET timezone TO 'UTC';
SELECT pg_reload_conf();

INSERT INTO registry (id, name, uuid)
VALUES (1, 'smart_home', '4d8a3176-c0c1-11ed-afa1-0242ac120002')
    ON CONFLICT DO NOTHING;

INSERT INTO registry (id, name, uuid)
VALUES (2, 'crearts-iot', 'c93af2d0-c0bf-11ed-afa1-0242ac120002')
    ON CONFLICT DO NOTHING;

INSERT INTO device (id, registry_id, name, uuid)
VALUES (1, 2, 'rpi-robot', 'e114cd7c-c0c4-11ed-afa1-0242ac120002')
    ON CONFLICT DO NOTHING;

INSERT INTO device (id, registry_id, name, uuid)
VALUES (2, 2, 'monitor', gen_random_uuid())
    ON CONFLICT DO NOTHING;