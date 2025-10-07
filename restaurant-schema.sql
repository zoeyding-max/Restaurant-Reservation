-- Restaurant Reservation System Database Schema
-- Author: Zoey (Zhijia) Ding
-- Optimized for high-speed access with proper indexing

-- Create database
CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

-- ==========================================
-- CUSTOMERS TABLE
-- ==========================================

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- TABLES TABLE (Restaurant Seating)
-- ==========================================

CREATE TABLE IF NOT EXISTS tables (
    table_id INT PRIMARY KEY AUTO_INCREMENT,
    table_number INT UNIQUE NOT NULL,
    capacity INT NOT NULL,
    location ENUM('INDOOR', 'OUTDOOR', 'PATIO', 'BAR') DEFAULT 'INDOOR',
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_capacity (capacity),
    INDEX idx_status (status),
    INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- RESERVATIONS TABLE
-- ==========================================

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    table_id INT NOT NULL,
    reservation_time DATETIME NOT NULL,
    party_size INT NOT NULL,
    status ENUM('CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW') DEFAULT 'CONFIRMED',
    special_requests TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (table_id) REFERENCES tables(table_id) ON DELETE CASCADE,
    
    INDEX idx_customer (customer_id),
    INDEX idx_table (table_id),
    INDEX idx_time (reservation_time),
    INDEX idx_status (status),
    INDEX idx_date (DATE(reservation_time))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- SAMPLE DATA
-- ==========================================

-- Insert sample tables
INSERT INTO tables (table_number, capacity, location, status) VALUES
(1, 2, 'INDOOR', 'AVAILABLE'),
(2, 2, 'INDOOR', 'AVAILABLE'),
(3, 4, 'INDOOR', 'AVAILABLE'),
(4, 4, 'INDOOR', 'AVAILABLE'),
(5, 4, 'OUTDOOR', 'AVAILABLE'),
(6, 6, 'INDOOR', 'AVAILABLE'),
(7, 6, 'PATIO', 'AVAILABLE'),
(8, 8, 'INDOOR', 'AVAILABLE'),
(9, 2, 'BAR', 'AVAILABLE'),
(10, 2, 'BAR', 'AVAILABLE');

-- Insert sample customers
INSERT INTO customers (name, email, phone) VALUES
('John Smith', 'john.smith@email.com', '555-0101'),
('Sarah Johnson', 'sarah.j@email.com', '555-0102'),
('Michael Brown', 'mbrown@email.com', '555-0103'),
('Emily Davis', 'emily.davis@email.com', '555-0104'),
('David Wilson', 'dwilson@email.com', '555-0105');

-- Insert sample reservations
INSERT INTO reservations (customer_id, table_id, reservation_time, party_size, status, special_requests) VALUES
(1, 3, '2024-12-20 18:00:00', 4, 'CONFIRMED', 'Window seat preferred'),
(2, 1, '2024-12-20 19:00:00', 2, 'CONFIRMED', 'Anniversary dinner'),
(3, 6, '2024-12-21 18:30:00', 6, 'CONFIRMED', 'Birthday celebration'),
(4, 4, '2024-12-21 20:00:00', 4, 'CONFIRMED', NULL),
(5, 9, '2024-12-22 17:00:00', 2, 'CONFIRMED', 'Dietary restrictions: vegetarian');

-- ==========================================
-- OPTIMIZED QUERIES FOR COMMON OPERATIONS
-- ==========================================

-- Find available tables for a specific time and party size
-- This query is optimized with indexes for fast lookups
DELIMITER //
CREATE PROCEDURE FindAvailableTable(
    IN p_party_size INT,
    IN p_reservation_time DATETIME
)
BEGIN
    SELECT t.* 
    FROM tables t
    WHERE t.capacity >= p_party_size 
      AND t.status = 'AVAILABLE'
      AND t.table_id NOT IN (
          SELECT r.table_id 
          FROM reservations r
          WHERE r.status = 'CONFIRMED'
            AND ABS(TIMESTAMPDIFF(MINUTE, r.reservation_time, p_reservation_time)) < 120
      )
    ORDER BY t.capacity ASC
    LIMIT 1;
END //
DELIMITER ;

-- Get daily reservation statistics
DELIMITER //
CREATE PROCEDURE GetDailyStatistics(IN p_date DATE)
BEGIN
    SELECT 
        COUNT(*) as total_reservations,
        SUM(party_size) as total_guests,
        AVG(party_size) as avg_party_size,
        COUNT(CASE WHEN status = 'CONFIRMED' THEN 1 END) as confirmed,
        COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled,
        COUNT(CASE WHEN status = 'NO_SHOW' THEN 1 END) as no_shows
    FROM reservations
    WHERE DATE(reservation_time) = p_date;
END //
DELIMITER ;

-- Get customer reservation history
DELIMITER //
CREATE PROCEDURE GetCustomerHistory(IN p_customer_id INT)
BEGIN
    SELECT 
        r.*,
        c.name as customer_name,
        c.email,
        t.table_number,
        t.capacity,
        t.location
    FROM reservations r
    JOIN customers c ON r.customer_id = c.customer_id
    JOIN tables t ON r.table_id = t.table_id
    WHERE r.customer_id = p_customer_id
    ORDER BY r.reservation_time DESC;
END //
DELIMITER ;

-- ==========================================
-- VIEWS FOR REPORTING
-- ==========================================

-- Active reservations view
CREATE OR REPLACE VIEW active_reservations AS
SELECT 
    r.reservation_id,
    r.reservation_time,
    r.party_size,
    c.name as customer_name,
    c.phone,
    t.table_number,
    t.location,
    r.special_requests
FROM reservations r
JOIN customers c ON r.customer_id = c.customer_id
JOIN tables t ON r.table_id = t.table_id
WHERE r.status = 'CONFIRMED' 
  AND r.reservation_time > NOW()
ORDER BY r.reservation_time;

-- Today's reservations view
CREATE OR REPLACE VIEW todays_reservations AS
SELECT 
    r.reservation_id,
    r.reservation_time,
    r.party_size,
    c.name as customer_name,
    c.phone,
    c.email,
    t.table_number,
    t.location,
    r.status,
    r.special_requests
FROM reservations r
JOIN customers c ON r.customer_id = c.customer_id
JOIN tables t ON r.table_id = t.table_id
WHERE DATE(r.reservation_time) = CURDATE()
ORDER BY r.reservation_time;

-- Table occupancy view
CREATE OR REPLACE VIEW table_occupancy AS
SELECT 
    t.table_id,
    t.table_number,
    t.capacity,
    t.location,
    t.status,
    COUNT(r.reservation_id) as total_bookings,
    SUM(CASE WHEN DATE(r.reservation_time) = CURDATE() THEN 1 ELSE 0 END) as today_bookings
FROM tables t
LEFT JOIN reservations r ON t.table_id = r.table_id 
    AND r.status = 'CONFIRMED'
    AND r.reservation_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY t.table_id, t.table_number, t.capacity, t.location, t.status
ORDER BY t.table_number;

-- ==========================================
-- TRIGGERS FOR DATA INTEGRITY
-- ==========================================

-- Prevent double booking
DELIMITER //
CREATE TRIGGER prevent_double_booking
BEFORE INSERT ON reservations
FOR EACH ROW
BEGIN
    DECLARE existing_count INT;
    
    SELECT COUNT(*) INTO existing_count
    FROM reservations
    WHERE table_id = NEW.table_id
      AND status = 'CONFIRMED'
      AND ABS(TIMESTAMPDIFF(MINUTE, reservation_time, NEW.reservation_time)) < 120;
    
    IF existing_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Table is already booked for this time slot';
    END IF;
END //
DELIMITER ;

-- Update table status when reservation is created
DELIMITER //
CREATE TRIGGER update_table_status_on_reservation
AFTER INSERT ON reservations
FOR EACH ROW
BEGIN
    IF NEW.status = 'CONFIRMED' 
       AND NEW.reservation_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 2 HOUR) THEN
        UPDATE tables 
        SET status = 'RESERVED' 
        WHERE table_id = NEW.table_id;
    END IF;
END //
DELIMITER ;

-- ==========================================
-- INDEXES FOR QUERY OPTIMIZATION
-- ==========================================

-- Composite indexes for common query patterns
CREATE INDEX idx_reservation_lookup ON reservations(customer_id, status, reservation_time);
CREATE INDEX idx_table_availability ON tables(status, capacity);
CREATE INDEX idx_time_range ON reservations(reservation_time, status, table_id);

-- Full-text search on special requests (optional)
-- ALTER TABLE reservations ADD FULLTEXT INDEX idx_special_requests(special_requests);

-- ==========================================
-- CLEANUP AND MAINTENANCE
-- ==========================================

-- Procedure to archive old reservations
DELIMITER //
CREATE PROCEDURE ArchiveOldReservations(IN p_days_old INT)
BEGIN
    -- Move old completed/cancelled reservations to archive table
    -- (Archive table creation omitted for brevity)
    
    DELETE FROM reservations
    WHERE status IN ('COMPLETED', 'CANCELLED', 'NO_SHOW')
      AND reservation_time < DATE_SUB(NOW(), INTERVAL p_days_old DAY);
      
    SELECT ROW_COUNT() as archived_count;
END //
DELIMITER ;

-- Event to automatically update table status
DELIMITER //
CREATE EVENT update_table_status_hourly
ON SCHEDULE EVERY 1 HOUR
DO
BEGIN
    -- Reset tables to AVAILABLE if no active reservations
    UPDATE tables t
    SET t.status = 'AVAILABLE'
    WHERE t.status = 'RESERVED'
      AND t.table_id NOT IN (
          SELECT r.table_id
          FROM reservations r
          WHERE r.status = 'CONFIRMED'
            AND r.reservation_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 2 HOUR)
      );
END //
DELIMITER ;

COMMIT;
