CREATE DATABASE IF NOT EXISTS assignment5;
USE assignment5;
CREATE TABLE IF NOT EXISTS demo_users(id VARCHAR(80) PRIMARY KEY,pwd VARCHAR(80) NOT NULL);
DELETE FROM demo_users;
INSERT INTO demo_users(id,pwd) VALUES('student1','correct123');
-- Local classroom demonstration only. Never store plaintext passwords in a real application.
