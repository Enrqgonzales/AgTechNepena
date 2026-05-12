# AgTech Nepeña

Aplicacion movil offline-first para agricultores de palta y mango en el Valle de Nepeña, Áncash, Perú.

## Arquitectura

- **Android App**: Java + MVC + DAO + Room 2.6.x
- **Backend**: Spring Boot 3.x + PostgreSQL
- **Sincronizacion**: WorkManager + Retrofit 2

## Estructura del Proyecto

```
AgTechNepeña/
├── app/                  # Android Application
│   ├── src/main/java/    # Codigo fuente Java
│   └── src/main/res/     # Recursos XML
└── backend/              # Spring Boot API
    ├── src/main/java/    # Backend Java
    └── src/main/resources/
```

## Requisitos

### Android App
- Android SDK: minSdk 26, targetSdk 34
- Java 8+
- Gradle 8.0+

### Backend
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Configuracion

### Base de datos PostgreSQL
```sql
CREATE DATABASE agtech_db;
```

### Backend (`backend/src/main/resources/application.properties`)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/agtech_db
spring.datasource.username=postgres
spring.datasource.password=changeme
```

### Android App (`app/build.gradle`)
La URL del backend esta configurada en `SyncWorker.java`:
```java
private static final String BASE_URL = "http://192.168.1.100:8080/api/sync";
```

## Compilacion

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Android
```bash
cd app
./gradlew assembleDebug
```

## Permisos Android

- `INTERNET` - Conexion a APIs
- `ACCESS_FINE_LOCATION` - GPS para clima
- `RECORD_AUDIO` - Comandos de voz
- `FOREGROUND_SERVICE` - Sincronizacion en background

## API Endpoints

| Endpoint | Metodo | Descripcion |
|----------|--------|-------------|
| `/api/sync/usuarios` | POST | Sincronizar usuarios |
| `/api/sync/parcelas` | POST | Sincronizar parcelas |
| `/api/sync/registros` | POST | Sincronizar registros |

## Caracteristicas

- **Offline-first**: Todos los datos se guardan localmente en SQLite via Room
- **Sincronizacion automatica**: WorkManager sincroniza cada 15 min con conexion
- **Accesibilidad**: Ajustes de fuente, brillo, tema y asistente de voz
- **Clima**: Integracion con Open-Meteo API
- **Tipo de cambio**: Integracion con ExchangeRate-API

## Licencia

MIT License - AgTech Nepeña Team
