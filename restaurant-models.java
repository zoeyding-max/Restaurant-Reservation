package com.restaurant.model;

import java.time.LocalDateTime;


public class Reservation {
    private int reservationId;
    private int customerId;
    private int tableId;
    private LocalDateTime reservationTime;
    private int partySize;
    private String status; // CONFIRMED, CANCELLED, COMPLETED
    private String specialRequests;

    public Reservation() {}

    public Reservation(int reservationId, int customerId, int tableId, 
                      LocalDateTime reservationTime, int partySize, 
                      String status, String specialRequests) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.tableId = tableId;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.status = status;
        this.specialRequests = specialRequests;
    }

    // Getters and Setters
    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public LocalDateTime getReservationTime() { return reservationTime; }
    public void setReservationTime(LocalDateTime reservationTime) { 
        this.reservationTime = reservationTime; 
    }

    public int getPartySize() { return partySize; }
    public void setPartySize(int partySize) { this.partySize = partySize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { 
        this.specialRequests = specialRequests; 
    }
}

// ==========================================
// CUSTOMER MODEL
// ==========================================

public class Customer {
    private int customerId;
    private String name;
    private String email;
    private String phone;

    public Customer() {}

    public Customer(int customerId, String name, String email, String phone) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

// ==========================================
// TABLE MODEL
// ==========================================

public class Table {
    private int tableId;
    private int tableNumber;
    private int capacity;
    private String location; // INDOOR, OUTDOOR, PATIO, BAR
    private String status; // AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE

    public Table() {}

    public Table(int tableId, int tableNumber, int capacity, 
                String location, String status) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.location = location;
        this.status = status;
    }

    // Getters and Setters
    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

// ==========================================
// REQUEST/RESPONSE MODELS
// ==========================================

public class ReservationRequest {
    private int customerId;
    private LocalDateTime reservationTime;
    private int partySize;
    private String specialRequests;

    public ReservationRequest() {}

    // Getters and Setters
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public LocalDateTime getReservationTime() { return reservationTime; }
    public void setReservationTime(LocalDateTime reservationTime) { 
        this.reservationTime = reservationTime; 
    }

    public int getPartySize() { return partySize; }
    public void setPartySize(int partySize) { this.partySize = partySize; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { 
        this.specialRequests = specialRequests; 
    }
}

public class ReservationResponse {
    private boolean success;
    private String message;
    private Reservation reservation;

    public ReservationResponse(boolean success, String message, Reservation reservation) {
        this.success = success;
        this.message = message;
        this.reservation = reservation;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }
}

public class TableStatusRequest {
    private String status;

    public TableStatusRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

public class TimeSlot {
    private LocalDateTime time;
    private boolean available;
    private int tableNumber;

    public TimeSlot(LocalDateTime time, boolean available, int tableNumber) {
        this.time = time;
        this.available = available;
        this.tableNumber = tableNumber;
    }

    // Getters and Setters
    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
}

public class RestaurantStatistics {
    private int totalReservations;
    private int activeReservations;
    private double averagePartySize;
    private double tableUtilization;

    public RestaurantStatistics() {}

    // Getters and Setters
    public int getTotalReservations() { return totalReservations; }
    public void setTotalReservations(int totalReservations) { 
        this.totalReservations = totalReservations; 
    }

    public int getActiveReservations() { return activeReservations; }
    public void setActiveReservations(int activeReservations) { 
        this.activeReservations = activeReservations; 
    }

    public double getAveragePartySize() { return averagePartySize; }
    public void setAveragePartySize(double averagePartySize) { 
        this.averagePartySize = averagePartySize; 
    }

    public double getTableUtilization() { return tableUtilization; }
    public void setTableUtilization(double tableUtilization) { 
        this.tableUtilization = tableUtilization; 
    }
}