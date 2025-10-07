package com.restaurant.database;

import com.restaurant.model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Manager for Restaurant Reservation System
 * Handles all MySQL database operations with optimized queries
 */
public class DatabaseManager {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    
    private Connection connection;

    public DatabaseManager() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // RESERVATION OPERATIONS
    // ==========================================

    /**
     * Create a new reservation
     * Optimized query for high-speed insertion
     */
    public int createReservation(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (customer_id, table_id, reservation_time, " +
                    "party_size, status, special_requests, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reservation.getCustomerId());
            stmt.setInt(2, reservation.getTableId());
            stmt.setTimestamp(3, Timestamp.valueOf(reservation.getReservationTime()));
            stmt.setInt(4, reservation.getPartySize());
            stmt.setString(5, reservation.getStatus());
            stmt.setString(6, reservation.getSpecialRequests());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Get reservation by ID
     */
    public Reservation getReservationById(int reservationId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToReservation(rs);
            }
        }
        return null;
    }

    /**
     * Get all reservations for a customer
     */
    public List<Reservation> getReservationsByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE customer_id = ? " +
                    "ORDER BY reservation_time DESC";
        
        List<Reservation> reservations = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    /**
     * Update an existing reservation
     */
    public boolean updateReservation(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservations SET table_id = ?, reservation_time = ?, " +
                    "party_size = ?, special_requests = ?, updated_at = NOW() " +
                    "WHERE reservation_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservation.getTableId());
            stmt.setTimestamp(2, Timestamp.valueOf(reservation.getReservationTime()));
            stmt.setInt(3, reservation.getPartySize());
            stmt.setString(4, reservation.getSpecialRequests());
            stmt.setInt(5, reservation.getReservationId());
            
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Cancel a reservation
     */
    public boolean cancelReservation(int reservationId) throws SQLException {
        String sql = "UPDATE reservations SET status = 'CANCELLED', updated_at = NOW() " +
                    "WHERE reservation_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get all reservations (admin)
     */
    public List<Reservation> getAllReservations() throws SQLException {
        String sql = "SELECT * FROM reservations ORDER BY reservation_time DESC";
        List<Reservation> reservations = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    /**
     * Get reservations by date
     */
    public List<Reservation> getReservationsByDate(String date) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE DATE(reservation_time) = ? " +
                    "ORDER BY reservation_time";
        
        List<Reservation> reservations = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    /**
     * Get reservations by status
     */
    public List<Reservation> getReservationsByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE status = ? " +
                    "ORDER BY reservation_time";
        
        List<Reservation> reservations = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    // ==========================================
    // TABLE OPERATIONS
    // ==========================================

    /**
     * Find available table for given party size and time
     * Optimized query for fast lookups
     */
    public Table findAvailableTable(int partySize, LocalDateTime reservationTime) 
            throws SQLException {
        return findAvailableTable(partySize, reservationTime, -1);
    }

    public Table findAvailableTable(int partySize, LocalDateTime reservationTime, 
            int excludeReservationId) throws SQLException {
        
        // Find tables with sufficient capacity that are not already booked
        // within 2 hours of the requested time
        String sql = "SELECT t.* FROM tables t " +
                    "WHERE t.capacity >= ? AND t.status = 'AVAILABLE' " +
                    "AND t.table_id NOT IN (" +
                    "  SELECT r.table_id FROM reservations r " +
                    "  WHERE r.status = 'CONFIRMED' " +
                    "  AND r.reservation_id != ? " +
                    "  AND ABS(TIMESTAMPDIFF(MINUTE, r.reservation_time, ?)) < 120" +
                    ") " +
                    "ORDER BY t.capacity ASC " +
                    "LIMIT 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, partySize);
            stmt.setInt(2, excludeReservationId);
            stmt.setTimestamp(3, Timestamp.valueOf(reservationTime));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Table(
                    rs.getInt("table_id"),
                    rs.getInt("table_number"),
                    rs.getInt("capacity"),
                    rs.getString("location"),
                    rs.getString("status")
                );
            }
        }
        return null;
    }

    /**
     * Get all tables
     */
    public List<Table> getAllTables() throws SQLException {
        String sql = "SELECT * FROM tables ORDER BY table_number";
        List<Table> tables = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tables.add(new Table(
                    rs.getInt("table_id"),
                    rs.getInt("table_number"),
                    rs.getInt("capacity"),
                    rs.getString("location"),
                    rs.getString("status")
                ));
            }
        }
        return tables;
    }

    /**
     * Update table status
     */
    public boolean updateTableStatus(int tableId, String status) throws SQLException {
        String sql = "UPDATE tables SET status = ? WHERE table_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, tableId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get available time slots
     */
    public List<TimeSlot> getAvailableTimeSlots(String date, int partySize) 
            throws SQLException {
        List<TimeSlot> availableSlots = new ArrayList<>();
        
        // Check availability for each hour from 9 AM to 9 PM
        for (int hour = 9; hour <= 21; hour++) {
            String timeString = String.format("%s %02d:00:00", date, hour);
            LocalDateTime slotTime = LocalDateTime.parse(timeString, 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            Table table = findAvailableTable(partySize, slotTime);
            
            if (table != null) {
                availableSlots.add(new TimeSlot(
                    slotTime,
                    true,
                    table.getTableNumber()
                ));
            } else {
                availableSlots.add(new TimeSlot(
                    slotTime,
                    false,
                    -1
                ));
            }
        }
        
        return availableSlots;
    }

    // ==========================================
    // CUSTOMER OPERATIONS
    // ==========================================

    /**
     * Create a new customer
     */
    public int createCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, email, phone, created_at) " +
                    "VALUES (?, ?, ?, NOW())";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Get customer by ID
     */
    public Customer getCustomerById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                );
            }
        }
        return null;
    }

    // ==========================================
    // STATISTICS
    // ==========================================

    /**
     * Get restaurant statistics
     */
    public RestaurantStatistics getStatistics(String startDate, String endDate) 
            throws SQLException {
        RestaurantStatistics stats = new RestaurantStatistics();
        
        // Total reservations
        String sql1 = "SELECT COUNT(*) as total FROM reservations";
        if (startDate != null && endDate != null) {
            sql1 += " WHERE DATE(reservation_time) BETWEEN ? AND ?";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql1)) {
            if (startDate != null && endDate != null) {
                stmt.setString(1, startDate);
                stmt.setString(2, endDate);
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setTotalReservations(rs.getInt("total"));
            }
        }
        
        // Active reservations
        String sql2 = "SELECT COUNT(*) as active FROM reservations " +
                     "WHERE status = 'CONFIRMED' AND reservation_time > NOW()";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql2)) {
            if (rs.next()) {
                stats.setActiveReservations(rs.getInt("active"));
            }
        }
        
        // Average party size
        String sql3 = "SELECT AVG(party_size) as avg_size FROM reservations " +
                     "WHERE status = 'CONFIRMED'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql3)) {
            if (rs.next()) {
                stats.setAveragePartySize(rs.getDouble("avg_size"));
            }
        }
        
        // Table utilization
        String sql4 = "SELECT " +
                     "(SELECT COUNT(*) FROM reservations WHERE status = 'CONFIRMED' " +
                     "AND DATE(reservation_time) = CURDATE()) * 100.0 / " +
                     "(SELECT COUNT(*) * 13 FROM tables) as utilization";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql4)) {
            if (rs.next()) {
                stats.setTableUtilization(rs.getDouble("utilization"));
            }
        }
        
        return stats;
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
            rs.getInt("reservation_id"),
            rs.getInt("customer_id"),
            rs.getInt("table_id"),
            rs.getTimestamp("reservation_time").toLocalDateTime(),
            rs.getInt("party_size"),
            rs.getString("status"),
            rs.getString("special_requests")
        );
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}