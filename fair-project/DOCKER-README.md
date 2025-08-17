# Fuar Backend - Docker Deployment

## 🐳 Docker ile Çalıştırma

### Ön Gereksinimler
- Docker
- Docker Compose

### Hızlı Başlangıç

1. **Projeyi klonlayın:**
```bash
git clone <repository-url>
cd FuarBackend/fair-project
```

2. **Docker ile uygulamayı başlatın:**
```bash
docker-compose up -d
```

3. **Uygulamanın çalıştığını kontrol edin:**
```bash
curl http://localhost:8080/actuator/health
```

### Servisler

| Servis | Port | Açıklama |
|--------|------|----------|
| fuar-backend | 8080 | Spring Boot API |
| fuar-db | 5432 | PostgreSQL Database |

### API Endpoints

- **Health Check:** http://localhost:8080/actuator/health
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/v3/api-docs

### Environment Variables

Backend servisi aşağıdaki environment variable'larını kullanır:

```yaml
SPRING_PROFILES_ACTIVE: docker
SPRING_DATASOURCE_URL: jdbc:postgresql://fuar-db:5432/fuar_db
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: secret
```

### Volume Mounting

- `./uploads:/app/uploads` - Dosya yüklemeleri için
- `postgres_data:/var/lib/postgresql/data` - PostgreSQL verileri için

### Useful Commands

```bash
# Servisleri başlat
docker-compose up -d

# Logları görüntüle
docker-compose logs -f fuar-backend

# Servisleri durdur
docker-compose down

# Servisleri durdur ve volume'ları sil
docker-compose down -v

# Image'ları yeniden build et
docker-compose build --no-cache

# Sadece backend'i yeniden başlat
docker-compose restart fuar-backend

# Container'a bash ile bağlan
docker exec -it fuar-spring-app /bin/sh

# PostgreSQL'e bağlan
docker exec -it fuar-postgres psql -U postgres -d fuar_db
```

### Production Deployment

Production ortamı için:

1. **Güvenlik için şifreleri değiştirin:**
```yaml
environment:
  POSTGRES_PASSWORD: your-secure-password
  SPRING_DATASOURCE_PASSWORD: your-secure-password
```

2. **SSL/HTTPS konfigürasyonu ekleyin**

3. **Monitoring ve logging ekleyin**

4. **Backup stratejisi belirleyin**

### Troubleshooting

**Problem:** Container başlamıyor
**Çözüm:** 
```bash
docker-compose logs fuar-backend
docker-compose down && docker-compose up -d
```

**Problem:** Database bağlantı hatası
**Çözüm:**
```bash
docker-compose restart fuar-db
```

**Problem:** Port çakışması
**Çözüm:** docker-compose.yml'de port'ları değiştirin

### Health Checks

Her iki servis de health check'lere sahiptir:
- **PostgreSQL:** `pg_isready` komutu
- **Spring Boot:** `/actuator/health` endpoint'i

Health check'ler fail olursa container otomatik restart olur.
