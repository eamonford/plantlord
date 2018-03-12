CREATE TABLE sensors(
	id SERIAL NOT NULL,
    device_id TEXT,
    name TEXT,
    plant_id INTEGER,
	type TEXT,
    PRIMARY KEY(id)
);
