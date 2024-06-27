-- noinspection SqlNoDataSourceInspectionForFile

-- noinspection SpellCheckingInspectionForFile

-- Create the 'users' table
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    name VARCHAR(50),
    surname VARCHAR(50),
    birthdate DATE,
    gender CHAR(1),
    email VARCHAR(100),
    location VARCHAR(100),
    password VARCHAR(100),
    isAdmin BOOLEAN
);

CREATE TABLE IF NOT EXISTS messages (
    id SERIAL PRIMARY KEY,
    sender VARCHAR(50) REFERENCES users(username) ON DELETE SET NULL,
    receiver VARCHAR(50) REFERENCES users(username) ON DELETE SET NULL,
    title VARCHAR(100),
    content TEXT,
    timestamp TIMESTAMP
);

-- Insert 1 admin by default so that other users can be added by this admin
INSERT INTO users (username, name, surname, birthdate, gender, email, location, password, isadmin) VALUES
        ('root', 'Root', 'Rootoglu', '1001-01-01', 'M', 'root@admin.com', 'Ankara', 'defaultadmin', TRUE);