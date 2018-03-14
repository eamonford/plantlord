CREATE TABLE plants(
	id SERIAL NOT NULL,
    name TEXT,
    PRIMARY KEY(id)
);

CREATE TABLE valves(
	id SERIAL NOT NULL,
    hardware_id INT NOT NULL UNIQUE,
    plant_id INT NOT NULL REFERENCES plants(id)
);

ALTER TABLE sensors ADD CONSTRAINT fk_sensor_plant FOREIGN KEY (plant_id) REFERENCES plants(id);
ALTER TABLE sensors ALTER COLUMN plant_id SET NOT NULL;