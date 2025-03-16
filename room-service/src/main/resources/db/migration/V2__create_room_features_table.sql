-- V2__create_room_features_table.sql
CREATE TABLE room_features (
    room_id INT NOT NULL,
    feature VARCHAR(255),
    PRIMARY KEY (room_id, feature),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);
