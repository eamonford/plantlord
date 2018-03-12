 CREATE TABLE rules(
	id SERIAL NOT NULL,
    sensor_id TEXT,
    type TEXT,
    threshold INT,
    PRIMARY KEY(id)
);
