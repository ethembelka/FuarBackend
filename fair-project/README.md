# Fuar Backend - Docker Setup

Bu proje Spring Boot ve PostgreSQL kullanarak geliştirilmiş bir backend uygulamasıdır.

## 🚀 Hızlı Başlangıç

### Gereksinimler
- Docker
- Docker Compose

### Çalıştırma

1. **Projeyi klonlayın:**
```bash
git clone <repository-url>
cd FuarBackend/fair-project
```

2. **Docker ile çalıştırın:**
```bash
# Tüm servisleri başlat
docker-compose up -d

# Logları takip et
docker-compose logs -f
```

3. **Uygulamaya erişin:**
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

### Durdurma

```bash
# Servisleri durdur
docker-compose down

# Verileri de sil
docker-compose down -v
```

## 📊 Servisler

### Backend (Spring Boot)
- **Port:** 8080
- **Container:** `fuar-spring-app`
- **Health Check:** `/actuator/health`

### Database (PostgreSQL)
- **Port:** 5432
- **Container:** `fuar-postgres`
- **Database:** `fuar_db`
- **User:** `postgres`
- **Password:** `secret`

## 🛠️ Geliştirme

### Local Build
```bash
# Sadece uygulamayı build et
docker build -t fuar-backend .

# Uygulamayı çalıştır (veritabanı ayrıca gerekli)
docker run -p 8080:8080 fuar-backend
```

### Database Connection
- **Local:** `jdbc:postgresql://localhost:5432/fuar_db`
- **Docker:** `jdbc:postgresql://fuar-db:5432/fuar_db`

## 📝 Notlar

- İlk çalıştırmada veritabanı otomatik oluşturulur
- Uploads klasörü volume olarak bağlanır
- Health check ile otomatik sağlık kontrolü yapılır
- Production için environment değişkenlerini güncelleyin
