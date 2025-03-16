-- V1__initial_schema.sql
CREATE TABLE rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    capacity INT,
    availability BOOLEAN DEFAULT true
);
