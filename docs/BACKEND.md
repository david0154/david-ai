# 📑 DAVID AI - Backend Integration Guide

## Overview

DAVID AI backend handles authentication, cloud sync, and API proxying.

## Architecture

```
DAVID AI App
    ↓
  HTTPS
    ↓
PHP Backend
├─ Auth Service (Google OAuth)
├─ Sync Service (Cloud Storage)
├─ Proxy Service (Replicate API)
└─ Logging Service
```

## Setup

### 1. Install Dependencies

```bash
cd backend
composer install
```

### 2. Configure Environment

```bash
cp .env.example .env
# Edit .env with your settings
```

### 3. Initialize Database

```bash
php artisan migrate
php artisan seed
```

### 4. Start Server

```bash
php artisan serve --host=0.0.0.0 --port=8000
```

## API Endpoints

### Authentication

**POST /auth/google**
```bash
curl -X POST http://localhost:8000/auth/google \
  -H "Content-Type: application/json" \
  -d '{"token": "google-token-here"}'
```

Response:
```json
{
  "success": true,
  "token": "jwt-token",
  "user_id": 123,
  "email": "user@example.com"
}
```

### Cloud Sync

**POST /sync/data**
```bash
curl -X POST http://localhost:8000/sync/data \
  -H "Authorization: Bearer jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "data_type": "settings",
    "data": "encrypted-data-here",
    "timestamp": 1704816899
  }'
```

**GET /sync/data**
```bash
curl http://localhost:8000/sync/data \
  -H "Authorization: Bearer jwt-token"
```

### Image Generation

**POST /api/image/generate**
```bash
curl -X POST http://localhost:8000/api/image/generate \
  -H "Authorization: Bearer jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "A beautiful sunset over mountains",
    "model": "stable-diffusion"
  }'
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    picture VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Sync Data Table
```sql
CREATE TABLE sync_data (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    data_type VARCHAR(50) NOT NULL,
    data LONGBLOB NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_sync (user_id, data_type)
);
```

## Environment Variables

```env
# Database
DB_HOST=localhost
DB_USER=root
DB_PASS=password
DB_NAME=david_ai

# Google OAuth
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# Replicate API (Image Generation)
REPLICATE_API_KEY=your-api-key

# Encryption
ENCRYPTION_KEY=base64-encoded-32-byte-key
MASTER_KEY=your-master-key

# JWT
JWT_SECRET=your-jwt-secret
JWT_EXPIRY=86400
```

## Deployment

### Production Checklist

- [ ] Database backups enabled
- [ ] HTTPS certificates installed
- [ ] Rate limiting configured
- [ ] Monitoring alerts set up
- [ ] Logging to external service
- [ ] Secrets not in version control
- [ ] Environment-specific config
- [ ] Database indexed
- [ ] API documentation generated

### Docker Deployment

```dockerfile
FROM php:8.2-apache

RUN docker-php-ext-install pdo pdo_mysql

COPY . /var/www/html/

RUN chown -R www-data:www-data /var/www/html/

EXPOSE 80
```

```bash
docker build -t david-ai-backend .
docker run -p 8000:80 -e DB_HOST=host.docker.internal david-ai-backend
```

## Monitoring

### Health Check
```bash
curl http://localhost:8000/health
```

### Logs
```bash
tail -f logs/app.log
```

### Metrics
- Request count
- Response time
- Error rate
- Database queries
- Cache hit ratio

## Security Best Practices

1. **API Keys**: Store in environment variables
2. **Tokens**: Use JWT with short expiry
3. **Passwords**: Hash with bcrypt
4. **CORS**: Whitelist specific origins
5. **Rate Limit**: 100 requests/minute per user
6. **Logging**: Log all sensitive operations
7. **Updates**: Keep dependencies current

## Troubleshooting

### Database Connection Failed
```bash
# Check MySQL is running
sudo systemctl status mysql

# Verify credentials in .env
# Test connection
mysql -u root -p -h localhost
```

### API Token Expired
```bash
# Token expires in JWT_EXPIRY seconds
# App should refresh token automatically
# Check token in /sync/verify endpoint
```

### Image Generation Failed
```bash
# Verify Replicate API key
# Check internet connection
# Review API quota
```

## See Also

- [Security & Encryption](ENCRYPTION.md)
- [Voice Control](VOICE_GUIDE.md)
- [API Reference](#)
