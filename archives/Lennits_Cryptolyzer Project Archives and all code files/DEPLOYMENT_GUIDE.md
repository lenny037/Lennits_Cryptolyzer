# Complete Deployment Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development](#local-development)
3. [Production Deployment](#production-deployment)
4. [Environment Configuration](#environment-configuration)
5. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### System Requirements
- Ubuntu 20.04+ / Debian 11+ / macOS / Windows WSL2
- 4GB RAM minimum (8GB recommended)
- 10GB disk space
- Python 3.10+
- Node.js 18+
- MongoDB 5.0+

### Install Dependencies

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install python3 python3-pip python3-venv nodejs npm mongodb
npm install -g yarn
```

**macOS:**
```bash
brew install python node mongodb-community yarn
```

---

## Local Development

### 1. Backend Setup

```bash
cd backend

# Create virtual environment
python3 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Configure environment
cp .env.example .env
nano .env  # Edit with your settings

# Run development server
uvicorn server:app --reload --host 0.0.0.0 --port 8001
```

**Test backend:**
```bash
curl http://localhost:8001/api/
# Expected: {"message":"Hello World"}
```

### 2. Frontend Setup

```bash
cd frontend

# Install dependencies
yarn install

# Configure environment
cp .env.example .env
nano .env  # Edit REACT_APP_BACKEND_URL

# Run development server
yarn start  # Opens http://localhost:3000
```

### 3. MongoDB Setup

**Local MongoDB:**
```bash
# Start MongoDB
sudo systemctl start mongod  # Linux
brew services start mongodb-community  # macOS

# Verify
mongosh
```

**MongoDB Atlas (Cloud):**
1. Create account at mongodb.com/cloud/atlas
2. Create cluster
3. Get connection string
4. Update backend/.env with MONGO_URL

---

## Production Deployment

### Option 1: Nginx + Gunicorn

**Backend:**
```bash
# Install Gunicorn
pip install gunicorn

# Create systemd service
sudo nano /etc/systemd/system/backend.service
```

```ini
[Unit]
Description=FastAPI Backend
After=network.target

[Service]
User=www-data
WorkingDirectory=/var/www/app/backend
Environment="PATH=/var/www/app/backend/venv/bin"
ExecStart=/var/www/app/backend/venv/bin/gunicorn server:app \
  --workers 4 \
  --worker-class uvicorn.workers.UvicornWorker \
  --bind 0.0.0.0:8001
Restart=always

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable backend
sudo systemctl start backend
```

**Frontend:**
```bash
cd frontend
yarn build

# Copy to web root
sudo cp -r build/* /var/www/html/
```

**Nginx Configuration:**
```bash
sudo nano /etc/nginx/sites-available/app
```

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    root /var/www/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8001;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/app /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### Option 2: Docker Deployment

**Create docker-compose.yml:**
```yaml
version: '3.8'

services:
  mongodb:
    image: mongo:5
    volumes:
      - mongo_data:/data/db
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: changeme

  backend:
    build: ./backend
    ports:
      - "8001:8001"
    environment:
      - MONGO_URL=mongodb://admin:changeme@mongodb:27017/
      - DB_NAME=production
    depends_on:
      - mongodb

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mongo_data:
```

**Deploy:**
```bash
docker-compose up -d
docker-compose logs -f
```

### Option 3: Cloud Platforms

**Heroku:**
```bash
heroku create myapp
git push heroku main
```

**AWS / GCP / Azure:**
See respective platform documentation for deployment.

---

## Environment Configuration

### Backend Environment Variables

**Required:**
- `MONGO_URL` - MongoDB connection string
- `DB_NAME` - Database name
- `CORS_ORIGINS` - Allowed origins (comma-separated)

**Optional:**
- `LOG_LEVEL` - Logging level (INFO, DEBUG, ERROR)
- `API_PREFIX` - API route prefix (default: /api)

### Frontend Environment Variables

**Required:**
- `REACT_APP_BACKEND_URL` - Backend API URL

**Build-time only:**
These must be set before `yarn build`

---

## SSL/TLS Setup

### Using Let's Encrypt (Free)

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d yourdomain.com

# Auto-renewal
sudo certbot renew --dry-run
```

---

## Database Management

### Backup

```bash
# Backup
mongodump --uri="$MONGO_URL" --out=/backup/$(date +%Y%m%d)

# Restore
mongorestore --uri="$MONGO_URL" /backup/20260403
```

### Create Indexes

```bash
mongosh
use myapp_production
db.status_checks.createIndex({ timestamp: -1 })
```

---

## Monitoring

### Health Checks

```bash
# Backend
curl http://localhost:8001/api/

# Frontend
curl http://localhost:3000/

# MongoDB
mongosh --eval "db.adminCommand('ping')"
```

### Logs

```bash
# Backend (systemd)
sudo journalctl -u backend -f

# Nginx
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# MongoDB
sudo tail -f /var/log/mongodb/mongod.log
```

---

## Troubleshooting

### Backend Won't Start

**Check logs:**
```bash
sudo journalctl -u backend -n 50
```

**Common issues:**
- MongoDB not running: `sudo systemctl start mongod`
- Port in use: `sudo lsof -i :8001`
- Missing dependencies: `pip install -r requirements.txt`

### Frontend Build Fails

**Clear cache:**
```bash
rm -rf node_modules yarn.lock
yarn cache clean
yarn install
```

**Check Node version:**
```bash
node --version  # Must be 18+
```

### Database Connection Issues

**Test connection:**
```bash
mongosh "$MONGO_URL"
```

**Check firewall:**
```bash
sudo ufw allow 27017/tcp
```

### CORS Errors

**Update backend/.env:**
```env
CORS_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

**Restart backend:**
```bash
sudo systemctl restart backend
```

---

## Performance Optimization

### Backend
- Use Redis for caching
- Enable connection pooling
- Add database indexes
- Use async operations
- Implement rate limiting

### Frontend
- Enable gzip compression
- Use CDN for static assets
- Lazy load components
- Optimize images
- Enable service workers

### Database
- Create indexes on queried fields
- Use aggregation pipelines
- Enable sharding for scale
- Regular maintenance

---

## Security Checklist

- [ ] Change default credentials
- [ ] Update CORS to specific domains
- [ ] Enable MongoDB authentication
- [ ] Configure firewall
- [ ] Install SSL certificates
- [ ] Use strong passwords
- [ ] Enable rate limiting
- [ ] Set up monitoring
- [ ] Regular backups
- [ ] Keep dependencies updated

---

## Production Checklist

**Before Launch:**
- [ ] All tests passing
- [ ] Environment variables set
- [ ] SSL configured
- [ ] Backups configured
- [ ] Monitoring set up
- [ ] Load testing done
- [ ] Security audit complete

**After Launch:**
- [ ] Monitor error rates
- [ ] Check performance
- [ ] Verify backups
- [ ] Review logs
- [ ] User feedback

---

For additional help, consult the framework documentation:
- FastAPI: https://fastapi.tiangolo.com
- React: https://react.dev
- MongoDB: https://docs.mongodb.com
