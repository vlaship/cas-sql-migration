-- Create a new keyspace
CREATE KEYSPACE migration WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

-- Use the new keyspace
USE migration;

-- Create a sample table
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       name text,
                       email text
);

-- Add a test user
INSERT INTO users (id, name, email) VALUES (uuid(), 'testuser', 'testuser@example.com');
