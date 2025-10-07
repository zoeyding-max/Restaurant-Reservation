# Restaurant Reservation System


## 🎯 Features

### Customer Features
- ✅ **Make Reservations** - Book tables with preferred date, time, and party size
- ✅ **Modify Reservations** - Change date, time, or party size
- ✅ **Cancel Reservations** - Easy cancellation with confirmation
- ✅ **View History** - See past and upcoming reservations
- ✅ **Real-time Availability** - Check available time slots
- ✅ **Special Requests** - Add dietary restrictions or preferences

### Admin Features
- ✅ **Reservation Management** - View, modify, and cancel all reservations
- ✅ **Seating Arrangements** - Manage table layout and assignments
- ✅ **Dashboard Statistics** - Real-time metrics and analytics
- ✅ **Table Status** - Track occupancy and availability
- ✅ **Customer Profiles** - Manage customer information
- ✅ **Reporting** - Generate daily/weekly/monthly reports

### Technical Features
- 📱 **Android Application** - Native mobile app for customers
- 🖥️ **JavaFX GUI** - Desktop admin interface
- 🔌 **RESTful API** - Spring Boot backend
- 🗄️ **MySQL Database** - Optimized queries for high-speed access
- 🔐 **Data Validation** - Prevent double bookings
- 📊 **Real-time Updates** - Instant availability checks
- 🚀 **Scalable Architecture** - Designed for growth



## 🛠️ Installation

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.8+
- Android Studio (for mobile app)
- JavaFX SDK (for desktop GUI)

### Backend Setup

1. **Clone the repository**
```bash
git clone https://github.com/zoeyding-max/restaurant-reservation-system.git
cd restaurant-reservation-system
```

2. **Set up MySQL database**
```bash
mysql -u root -p < database/schema.sql
```

3. **Configure database connection**

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

4. **Build and run**
```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### Android App Setup

1. **Open Android Studio**
2. **Import project** from `android/` directory
3. **Update API URL** in `app/src/main/res/values/strings.xml`:
```xml
<string name="api_base_url">http://your-server-ip:8080/api</string>
```
4. **Build and run** on emulator or device

### JavaFX GUI Setup

1. **Ensure JavaFX SDK is installed**
2. **Run the GUI application**:
```bash
mvn javafx:run
```

## 📋 API Endpoints

### Customer Endpoints

#### Create Reservation
```
POST /api/reservations
```
Request body:
```json
{
  "customerId": 1,
  "reservationTime": "2024-12-25T18:00:00",
  "partySize": 4,
  "specialRequests": "Window seat preferred"
}
```

#### Get Customer Reservations
```
GET /api/customer/{customerId}/reservations
```

#### Modify Reservation
```
PUT /api/reservations/{reservationId}
```

#### Cancel Reservation
```
DELETE /api/reservations/{reservationId}?customerId={customerId}
```

#### Check Availability
```
GET /api/availability?date=2024-12-25&partySize=4
```

### Admin Endpoints

#### Get All Reservations
```
GET /api/admin/reservations?date=2024-12-25&status=CONFIRMED
```

#### Get Statistics
```
GET /api/admin/statistics?startDate=2024-12-01&endDate=2024-12-31
```

#### Get All Tables
```
GET /api/admin/tables
```

#### Update Table Status
```
PUT /api/admin/tables/{tableId}
```

### Customer Management

#### Create Customer
```
POST /api/customers
```

#### Get Customer Profile
```
GET /api/customers/{customerId}
```

## 🗄️ Database Schema

### Tables

**customers**
- customer_id (PK)
- name
- email (UNIQUE)
- phone
- created_at, updated_at

**tables**
- table_id (PK)
- table_number (UNIQUE)
- capacity
- location (INDOOR, OUTDOOR, PATIO, BAR)
- status (AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE)

**reservations**
- reservation_id (PK)
- customer_id (FK)
- table_id (FK)
- reservation_time
- party_size
- status (CONFIRMED, CANCELLED, COMPLETED, NO_SHOW)
- special_requests
- created_at, updated_at

### Optimizations
- **Indexed columns** for fast lookups
- **Composite indexes** on common query patterns
- **Foreign key constraints** for data integrity
- **Triggers** to prevent double booking
- **Stored procedures** for complex operations
- **Views** for common reporting needs

## 🚀 Usage

### Customer Flow

1. **Create Account**
   - Provide name, email, phone

2. **Make Reservation**
   - Select date and time
   - Choose party size
   - Add special requests (optional)
   - Confirm booking

3. **Modify Reservation** (if needed)
   - Change date/time
   - Update party size
   - Modify special requests

4. **Cancel Reservation** (if needed)
   - Select reservation
   - Confirm cancellation

### Admin Flow

1. **View Dashboard**
   - See today's reservations
   - Check table occupancy
   - View statistics

2. **Manage Reservations**
   - View all bookings
   - Modify or cancel as needed
   - Handle walk-ins

3. **Manage Seating**
   - Update table status
   - Assign tables
   - Mark tables for maintenance

4. **Generate Reports**
   - Daily revenue
   - Customer analytics
   - Table utilization


