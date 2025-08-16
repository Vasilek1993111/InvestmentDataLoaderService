# Deployment Guide

## Обзор

Данное руководство описывает различные способы развертывания Ingestion Service в продакшн среде.

## Предварительные требования

### Системные требования

- **CPU:** 2+ ядра
- **RAM:** 4+ GB
- **Диск:** 20+ GB свободного места
- **Сеть:** Доступ к интернету для API Тинькофф

### Программное обеспечение

- **Java 21** или выше
- **PostgreSQL 12** или выше
- **Docker** (опционально)
- **Kubernetes** (опционально)

## Варианты развертывания

### 1. Локальное развертывание

#### Шаг 1: Подготовка базы данных

```bash
# Установка PostgreSQL
sudo apt update
sudo apt install postgresql postgresql-contrib

# Запуск сервиса
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Создание базы данных
sudo -u postgres psql
```

```sql
-- Создание базы данных и пользователя
CREATE DATABASE postgres;
CREATE SCHEMA invest;
CREATE USER postgres WITH PASSWORD '123password123';
GRANT ALL PRIVILEGES ON DATABASE postgres TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA invest TO postgres;

-- Создание таблиц (автоматически создадутся при запуске приложения)
-- Выход из psql
\q
```

#### Шаг 2: Настройка приложения

```bash
# Клонирование репозитория
git clone <repository-url>
cd ingestion-service

# Создание JAR файла
mvn clean package
```

#### Шаг 3: Конфигурация

Создайте файл `application-prod.properties`:

```properties
# Продакшн конфигурация
spring.application.name=ingestion-service
server.port=8083

# База данных
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=123password123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Токен API Тинькофф
tinkoff.api.token=your_production_token_here

# Логирование
logging.level.com.example.ingestionservice=INFO
logging.level.org.springframework.web=WARN
logging.file.name=/var/log/ingestion-service/application.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Часовой пояс
app.timezone=Europe/Moscow
```

#### Шаг 4: Запуск приложения

```bash
# Создание директории для логов
sudo mkdir -p /var/log/ingestion-service
sudo chown $USER:$USER /var/log/ingestion-service

# Запуск приложения
java -jar target/ingestion-service-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8083
```

#### Шаг 5: Настройка systemd сервиса

Создайте файл `/etc/systemd/system/ingestion-service.service`:

```ini
[Unit]
Description=Ingestion Service
After=network.target postgresql.service

[Service]
Type=simple
User=ingestion
Group=ingestion
WorkingDirectory=/opt/ingestion-service
ExecStart=/usr/bin/java -jar ingestion-service.jar --spring.profiles.active=prod
ExecReload=/bin/kill -HUP $MAINPID
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# Создание пользователя
sudo useradd -r -s /bin/false ingestion

# Копирование файлов
sudo mkdir -p /opt/ingestion-service
sudo cp target/ingestion-service-0.0.1-SNAPSHOT.jar /opt/ingestion-service/ingestion-service.jar
sudo cp application-prod.properties /opt/ingestion-service/
sudo chown -R ingestion:ingestion /opt/ingestion-service

# Запуск сервиса
sudo systemctl daemon-reload
sudo systemctl enable ingestion-service
sudo systemctl start ingestion-service

# Проверка статуса
sudo systemctl status ingestion-service
```

### 2. Docker развертывание

#### Шаг 1: Создание Dockerfile

```dockerfile
FROM openjdk:21-jre-slim

# Создание пользователя
RUN groupadd -r ingestion && useradd -r -g ingestion ingestion

# Установка рабочей директории
WORKDIR /app

# Копирование JAR файла
COPY target/ingestion-service-0.0.1-SNAPSHOT.jar app.jar

# Создание директории для логов
RUN mkdir -p /var/log/ingestion-service && \
    chown -R ingestion:ingestion /var/log/ingestion-service

# Переключение на пользователя
USER ingestion

# Открытие порта
EXPOSE 8083

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Шаг 2: Создание docker-compose.yml

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/postgres
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=123password123
      - TINKOFF_API_TOKEN=your_production_token_here
    volumes:
      - ./logs:/var/log/ingestion-service
    depends_on:
      - db
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=postgres
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=123password123
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    restart: unless-stopped

volumes:
  postgres_data:
```

#### Шаг 3: Создание init.sql

```sql
-- Создание схемы для рыночных данных
CREATE SCHEMA IF NOT EXISTS invest;

-- Создание индексов для оптимизации
CREATE INDEX IF NOT EXISTS idx_shares_ticker ON shares(ticker);
CREATE INDEX IF NOT EXISTS idx_futures_ticker ON futures(ticker);
CREATE INDEX IF NOT EXISTS idx_close_prices_date ON invest.close_prices(price_date);
CREATE INDEX IF NOT EXISTS idx_close_prices_figi ON invest.close_prices(figi);
```

#### Шаг 4: Запуск

```bash
# Сборка и запуск
docker-compose up -d

# Проверка статуса
docker-compose ps

# Просмотр логов
docker-compose logs -f app
```

### 3. Kubernetes развертывание

#### Шаг 1: Создание ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ingestion-service-config
data:
  application.properties: |
    spring.application.name=ingestion-service
    server.port=8083
    
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=false
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
    
    app.timezone=Europe/Moscow
```

#### Шаг 2: Создание Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: ingestion-service-secrets
type: Opaque
data:
  tinkoff-api-token: <base64-encoded-token>
  db-password: <base64-encoded-password>
```

#### Шаг 3: Создание Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ingestion-service
  labels:
    app: ingestion-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: ingestion-service
  template:
    metadata:
      labels:
        app: ingestion-service
    spec:
      containers:
      - name: ingestion-service
        image: your-registry/ingestion-service:latest
        ports:
        - containerPort: 8083
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres-service:5432/postgres"
        - name: SPRING_DATASOURCE_USERNAME
          value: "postgres"
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: ingestion-service-secrets
              key: db-password
        - name: TINKOFF_API_TOKEN
          valueFrom:
            secretKeyRef:
              name: ingestion-service-secrets
              key: tinkoff-api-token
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8083
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8083
          initialDelaySeconds: 30
          periodSeconds: 10
      volumes:
      - name: config-volume
        configMap:
          name: ingestion-service-config
```

#### Шаг 4: Создание Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: ingestion-service
spec:
  selector:
    app: ingestion-service
  ports:
  - port: 8083
    targetPort: 8083
  type: ClusterIP
```

#### Шаг 5: Создание Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ingestion-service-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: api.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: ingestion-service
            port:
              number: 8083
```

#### Шаг 6: Развертывание

```bash
# Применение конфигураций
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f ingress.yaml

# Проверка статуса
kubectl get pods
kubectl get services
kubectl get ingress
```

## Мониторинг и логирование

### Prometheus метрики

Добавьте зависимость в `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Настройка в `application.properties`:

```properties
# Actuator
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

### Grafana Dashboard

Создайте dashboard для мониторинга:

```json
{
  "dashboard": {
    "title": "Ingestion Service Metrics",
    "panels": [
      {
        "title": "HTTP Requests",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])",
            "legendFormat": "{{method}} {{uri}}"
          }
        ]
      },
      {
        "title": "JVM Memory Usage",
        "type": "graph",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes",
            "legendFormat": "{{area}}"
          }
        ]
      }
    ]
  }
}
```

## Резервное копирование

### База данных

```bash
#!/bin/bash
# backup.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/postgres"
DB_NAME="postgres"

# Создание резервной копии
pg_dump -h localhost -U postgres -d $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# Удаление старых резервных копий (старше 30 дней)
find $BACKUP_DIR -name "backup_*.sql" -mtime +30 -delete
```

### Автоматизация через cron

```bash
# Добавление в crontab
0 2 * * * /path/to/backup.sh
```

## Обновление приложения

### Blue-Green Deployment

```bash
# Создание нового deployment
kubectl apply -f deployment-v2.yaml

# Проверка готовности
kubectl rollout status deployment/ingestion-service-v2

# Переключение трафика
kubectl patch service ingestion-service -p '{"spec":{"selector":{"app":"ingestion-service-v2"}}}'

# Удаление старого deployment
kubectl delete deployment ingestion-service-v1
```

### Rolling Update

```bash
# Обновление с rolling update
kubectl set image deployment/ingestion-service ingestion-service=your-registry/ingestion-service:v2

# Откат при необходимости
kubectl rollout undo deployment/ingestion-service
```

## Безопасность

### SSL/TLS

Настройка HTTPS в `application.properties`:

```properties
# SSL конфигурация
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your-password
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

### Аутентификация

Добавьте Spring Security:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### Сетевая безопасность

```bash
# Настройка firewall
sudo ufw allow 8083/tcp
sudo ufw allow 5432/tcp
sudo ufw enable
```

## Troubleshooting

### Частые проблемы

1. **Ошибка подключения к БД**
   ```bash
   # Проверка статуса PostgreSQL
   sudo systemctl status postgresql
   
   # Проверка подключения
   psql -h localhost -U postgres -d postgres
   ```

2. **Недостаточно памяти**
   ```bash
   # Увеличение heap size
   java -Xmx2g -Xms1g -jar app.jar
   ```

3. **Проблемы с API Тинькофф**
   ```bash
   # Проверка токена
   curl -H "Authorization: Bearer YOUR_TOKEN" \
        https://invest-public-api.tinkoff.ru/ru.tinkoff.piapi.contract.v1.UsersService/GetAccounts
   ```

### Логи

```bash
# Просмотр логов приложения
tail -f /var/log/ingestion-service/application.log

# Просмотр логов systemd
journalctl -u ingestion-service -f

# Просмотр логов Docker
docker-compose logs -f app
```