-- noinspection SpellCheckingInspectionForFile

-- Create the 'users' table
CREATE TABLE users (
                       username VARCHAR(50) PRIMARY KEY,
                       name VARCHAR(50),
                       surname VARCHAR(50),
                       birthdate DATE,
                       gender CHAR(1),
                       email VARCHAR(100),
                       location VARCHAR(100),
                       password VARCHAR(50),
                       isadmin BOOLEAN
);

-- Insert 1 admin by default so that other users can be added by this admin
INSERT INTO users (username, name, surname, birthdate, gender, email, location, password, isadmin) VALUES
        ('root', 'Root', 'Rootoglu', '1001-01-01', 'M', 'root@admin.com', 'Ankara', 'defaultadmin', TRUE);


-- Create the 'messages' table
CREATE TABLE messages (
                          id SERIAL PRIMARY KEY,
                          sender VARCHAR(50) NOT NULL,
                          receiver VARCHAR(50) NOT NULL,
                          title VARCHAR(100) NOT NULL,
                          content TEXT NOT NULL,
                          timestamp TIMESTAMPTZ NOT NULL
);

