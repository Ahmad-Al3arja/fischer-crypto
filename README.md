# Fischer Crypto - Investment Platform

A comprehensive Spring Boot-based investment platform with referral system, daily profit counters, and admin management.

## Features

### Core Features
- **User Authentication & Authorization** - JWT-based authentication with role-based access
- **Investment Plans** - Multiple plan levels with different profit rates
- **Daily Profit Counter** - Automated daily profit calculation and distribution
- **Referral System** - Multi-level referral program with earnings tracking
- **Wallet Management** - USDT TRC20 wallet integration
- **Deposit/Withdrawal System** - Secure transaction processing with admin approval
- **Admin Dashboard** - Comprehensive admin panel for platform management

### Security Features
- **Rate Limiting** - Withdrawal rate limiting (max 3 per 24 hours)
- **Input Validation** - Strong validation for phone numbers, usernames, and passwords
- **CORS Configuration** - Production-ready CORS setup
- **JWT Security** - Environment variable-based JWT secret management

### Internationalization
- **Multi-language Support** - Arabic, English, and German localization
- **Default Arabic** - Platform defaults to Arabic language

## Technology Stack

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database**: MySQL with Flyway migrations
- **Authentication**: JWT tokens
- **Build Tool**: Gradle
- **API Documentation**: Swagger/OpenAPI

## Quick Start

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher
- Gradle 7.x or higher

### Environment Variables
```bash
# Required
JWT_SECRET=your-super-secret-jwt-key-here
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/fischer_crypto
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# Optional
SPRING_PROFILES_ACTIVE=dev
```

### Database Setup
The application uses Flyway for database migrations. Tables will be created automatically on startup.

### Running the Application
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will be available at `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/change-password` - Change password

### User Endpoints
- `GET /api/user/dashboard` - User dashboard
- `GET /api/user/profile` - User profile
- `PUT /api/user/profile` - Update profile
- `GET /api/user/team/stats` - Team statistics
- `GET /api/user/team/earnings-breakdown` - Referral earnings breakdown
- `GET /api/user/daily-counter/status` - Daily counter status

### Transaction Endpoints
- `POST /api/transactions/deposits` - Create deposit
- `POST /api/transactions/withdrawals` - Create withdrawal
- `GET /api/transactions/deposits` - Get user deposits
- `GET /api/transactions/withdrawals` - Get user withdrawals
- `GET /api/transactions/wallet` - Get wallet info

### Admin Endpoints
- `GET /api/admin/users` - Get all users
- `POST /api/admin/users/{userId}/activate` - Activate user
- `POST /api/admin/users/{userId}/suspend` - Suspend user
- `GET /api/admin/deposits` - Get all deposits
- `POST /api/admin/deposits/{depositId}/approve` - Approve deposit
- `GET /api/admin/withdrawals` - Get all withdrawals
- `POST /api/admin/withdrawals/{withdrawalId}/approve` - Approve withdrawal
- `GET /api/admin/stats/overview` - Admin dashboard statistics

### Platform Endpoints
- `GET /api/platform/info` - Platform information
- `GET /api/plans` - Get available plans

## Database Schema

### Core Tables
- `users` - User accounts and profiles
- `plans` - Investment plans
- `daily_counters` - Daily profit tracking
- `deposits` - Deposit transactions
- `withdrawals` - Withdrawal transactions
- `wallets` - User wallet addresses
- `admin_settings` - Platform configuration
- `promo_codes` - Promotional codes
- `referral_usage` - Referral tracking

## Security Considerations

1. **JWT Secret**: Always use a strong, unique JWT secret via environment variable
2. **Database**: Use strong passwords and limit database access
3. **CORS**: Configure allowed origins for production
4. **Rate Limiting**: Withdrawal rate limiting prevents abuse
5. **Input Validation**: All user inputs are validated and sanitized

## Development

### Project Structure
```
src/main/java/com/crypto/crypto/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── repository/     # Data access layer
├── service/        # Business logic
└── security/       # Security configuration
```

### Adding New Features
1. Create entity in `entity/` package
2. Add repository interface in `repository/` package
3. Implement business logic in `service/` package
4. Create DTOs in `dto/` package
5. Add controller endpoints in `controller/` package
6. Add Flyway migration if needed

## License

This project is proprietary software. All rights reserved.

## Support

For support and questions, please contact the development team. 