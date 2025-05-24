# CMFC System Server

**CMFC System Server** - A comprehensive dairy cooperative management
system built with Kotlin and Ktor framework.

## Overview

The CMFC System Server is a robust backend application designed specifically for dairy cooperative
management. It provides complete functionality for managing milk production, livestock tracking,
member management, inventory control, and financial reporting for dairy farming cooperatives.

## Key Features

### 🐄 Livestock Management

- **Cow Registration & Tracking**: Complete cow profiles with breed, age, weight, and health status
- **Health Status Monitoring**: Track health conditions, treatments, vaccinations, and deworming
  schedules
- **Action Status Tracking**: Monitor cow activities (active, sold, deceased, vaccinated, wormed)
- **Breeding Management**: Track calving dates and gestation periods
- **Antibiotic Treatment Monitoring**: Ensure milk safety with treatment tracking
- **Auto-Generated Cow IDs**: Systematic cow identification (CW01, CW02, etc.)

### 🥛 Milk Production Management

- **Milk Collection Tracking**: Record morning and evening milking sessions
- **Real-time Inventory Management**: Automatic stock calculations and updates
- **Milk Sales Management**: Track sales to customers with pricing and payment modes
- **Spoilage Tracking**: Record and categorize milk spoilage with loss calculations
- **Production Analytics**: Calculate daily, weekly, and monthly production metrics

### 👥 Member & Customer Management

- **Cooperative Member Management**: Register and manage dairy farmers
- **Member Statistics**: Track individual production performance
- **Customer Database**: Maintain customer records for milk sales
- **Active/Archived Status**: Manage member lifecycle with archiving capabilities
- **Auto-Generated IDs**: Systematic identification for members (OON01, OON02) and customers (
  CST001, CST002)

### 📊 Financial & Reporting System

- **Earnings Tracking**: Daily, weekly, and monthly revenue reports
- **Stock Summaries**: Real-time inventory with production and sales data
- **Payment Management**: Support for cash and M-Pesa payment modes
- **Loss Calculation**: Automatic financial impact calculation for spoilage

### 🔐 User Management & Security

- **Role-Based Access Control**: Tier system (USER)
- **JWT Authentication**: Secure session management
- **API Key Protection**: Protected endpoints with API key authentication
- **Password Security**: Encrypted password storage with bcrypt hashing
- **User Data Storage**: Key-value data storage for user preferences and settings

### 📈 Analytics & Insights

- **Production Statistics**: Average daily milk production per cow and member
- **Health Monitoring**: Track cows needing attention vs healthy livestock
- **Inventory Analytics**: Real-time stock levels with trend analysis
- **Member Performance**: Individual and collective productivity metrics

## Technology Stack

### Backend Framework

- **Kotlin** - Primary programming language
- **Ktor 2.3.5** - Lightweight web framework for building REST APIs
- **Netty** - High-performance NIO server engine

### Database & ORM

- **PostgreSQL** - Primary production database support
- **H2 Database** - Development and testing database
- **Exposed ORM** - Kotlin SQL framework for database operations
- **JDBC** - Database connectivity

### Security & Authentication

- **JWT (JSON Web Tokens)** - Session management and authentication
- **BCrypt** - Password hashing and security
- **API Key Authentication** - Protected endpoint access

### Data Handling

- **Kotlinx Serialization** - JSON serialization/deserialization
- **Kotlinx DateTime** - Date and time handling
- **Content Negotiation** - Automatic JSON content handling

### Development & Testing

- **Gradle with Kotlin DSL** - Build automation
- **JUnit** - Unit testing framework
- **Kotlinx Coroutines** - Asynchronous programming
- **Logback** - Comprehensive logging system

### Deployment & Infrastructure

- **Embedded Server** - Self-contained deployment
- **File-based Data Storage** - Local data persistence
- **Cross-platform Support** - Runs on Windows, Linux, macOS

## API Architecture

### RESTful Endpoints

- **Authentication Routes** (`/auth/*`) - User registration, login, role management
- **User Management** (`/users/*`) - User CRUD operations and data management
- **Milk Management** (`/milk-in/*`) - Milk collection and inventory tracking
- **Member Operations** - Farmer and cooperative member management
- **Reporting APIs** - Financial and production analytics

### Data Models

- **Cow**: Complete livestock information with health and status tracking
- **Member**: Dairy farmer profiles with production statistics
- **MilkEntry**: Production records with time-based tracking
- **User**: Authentication and role management
- **Customer**: Sales and distribution management
- **Summary Models**: Aggregated reporting and analytics

## Database Schema

### Core Tables

- **Members** - Cooperative farmer records
- **Cows** - Livestock inventory and health data
- **Customers** - Milk buyers and distribution partners
- **MilkInEntries** - Production collection records
- **MilkOutEntries** - Sales and distribution records
- **MilkSpoiltEntries** - Loss tracking and categorization
- **MilkInventory** - Real-time stock management
- **Users** - Authentication and access control
- **UserData** - User preferences and application data

### Key Features

- **Automated ID Generation** - Systematic identification across all entities
- **Referential Integrity** - Proper foreign key relationships
- **Date-based Tracking** - Comprehensive temporal data management
- **Status Management** - Active/archived entity lifecycle

## Configuration

### Server Configuration

- **Port**: 8081 (configurable)
- **Host**: 0.0.0.0 (accepts connections from all interfaces)
- **Database**: File-based H2 for development, PostgreSQL for production
- **API Key**: `dairy-app-secret-key-12345` (configurable)

### Development Setup

- **Build Tool**: Gradle with Kotlin DSL
- **JVM Target**: Kotlin 1.9.0
- **Development Mode**: Hot reload support
- **Logging**: Comprehensive request/response logging

## Security Features

### Authentication & Authorization

- **Multi-role System**: USER, ADMIN, SUPERUSER hierarchical access
- **Session Management**: JWT-based authentication with configurable expiration
- **Protected Routes**: API key authentication for sensitive operations
- **Password Security**: Industry-standard bcrypt hashing

### Data Protection

- **Input Validation**: Comprehensive request validation
- **Error Handling**: Secure error responses without sensitive data exposure
- **Access Logging**: Complete audit trail for all operations

## Monitoring & Logging

### Comprehensive Logging

- **Request/Response Logging**: Complete HTTP transaction logs
- **Database Operations**: SQL query logging and performance monitoring
- **Authentication Events**: Login attempts and security events
- **Error Tracking**: Detailed error logging with stack traces

### Performance Monitoring

- **Real-time Metrics**: Production, sales, and inventory statistics
- **Health Checks**: System status and database connectivity
- **Stock Calculations**: Automated inventory reconciliation

## Installation & Deployment

### Requirements

- **JVM 11+** - Java Virtual Machine
- **Gradle** - Build automation (wrapper included)
- **Database** - H2 (embedded) or PostgreSQL

### Quick Start

1. Clone the repository
2. Run `./gradlew :server:run` (Unix) or `gradlew.bat :server:run` (Windows)
3. Server starts on `http://localhost:8081`
4. Default superuser: username=`admin`, password=`admin`

### Production Deployment

- Configure PostgreSQL database connection
- Update API keys and security settings
- Set up proper logging and monitoring
- Configure reverse proxy (nginx/Apache) if needed

## API Documentation

Complete API documentation with examples is available in `client_example.md`, covering:

- Authentication workflows
- User management operations
- Milk production tracking
- Financial reporting
- Member and livestock management

## Contributing

This system is designed for dairy cooperative management with extensible architecture supporting
additional features like:

- Mobile app integration
- Advanced analytics and reporting
- IoT device integration for automated milk collection
- Multi-tenant support for multiple cooperatives
- Advanced financial management and accounting integration

---


