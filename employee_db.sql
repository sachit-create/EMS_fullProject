-- ECCS Database Schema
-- Finalized for Deployment

CREATE DATABASE IF NOT EXISTS employee_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

-- Allow employee_app to connect from ANY host (required for Docker)
CREATE USER IF NOT EXISTS 'employee_app'@'%' IDENTIFIED BY 'employee_app_123';
GRANT ALL PRIVILEGES ON employee_db.* TO 'employee_app'@'%';
FLUSH PRIVILEGES;

USE employee_db;

-- Clear old tables to force fresh schema
DROP TABLE IF EXISTS employee_salary;
DROP TABLE IF EXISTS add_emp;
DROP TABLE IF EXISTS app_user;

-- 1. Main Employee Table
CREATE TABLE add_emp (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  employee_code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  father_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) UNIQUE,
  gender ENUM('M', 'F') NOT NULL,
  salary DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  employee_photo LONGBLOB NULL,
  employee_photo_type VARCHAR(100) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Salary History Table (For future expansion)
CREATE TABLE IF NOT EXISTS employee_salary (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  employee_code VARCHAR(20) NOT NULL,
  salary_month DATE NOT NULL,
  basic_salary DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  allowance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  deduction DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  net_salary DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
  note VARCHAR(255) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_employee_salary_code
    FOREIGN KEY (employee_code) REFERENCES add_emp(employee_code)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Application Admin Users
CREATE TABLE IF NOT EXISTS app_user (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  full_name VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- Insert default admin if not exists
INSERT INTO app_user (username, password, full_name)
SELECT 'admin', 'admin123', 'System Admin'
WHERE NOT EXISTS (
  SELECT 1 FROM app_user WHERE username = 'admin'
);
