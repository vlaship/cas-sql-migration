-- Create a new database
CREATE DATABASE migration;

-- Use the new database
USE migration;

-- Create a new user
CREATE LOGIN my_user WITH PASSWORD = 'YourUserPassword123';
CREATE USER my_user FOR LOGIN my_user;
ALTER ROLE db_owner ADD MEMBER my_user;

-- Create a sample table
CREATE TABLE users (
                       id INT PRIMARY KEY,
                       name NVARCHAR(50),
                       email NVARCHAR(50)
);

-- Add a test user
INSERT INTO users (id, name, email) VALUES (1, 'testuser', 'testuser@example.com');
