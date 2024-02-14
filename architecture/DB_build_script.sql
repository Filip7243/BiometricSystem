-- Drop DB if exists
DROP DATABASE IF EXISTS biometrics;

-- Create DB
CREATE DATABASE IF NOT EXISTS biometrics;
USE biometrics;

-- Create Room table
CREATE TABLE IF NOT EXISTS Room (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255)
);

-- Create Employee table
CREATE TABLE IF NOT EXISTS Employee (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    pesel VARCHAR(11)
);

-- Create Employee_Room (transfer table m-t-m)
CREATE TABLE IF NOT EXISTS Employee_Room (
    employee_id INT,
    room_id INT,
    PRIMARY KEY (employee_id, room_id),
    FOREIGN KEY (employee_id) REFERENCES Employee(id),
    FOREIGN KEY (room_id) REFERENCES Room(id)
);

-- Create Finger table
CREATE TABLE IF NOT EXISTS Finger (
    id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT,
    thumb BLOB NULL,
    pointing BLOB NULL,
    middle BLOB NULL,
    ring BLOB NULL,
    FOREIGN KEY (employee_id) REFERENCES Employee(id)
);

-- Add default Room data
INSERT INTO Room (name) VALUES
('101'),
('202'),
('303'),
('404'),
('505');