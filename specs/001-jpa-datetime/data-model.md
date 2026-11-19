# Data Model: JPA Date & Time Types Persistence

**Feature Directory**: `specs/001-jpa-datetime`  
**Date**: 2026-10-09  
**Status**: Completed  

---

## 1. Entity: `TimeRecord`

Represents a test record containing all modern Java 8+ Date/Time types to compare their persistence across different database engines.

### 1.1 Fields & Column Specifications

| Field Name | Kotlin / Java Type | Nullable | MySQL Column Type | PostgreSQL Column Type | Description / Notes |
|:---|:---|:---:|:---|:---|:---|
| `id` | `String` (UUID) | No | `VARCHAR(36)` / `CHAR(36)` | `VARCHAR(36)` / `UUID` | Primary Key, auto-generated or assigned UUID string |
| `title` | `String` | No | `VARCHAR(255)` | `VARCHAR(255)` | Scenario identifier (e.g. "Seoul Now", "UTC Baseline") |
| `localDateTime` | `java.time.LocalDateTime` | No | `DATETIME(6)` | `TIMESTAMP(6) WITHOUT TIME ZONE` | Date and time without timezone offset |
| `localDate` | `java.time.LocalDate` | No | `DATE` | `DATE` | Date only (yyyy-MM-dd) |
| `localTime` | `java.time.LocalTime` | No | `TIME(6)` | `TIME(6) WITHOUT TIME ZONE` | Time only without timezone offset (HH:mm:ss.SSSSSS) |
| `instant` | `java.time.Instant` | No | `DATETIME(6)` | `TIMESTAMP(6) WITH TIME ZONE` | UTC point in time |
| `zonedDateTime` | `java.time.ZonedDateTime` | No | `DATETIME(6)` | `TIMESTAMP(6) WITH TIME ZONE` | Date/time with full TimeZone (`Asia/Seoul`, `UTC`, etc.) |
| `offsetDateTime` | `java.time.OffsetDateTime` | No | `DATETIME(6)` | `TIMESTAMP(6) WITH TIME ZONE` | Date/time with ZoneOffset (`+09:00`, `+00:00`, `-04:00`) |
| `offsetTime` | `java.time.OffsetTime` | No | `TIME(6)` | `TIME(6) WITH TIME ZONE` | Time with ZoneOffset (`14:30:00+09:00`) |
| `createdAt` | `java.time.Instant` | No | `DATETIME(6)` | `TIMESTAMP(6) WITH TIME ZONE` | Entity creation audit timestamp |
| `updatedAt` | `java.time.Instant` | No | `DATETIME(6)` | `TIMESTAMP(6) WITH TIME ZONE` | Entity update audit timestamp |

---

## 2. DB Raw Metadata & Inspection Model: `TimeRecordRawView`

DTO used by the inspection endpoint `/api/time-records/{id}/db-raw` to return both the JPA entity values and the raw JDBC database column representations for side-by-side comparison.

### 2.1 Properties

```kotlin
data class TimeRecordRawView(
    val entity: TimeRecordResponse,
    val dbEngine: String, // "MySQL 8.x" or "PostgreSQL 16.x"
    val columnTypes: Map<String, String>, // e.g. "offset_date_time" -> "TIMESTAMP WITH TIME ZONE"
    val rawValues: Map<String, String?>  // e.g. "offset_date_time" -> "2026-10-09 08:30:00.123456+09"
)
```

---

## 3. Data Transfer Objects (DTOs)

### 3.1 `CreateTimeRecordRequest`

```kotlin
data class CreateTimeRecordRequest(
    val title: String,
    val localDateTime: LocalDateTime = LocalDateTime.now(),
    val localDate: LocalDate = LocalDate.now(),
    val localTime: LocalTime = LocalTime.now(),
    val instant: Instant = Instant.now(),
    val zonedDateTime: ZonedDateTime = ZonedDateTime.now(),
    val offsetDateTime: OffsetDateTime = OffsetDateTime.now(),
    val offsetTime: OffsetTime = OffsetTime.now()
)
```

### 3.2 `TimeRecordResponse`

```kotlin
data class TimeRecordResponse(
    val id: String,
    val title: String,
    val localDateTime: LocalDateTime,
    val localDate: LocalDate,
    val localTime: LocalTime,
    val instant: Instant,
    val zonedDateTime: ZonedDateTime,
    val offsetDateTime: OffsetDateTime,
    val offsetTime: OffsetTime,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

---

## 4. Database Schema DDL (Auto-Generated Reference)

### MySQL DDL Reference
```sql
CREATE TABLE time_record (
    id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    local_date_time DATETIME(6) NOT NULL,
    local_date DATE NOT NULL,
    local_time TIME(6) NOT NULL,
    instant DATETIME(6) NOT NULL,
    zoned_date_time DATETIME(6) NOT NULL,
    offset_date_time DATETIME(6) NOT NULL,
    offset_time TIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);
```

### PostgreSQL DDL Reference
```sql
CREATE TABLE time_record (
    id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    local_date_time TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    local_date DATE NOT NULL,
    local_time TIME(6) WITHOUT TIME ZONE NOT NULL,
    instant TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    zoned_date_time TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    offset_date_time TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    offset_time TIME(6) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);
```
