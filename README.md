# CMFC System Server

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Setup](#setup)
- [Contributing](#contributing)
- [Future Plans](#future-plans)

## Introduction

CMFC System Server is a simple cooperative management system built with Kotlin and Ktor
framework. It provides a robust backend system for managing dairy operations including livestock
tracking, milk production, member management, and financial reporting, facilitating the development
of dairy cooperative management platforms.

## Features

- User authentication and authorization with JWT
- CRUD operations for managing members, cows, and milk production
- Secure API endpoints for interacting with the server
- Flexible and scalable architecture for accommodating future enhancements
- Livestock health monitoring and breeding management
- Real-time milk inventory tracking and spoilage management
- Financial reporting with daily, weekly, and monthly analytics
- Member lifecycle management with archiving capabilities
- Auto-generated IDs for systematic identification

## Installation

To install CMFC System Server locally, follow these steps:

1. Clone the repository:

```bash
git clone https://github.com/mkaomwakuni/CMCF_System_Server_KMP.git
```

2. Navigate to the project directory:

```bash
cd "CMFC System Server"
```

3. Install dependencies:
```bash
./gradlew build
```

4. Start the server:
```bash
./gradlew :server:run
```

## Usage

Once the server is running:

- Server starts on `http://localhost:8081`
- Default superuser: username=`admin`, password=`admin`
- Use API endpoints to manage dairy operations
- Access health check at `/health`

## API Endpoints

### Authentication
- `POST /auth/signup` - User registration
- `POST /auth/signin` - User login

### Core Operations
- `GET/POST/PUT/DELETE /cows` - Livestock management
- `GET/POST /members` - Member management
- `GET/POST/DELETE /milk-in` - Milk collection tracking
- `GET/POST/DELETE /milk-out` - Milk sales management
- `GET/POST/DELETE /milk-spoilt` - Spoilage tracking
- `GET/POST /customers` - Customer management

### Analytics & Reports
- `GET /stock-summary` - Inventory reports
- `GET /earnings-summary` - Financial reports
- `GET /cow-summary` - Livestock statistics

### Health Check

- `GET /health` - Server status check

## Setup

Remember you need to setup the CMFC System Server to use dairy management applications. It's
mandatory, otherwise it will not work at all. To Setup, you need to clone this on your local device.
Open it in IntelliJ IDEA.

### Database Configuration

After cloning the project, you have two options:

**Option 1: H2 Database (Development)**

- The server uses H2 database by default
- Database files are stored in `./data/` directory
- No additional setup required

**Option 2: PostgreSQL (Production)**

1. Install PostgreSQL and pgAdmin 4
2. Create a new database
3. Update database configuration in `DatabaseConfig.kt`:

```kotlin
Database.connect(
  url = "jdbc:postgresql://localhost:5432/your_database_name",
  driver = "org.postgresql.Driver",
  user = "your_username",
  password = "your_password"
)
```

### API Configuration

- Update API key in `API_KEY_HEADER.kt` if needed
- Default API key: `dairy-app-secret-key-12345`

### Network Setup

To use this server in your applications locally, you need to get your local IP:

1. Open Terminal or Command Prompt
2. Get the local IP address
3. Update base URL in your client applications to `http://your_ip:8081/`

If you still get any errors, please create an issue in the repository.

## Contributing

If you want to contribute, please make sure to add new features and then make a PR. Feel free to
contribute to the project and stay tuned for more exciting updates!

## Future Plans

- Mobile app integration for field operations
- Advanced analytics dashboard with charts
- IoT device integration for automated milk collection
- Multi-tenant support for multiple cooperatives
- Deployment on Heroku or Google Cloud
- Enhancement to existing codebase
- Real-time notifications system
- Backup and data export features

---
