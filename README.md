# Holiday API

A production-ready REST API for retrieving public holiday information from the Nager.Date API. Built with Spring Boot and designed to handle corporate proxy environments.

## 📋 Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [SSL Configuration](#ssl-configuration)
- [Project Structure](#project-structure)
- [Performance Optimization](#performance-optimization)
- [Error Handling](#error-handling)

## ✨ Features

The API provides three main functionalities as per requirements:

1. **Last 3 Celebrated Holidays**: Retrieve the most recent 3 celebrated holidays for a given country, looking backward from the current date.

2. **Non-Weekend Holiday Count**: For a given year and multiple countries, return the count of public holidays that don't fall on weekends (Saturday/Sunday), sorted in descending order.

3. **Common Holidays**: Given a year and two country codes, return a deduplicated list of dates celebrated in both countries with their local names.

## 🛠 Technologies

- **Java**: 21 (Eclipse Adoptium)
- **Spring Boot**: 3.3.0
- **Spring WebFlux**: For reactive WebClient
- **ByteBuddy**: 1.14.18 (Java 21 support)
- **Maven**: 3.8+
- **Mockito**: For unit testing
- **Caffeine Cache**: For API response caching
- **Springdoc OpenAPI**: For API documentation
- **Lombok**: For reducing boilerplate code

## 📦 Prerequisites

- **JDK 21** or higher
- **Maven 3.8+**
- **Git** (for cloning the repository)

## 🚀 Installation & Setup

### 1. Clone the Repository

````bash
git clone https://github.com/anantdandwate/holiday-api.git
cd holiday-api

2. Build the Project

```sh
mvn clean install
````

This will:

- Download all dependencies
- Compile the source code
- Run all tests
- Package the application as a JAR file

3. **SSL Configuration (Corporate Proxy Environments)**

If you're behind a corporate proxy that intercepts SSL traffic (e.g., Zscaler, Capgemini proxy):

**Option A: Use Custom Truststore (Recommended for Production)**

Create the certs directory:

```sh
mkdir certs
```

Copy your corporate root CA certificates to the `certs` folder.

The application is configured to use `certs/truststore.jks` by default.

**Option B: Use Insecure Mode (Development Only)**

Add this to `application.properties`:

```properties
ssl.insecure-mode=true
```

> ⚠️ **Warning:** Only use insecure mode in trusted development environments.

---

## 🏃 Running the Application

**Using Maven**

```sh
mvn spring-boot:run
```

**Using Java JAR**

```sh
java -jar target/holiday-api-1.0.0.jar
```

The application will start on [http://localhost:8080](http://localhost:8080)

**Verify Application is Running**

```sh
curl http://localhost:8080/actuator/health
```

---

## 📡 API Endpoints

### 1. Get Last 3 Celebrated Holidays

Retrieves the most recent 3 holidays that have been celebrated (past dates) for a given country.

**Endpoint:**

```
GET /api/holidays/last3/{countryCode}
```

**Example Request:**

```sh
curl http://localhost:8080/api/holidays/last3/US
```

**Example Response:**

```json
[
  {
    "date": "2024-04-03",
    "localName": "Good Friday",
    "name": "Good Friday",
    "countryCode": "US"
  },
  {
    "date": "2024-02-16",
    "localName": "Washington's Birthday",
    "name": "Presidents Day",
    "countryCode": "US"
  },
  {
    "date": "2024-01-19",
    "localName": "Martin Luther King, Jr. Day",
    "name": "Martin Luther King, Jr. Day",
    "countryCode": "US"
  }
]
```

Supported Country Codes: `US`, `GB`, `DE`, `FR`, etc. (ISO 3166-1 alpha-2 codes)

---

### 2. Count Non-Weekend Holidays

Returns the count of public holidays that don't fall on weekends for each country, sorted in descending order.

**Endpoint:**

```
GET /api/holidays/non-weekend-count?year={year}&countryCodes={codes}
```

**Parameters:**

- `year` (required): Year (e.g., 2024)
- `countryCodes` (required): Comma-separated list of country codes

**Example Request:**

```sh
curl "http://localhost:8080/api/holidays/non-weekend-count?year=2024&countryCodes=US,GB,DE"
```

**Example Response:**

```json
{
  "DE": 11,
  "US": 10,
  "GB": 8
}
```

---

### 3. Get Common Holidays Between Two Countries

Returns dates celebrated in both countries with their respective local names.

**Endpoint:**

```
GET /api/holidays/deduplicated?year={year}&countryCode1={code1}&countryCode2={code2}
```

**Parameters:**

- `year` (required): Year (e.g., 2024)
- `countryCode1` (required): First country code
- `countryCode2` (required): Second country code

**Example Request:**

```sh
curl "http://localhost:8080/api/holidays/deduplicated?year=2024&countryCode1=US&countryCode2=GB"
```

**Example Response:**

```json
[
  {
    "date": "2024-01-01",
    "localNames": ["New Year's Day", "New Year's Day"]
  },
  {
    "date": "2024-12-25",
    "localNames": ["Christmas Day", "Christmas Day"]
  }
]
```

---

## 📚 API Documentation (Swagger)

Interactive API documentation is available via Swagger UI:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

This provides:

- Interactive API testing
- Request/response examples
- Schema definitions
- Parameter descriptions

---

## 🧪 Testing

The project includes both unit tests and integration tests.

**Run All Tests**

```sh
mvn test
```

**Run Specific Test Class**

```sh
mvn test -Dtest=HolidayServiceTest
```

**Test Coverage**

- Unit Tests: Mock-based tests for service layer logic
- Integration Tests: Full Spring context tests for REST endpoints

**Test Results:**

```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

---

## 🔒 SSL Configuration

### Understanding the SSL Setup

This application is designed to work in corporate environments where SSL traffic is intercepted by proxies (e.g., Zscaler).

### Configuration Properties

In `src/main/resources/application.properties`:

```properties
# SSL Truststore Configuration
ssl.truststore.path=certs/truststore.jks
ssl.truststore.password=changeit
ssl.insecure-mode=false
```

### For Corporate Environments

**Export Corporate Root CA Certificate:**

1. Open Windows Certificate Manager (`certmgr.msc`)
2. Navigate to: Trusted Root Certification Authorities → Certificates
3. Export your corporate root CA as Base-64 encoded X.509 (.CER)

**Create Truststore:**

```sh
# Copy default Java truststore
cp $JAVA_HOME/lib/security/cacerts certs/truststore.jks

# Import corporate certificate
keytool -import -trustcacerts -alias corporate-root \
  -file corporate-root.cer \
  -keystore certs/truststore.jks \
  -storepass changeit
```

**Run Application:**

```sh
mvn spring-boot:run
```

### Development Mode (Insecure SSL)

For quick testing, you can bypass SSL validation:

```sh
mvn spring-boot:run -Dssl.insecure-mode=true
```

> ⚠️ **Never use this in production!**

---

## 📁 Project Structure

```
holiday-api/
├── src/
│   ├── main/
│   │   ├── java/com/acn/holidayapi/
│   │   │   ├── HolidayApiApplication.java    # Main application class
│   │   │   ├── client/
│   │   │   │   └── NagerDateApiClient.java   # External API client
│   │   │   ├── config/
│   │   │   │   └── WebClientConfig.java      # SSL & WebClient configuration
│   │   │   ├── controller/
│   │   │   │   └── HolidayController.java    # REST endpoints
│   │   │   ├── dto/
│   │   │   │   ├── HolidayResponseDto.java
│   │   │   │   └── DeduplicatedHolidayDto.java
|   |   |   |   ├── CountryDto.java
│   │   │   ├── exception/
│   │   │   │   ├── ApiException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/
│   │   │   │   └── Holiday.java              # Domain model
│   │   │   └── service/
│   │   │       └── HolidayService.java       # Business logic
│   │   └── resources/
│   │       └── application.properties         # Configuration
│   └── test/
│       └── java/com/acn/holidayapi/
│           ├── controller/
│           │   └── HolidayControllerIntegrationTest.java
│           └── service/
│               └── HolidayServiceTest.java
|               └── CountryValidationServiceTest.java
├── certs/                                     # SSL certificates (not in Git)
│   └── truststore.jks
├── .gitignore                                 # Git ignore rules
├── pom.xml                                    # Maven configuration
└── README.md                                  # This file
```

---

## ⚡ Performance Optimization

### Caching Strategy

The application implements caching to reduce external API calls:

- **Cache Type:** Caffeine (in-memory)
- **Cache Size:** 500 entries maximum
- **TTL:** 1 hour (configurable)

**Configuration (in application.properties):**

```properties
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=1h
```

### Algorithmic Complexity

- **Last 3 Holidays:** O(n log n) - sorting holidays by date
- **Non-Weekend Count:** O(n × m) - n countries, m holidays per country
- **Common Holidays:** O(n + m) - using HashMap for efficient lookup

### Memory Optimization

- WebClient uses reactive streams for efficient memory usage
- Response buffer limited to 16MB
- Cached responses reduce redundant API calls

---

## ⚠️ Error Handling

The application provides comprehensive error handling:

### HTTP Status Codes

- `200 OK` - Successful request
- `400 Bad Request` - Invalid parameters
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server-side error
- `502 Bad Gateway` - External API error

**Example Error Response**

```json
{
  "timestamp": "2024-05-03T12:30:45",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid country code: XYZ"
}
```

---

## 🔍 Logging

Application uses SLF4J with Logback for logging:

**Log Levels (configurable in application.properties):**

```properties
logging.level.com.acn.holidayapi=DEBUG
logging.level.org.springframework.web=INFO
logging.level.reactor.netty=INFO
```

---

## 📝 Configuration

Key configuration properties in `application.properties`:

```properties
# Application
nager.date.api.base-url=https://date.nager.at/api/v3
server.port=8080

# SSL
ssl.truststore.path=certs/truststore.jks
ssl.truststore.password=changeit
ssl.insecure-mode=false

# Caching
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=1h

# Logging
logging.level.com.acn.holidayapi=DEBUG
```

---

## 🚫 What's Not Included in Git

The following directories are excluded from version control:

- `target/` - Maven build output
- `certs/` - SSL certificates (security sensitive)
- `.idea/` - IDE-specific files
- `*.iml` - IntelliJ project files

See `.gitignore` for complete list.

---

## 🐛 Troubleshooting

**Issue: SSL Certificate Error**

- **Error:** PKIX path building failed
- **Solution:**
  - Set `ssl.insecure-mode=true` for testing
  - Or properly configure `certs/truststore.jks` with corporate CA

**Issue: Tests Failing with Mockito Error**

- **Error:** Java 21 is not supported by ByteBuddy
- **Solution:** Already fixed in `pom.xml` with ByteBuddy 1.14.18

**Issue: API Returns Empty Results**

- **Possible Causes:**
  - Invalid country code
  - No holidays in the requested period
  - External API is down
  - Check logs for detailed error messages.

---

## 📄 License

This project is created for assessment purposes.

---

## 👤 Author

**Anant Dandwate**

GitHub: [@anantdandwate](https://github.com/anantdandwate)

---

## 🙏 Acknowledgments

- Nager.Date API - Public holiday data provider
- Spring Boot team for excellent framework
- Open source community

---

## 🚀 Quick Start Commands

```sh
# Clone
git clone https://github.com/anantdandwate/holiday-api.git
cd holiday-api

# Build
mvn clean install

# Run
mvn spring-boot:run

# Test
curl http://localhost:8080/api/holidays/last3/US

# View API Docs
open http://localhost:8080/swagger-ui.html
```

_Last Updated: May 2024_

---
