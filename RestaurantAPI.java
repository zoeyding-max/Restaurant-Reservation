package com.restaurant.api;

import com.restaurant.model.*;
import com.restaurant.database.DatabaseManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Restaurant Reservation System - RESTful API
 * Author: Zoey (Zhijia) Ding
 * 
 * Spring Boot application providing RESTful APIs for:
 * - Customer reservation management
 * - Restaurant seating arrangements
 * - Admin functionalities
 */

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RestaurantAPI {

    private DatabaseManager dbManager;

    public RestaurantAPI() {
        this.dbManager = new DatabaseManager();
    }

    public static void main(String[] args) {
        SpringApplication.run(RestaurantAPI.class, args);
    }

    // ==========================================
    // CUSTOMER ENDPOINTS
    // ==========================================

    /**
     * Get all reservations for a customer
     */
    @GetMapping("/customer/{customerId}/reservations")
    public ResponseEntity<List<Reservation>> getCustomerReservations(
            @PathVariable int customerId) {
        try {
            List<Reservation> reservations = dbManager.getReservationsByCustomerId(customerId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new reservation
     */
    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody ReservationRequest request) {
        try {
            // Validate request
            if (!isValidReservation(request)) {
                return ResponseEntity.badRequest()
                    .body(new ReservationResponse(false, "Invalid reservation details", null));
            }

            // Check table availability
            Table availableTable = dbManager.findAvailableTable(
                request.getPartySize(), 
                request.getReservationTime()
            );

            if (availableTable == null) {
                return ResponseEntity.ok()
                    .body(new ReservationResponse(false, "No tables available", null));
            }

            // Create reservation
            Reservation reservation = new Reservation(
                0, // ID will be auto-generated
                request.getCustomerId(),
                availableTable.getTableId(),
                request.getReservationTime(),
                request.getPartySize(),
                "CONFIRMED",
                request.getSpecialRequests()
            );

            int reservationId = dbManager.createReservation(reservation);
            reservation.setReservationId(reservationId);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ReservationResponse(true, "Reservation created successfully", reservation));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ReservationResponse(false, "Server error: " + e.getMessage(), null));
        }
    }

    /**
     * Modify an existing reservation
     */
    @PutMapping("/reservations/{reservationId}")
    public ResponseEntity<ReservationResponse> modifyReservation(
            @PathVariable int reservationId,
            @RequestBody ReservationRequest request) {
        try {
            // Get existing reservation
            Reservation existing = dbManager.getReservationById(reservationId);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ReservationResponse(false, "Reservation not found", null));
            }

            // Check if customer owns this reservation
            if (existing.getCustomerId() != request.getCustomerId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ReservationResponse(false, "Unauthorized", null));
            }

            // Find new table if party size or time changed
            Table newTable = dbManager.findAvailableTable(
                request.getPartySize(),
                request.getReservationTime(),
                reservationId // Exclude current reservation
            );

            if (newTable == null) {
                return ResponseEntity.ok()
                    .body(new ReservationResponse(false, "No tables available for requested time", null));
            }

            // Update reservation
            existing.setTableId(newTable.getTableId());
            existing.setReservationTime(request.getReservationTime());
            existing.setPartySize(request.getPartySize());
            existing.setSpecialRequests(request.getSpecialRequests());

            boolean updated = dbManager.updateReservation(existing);

            if (updated) {
                return ResponseEntity.ok()
                    .body(new ReservationResponse(true, "Reservation updated successfully", existing));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReservationResponse(false, "Failed to update reservation", null));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ReservationResponse(false, "Server error: " + e.getMessage(), null));
        }
    }

    /**
     * Cancel a reservation
     */
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable int reservationId,
            @RequestParam int customerId) {
        try {
            // Verify ownership
            Reservation reservation = dbManager.getReservationById(reservationId);
            if (reservation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ReservationResponse(false, "Reservation not found", null));
            }

            if (reservation.getCustomerId() != customerId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ReservationResponse(false, "Unauthorized", null));
            }

            // Cancel reservation
            boolean cancelled = dbManager.cancelReservation(reservationId);

            if (cancelled) {
                return ResponseEntity.ok()
                    .body(new ReservationResponse(true, "Reservation cancelled successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReservationResponse(false, "Failed to cancel reservation", null));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ReservationResponse(false, "Server error: " + e.getMessage(), null));
        }
    }

    /**
     * Check table availability
     */
    @GetMapping("/availability")
    public ResponseEntity<List<TimeSlot>> checkAvailability(
            @RequestParam String date,
            @RequestParam int partySize) {
        try {
            List<TimeSlot> availableSlots = dbManager.getAvailableTimeSlots(date, partySize);
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // ADMIN ENDPOINTS
    // ==========================================

    /**
     * Get all reservations for admin view
     */
    @GetMapping("/admin/reservations")
    public ResponseEntity<List<Reservation>> getAllReservations(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String status) {
        try {
            List<Reservation> reservations;
            
            if (date != null) {
                reservations = dbManager.getReservationsByDate(date);
            } else if (status != null) {
                reservations = dbManager.getReservationsByStatus(status);
            } else {
                reservations = dbManager.getAllReservations();
            }

            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get restaurant statistics
     */
    @GetMapping("/admin/statistics")
    public ResponseEntity<RestaurantStatistics> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            RestaurantStatistics stats = dbManager.getStatistics(startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Manage seating arrangements
     */
    @GetMapping("/admin/tables")
    public ResponseEntity<List<Table>> getAllTables() {
        try {
            List<Table> tables = dbManager.getAllTables();
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update table status
     */
    @PutMapping("/admin/tables/{tableId}")
    public ResponseEntity<String> updateTableStatus(
            @PathVariable int tableId,
            @RequestBody TableStatusRequest request) {
        try {
            boolean updated = dbManager.updateTableStatus(tableId, request.getStatus());
            
            if (updated) {
                return ResponseEntity.ok("Table status updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Table not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Server error: " + e.getMessage());
        }
    }

    /**
     * Create new customer profile
     */
    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        try {
            int customerId = dbManager.createCustomer(customer);
            customer.setCustomerId(customerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(customer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get customer profile
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable int customerId) {
        try {
            Customer customer = dbManager.getCustomerById(customerId);
            
            if (customer != null) {
                return ResponseEntity.ok(customer);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================

    private boolean isValidReservation(ReservationRequest request) {
        if (request.getCustomerId() <= 0) return false;
        if (request.getPartySize() <= 0 || request.getPartySize() > 20) return false;
        if (request.getReservationTime() == null) return false;
        
        // Check if reservation is in the future
        LocalDateTime now = LocalDateTime.now();
        if (request.getReservationTime().isBefore(now)) return false;
        
        // Check business hours (9 AM - 10 PM)
        int hour = request.getReservationTime().getHour();
        if (hour < 9 || hour > 22) return false;
        
        return true;
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Restaurant API is running");
    }
}