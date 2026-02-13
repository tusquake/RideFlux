# 🧪 Ride-Sharing Platform — End-to-End Testing Guide

> Test the complete ride lifecycle: Register → Login → Set Driver Location → Book Ride → Accept → Start → Complete → Payment (auto) → Notification (auto) → Rate Driver

## ✅ Services Running

| # | Service | Port | Status |
|---|---------|------|--------|
| 1 | Config Server | 8888 | ✅ UP |
| 2 | Service Registry (Eureka) | 8761 | ✅ UP |
| 3 | API Gateway | 8080 | ✅ UP |
| 4 | User Service | 8081 | ✅ UP |
| 5 | Ride Service | 8082 | ✅ UP |
| 6 | Driver Location Service | 8083 | ✅ UP |
| 7 | Pricing Service | 8084 | ✅ UP |
| 8 | Payment Service | 8085 | ✅ UP |
| 9 | Notification Service | 8086 | ✅ UP |
| 10 | Rating Service | 8087 | ✅ UP |

**Dashboard:** http://localhost:8761 (Eureka)
**RabbitMQ:** http://localhost:15672 (guest/guest)

---

## 🔥 Complete E2E Flow

### Step 1: Register a Rider

```bash
curl -X POST http://localhost:8081/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\": \"Tushar\", \"email\": \"tushar@test.com\", \"password\": \"password123\", \"phone\": \"9876543210\", \"role\": \"RIDER\"}"
```

**Expected:** `201 Created` with user details + JWT token

---

### Step 2: Register a Driver

```bash
curl -X POST http://localhost:8081/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\": \"Rahul Driver\", \"email\": \"rahul@test.com\", \"password\": \"password123\", \"phone\": \"9876543211\", \"role\": \"DRIVER\"}"
```

**Expected:** `201 Created` with driver details + JWT token

> **Note the `id` fields from both responses — you'll need them as `riderId` and `driverId`**

---

### Step 3: Login (Get JWT Token)

```bash
curl -X POST http://localhost:8081/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"tushar@test.com\", \"password\": \"password123\"}"
```

**Expected:** JWT token in response — save it for authenticated requests

---

### Step 4: Set Driver Location (Redis GEO)

```bash
curl -X POST http://localhost:8083/locations/update ^
  -H "Content-Type: application/json" ^
  -d "{\"driverId\": 2, \"latitude\": 28.6139, \"longitude\": 77.2090, \"available\": true}"
```

**Expected:** `200 OK` — Driver's GPS coordinates stored in Redis

---

### Step 5: Find Nearby Drivers

```bash
curl "http://localhost:8083/locations/nearby?latitude=28.6200&longitude=77.2100&radiusInKm=5"
```

**Expected:** List of nearby drivers within 5km radius (should include driver from Step 4)

---

### Step 6: Book a Ride 🚗 (Triggers: Pricing + RabbitMQ Event)

```bash
curl -X POST http://localhost:8082/rides/book ^
  -H "Content-Type: application/json" ^
  -d "{\"riderId\": 1, \"pickupLatitude\": 28.6139, \"pickupLongitude\": 77.2090, \"dropoffLatitude\": 28.5355, \"dropoffLongitude\": 77.3910, \"pickupAddress\": \"Connaught Place, Delhi\", \"dropoffAddress\": \"Noida Sector 62\", \"vehicleType\": \"SEDAN\"}"
```

**Expected:**
- `200 OK` with ride details (id, fare, status=REQUESTED)
- 🔗 **Behind the scenes:** Ride Service called Pricing Service (Circuit Breaker protected)
- 📨 **RabbitMQ:** `RIDE_BOOKED` event published → Notification Service creates notification

> **Save the ride `id` from the response — needed for all subsequent steps!**

---

### Step 7: Check Notification (Auto-generated from RIDE_BOOKED event)

```bash
curl http://localhost:8086/notifications/user/1
```

**Expected:** Notification like "🚗 Ride booked! Looking for a driver near Connaught Place, Delhi..."

---

### Step 8: Accept the Ride (Driver)

```bash
curl -X PUT "http://localhost:8082/rides/1/accept?driverId=2"
```

**Expected:** `200 OK` — status changes to `ACCEPTED`, driverId set

---

### Step 9: Start the Ride

```bash
curl -X PUT http://localhost:8082/rides/1/start
```

**Expected:** `200 OK` — status changes to `IN_PROGRESS`

---

### Step 10: Complete the Ride 🏁 (Triggers: Payment + Notification + Saga)

```bash
curl -X PUT http://localhost:8082/rides/1/complete
```

**Expected:**
- `200 OK` — status changes to `COMPLETED`
- 📨 **RabbitMQ:** `RIDE_COMPLETED` event published
- 💰 **Payment Service** auto-processes payment (check in Step 11)
- 🔔 **Notification Service** creates completion notification
- 🔄 **Saga:** Payment publishes `PAYMENT_SUCCESS` → Ride marks as `PAID`

---

### Step 11: Verify Auto-Payment (Saga Pattern in action!)

```bash
curl http://localhost:8085/payments/ride/1
```

**Expected:**
```json
{
  "id": 1,
  "rideId": 1,
  "amount": 245.50,
  "status": "COMPLETED",
  "transactionId": "TXN-A1B2C3D4",
  "paymentMethod": "UPI"
}
```

**This was triggered automatically by RabbitMQ — no API call needed!**

---

### Step 12: Check All Notifications

**Rider notifications:**
```bash
curl http://localhost:8086/notifications/user/1
```

**Driver notifications:**
```bash
curl http://localhost:8086/notifications/user/2
```

**Expected:** Multiple notifications for both rider and driver (booked, completed)

**Unread count (badge number):**
```bash
curl http://localhost:8086/notifications/user/1/unread-count
```

**Mark all as read:**
```bash
curl -X PUT http://localhost:8086/notifications/user/1/read-all
```

---

### Step 13: Rate the Driver ⭐ (CQRS Pattern)

```bash
curl -X POST http://localhost:8087/ratings ^
  -H "Content-Type: application/json" ^
  -d "{\"rideId\": 1, \"ratedBy\": 1, \"ratedUser\": 2, \"score\": 5, \"review\": \"Excellent ride! Very punctual driver.\"}"
```

**Expected:** `200 OK` — Rating saved to BOTH command model (ratings table) AND query model (driver_rating_summary)

---

### Step 14: Get Driver Rating Summary (CQRS Read Model — O(1))

```bash
curl http://localhost:8087/ratings/driver/2/summary
```

**Expected:**
```json
{
  "driverId": 2,
  "averageRating": 5.0,
  "totalRatings": 1,
  "fiveStarCount": 1,
  "fourStarCount": 0,
  "threeStarCount": 0,
  "twoStarCount": 0,
  "oneStarCount": 0
}
```

---

### Step 15: Submit More Ratings (Watch CQRS in action)

```bash
curl -X POST http://localhost:8087/ratings ^
  -H "Content-Type: application/json" ^
  -d "{\"rideId\": 1, \"ratedBy\": 2, \"ratedUser\": 1, \"score\": 4, \"review\": \"Good passenger, was on time.\"}"
```

Check rider's summary:
```bash
curl http://localhost:8087/ratings/driver/1/summary
```

---

## 🔄 Test Cancellation Flow

### Cancel a Ride (after booking)

First book another ride:
```bash
curl -X POST http://localhost:8082/rides/book ^
  -H "Content-Type: application/json" ^
  -d "{\"riderId\": 1, \"pickupLatitude\": 28.6139, \"pickupLongitude\": 77.2090, \"dropoffLatitude\": 28.7041, \"dropoffLongitude\": 77.1025, \"pickupAddress\": \"CP Delhi\", \"dropoffAddress\": \"Dwarka\", \"vehicleType\": \"AUTO\"}"
```

Then cancel it (use the new ride ID):
```bash
curl -X PUT "http://localhost:8082/rides/2/cancel?reason=Changed my mind"
```

**Expected:**
- Ride status → CANCELLED
- 📨 `RIDE_CANCELLED` event → Notification Service creates cancellation alert
- 💰 Payment Service checks if payment exists → processes refund if needed

---

## 🔍 Verify Design Patterns in Action

### Circuit Breaker: Stop Pricing Service, then book a ride
```bash
# 1. Note the Pricing Service PID
# 2. Stop it (kill the process)
# 3. Book a ride — should get FALLBACK pricing (flat ₹12/km)
curl -X POST http://localhost:8082/rides/book ^
  -H "Content-Type: application/json" ^
  -d "{\"riderId\": 1, \"pickupLatitude\": 28.6139, \"pickupLongitude\": 77.2090, \"dropoffLatitude\": 28.5355, \"dropoffLongitude\": 77.3910, \"vehicleType\": \"SEDAN\"}"
# Expected: Ride booked with estimated fare (fallback), NOT an error!
```

### Event-Driven: Check RabbitMQ Dashboard
Open http://localhost:15672 (guest/guest)
- Go to **Queues** tab
- You'll see: `ride.booked.queue`, `ride.completed.queue`, `ride.cancelled.queue`, `payment.success.queue`, `payment.failed.queue`
- Check **Message rates** — you'll see messages flowing when you perform actions

### Service Discovery: Check Eureka Dashboard
Open http://localhost:8761
- All 8 services should be registered
- Each shows its IP, port, and health status

---

## 🛑 Stopping Everything

```bash
# Stop all Java processes
Get-Process java | Stop-Process -Force

# Stop Docker containers
docker compose down
```

---

## 📋 Quick Reference — All Endpoints

### User Service (:8081)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register user |
| POST | `/auth/login` | Login (JWT) |
| GET | `/users/{id}` | Get user by ID (needs JWT) |
| GET | `/users/email/{email}` | Get user by email (needs JWT) |

### Ride Service (:8082)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/rides/book` | Book a ride |
| PUT | `/rides/{id}/accept?driverId=` | Accept ride |
| PUT | `/rides/{id}/start` | Start ride |
| PUT | `/rides/{id}/complete` | Complete ride |
| PUT | `/rides/{id}/cancel?reason=` | Cancel ride |
| GET | `/rides/{id}` | Get ride details |
| GET | `/rides/rider/{riderId}` | Rider's ride history |
| GET | `/rides/driver/{driverId}` | Driver's ride history |

### Driver Location (:8083)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/locations/update` | Update driver GPS |
| GET | `/locations/nearby?lat=&lng=&radius=` | Find nearby drivers |
| GET | `/locations/{driverId}` | Get driver location |
| DELETE | `/locations/{driverId}` | Remove driver |

### Pricing Service (:8084)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/pricing/calculate` | Calculate fare |
| GET | `/pricing/rules` | Get pricing rules |

### Payment Service (:8085)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/payments/ride/{rideId}` | Get payment for ride |
| GET | `/payments/rider/{riderId}` | Rider's payment history |
| POST | `/payments/refund/{rideId}` | Process refund |

### Notification Service (:8086)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/notifications/user/{userId}` | All notifications |
| GET | `/notifications/user/{userId}/unread` | Unread only |
| GET | `/notifications/user/{userId}/unread-count` | Unread count |
| PUT | `/notifications/{id}/read` | Mark as read |
| PUT | `/notifications/user/{userId}/read-all` | Mark all read |

### Rating Service (:8087)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/ratings` | Submit rating |
| GET | `/ratings/driver/{id}/summary` | Driver summary (CQRS) |
| GET | `/ratings/ride/{rideId}` | Ride ratings |
| GET | `/ratings/user/{userId}` | User's received ratings |
