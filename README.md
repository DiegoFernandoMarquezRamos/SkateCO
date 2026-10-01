# SkateCO (TFG - DAM)

## 📌 Abstract
Plataforma móvil nativa en Android (*Offline-First* con Room) y backend en Spring Boot para la geolocalización y catalogación comunitaria de *spots* urbanos de deslizamiento. Resuelve la indexación de infraestructura callejera no mapeada mediante consultas espaciales en PostgreSQL/PostGIS y subida de medios desacoplada en AWS S3.

## 🏗 Arquitectura y Stack Tecnológico

El proyecto se rige por principios de Clean Architecture, segregación de interfaces e integridad transaccional ACID, preparado para entornos de conectividad degradada y restricciones de recursos (Cloud Free Tier).

### Cliente Móvil (Android)
- **Lenguaje:** Kotlin.
- **Arquitectura:** Clean Architecture + MVVM.
- **Persistencia Local (Offline-First):** Room (SQLite) como Single Source of Truth + flujos reactivos (`StateFlow`).
- **Sincronización:** Jetpack WorkManager (ejecución diferida garantizada).
- **Cartografía:** MapLibre Native SDK + teselas vectoriales OpenStreetMap (OSM).
- **Inyección de Dependencias:** Hilt.
- **Edge AI (Opcional):** Inferencia on-device (LiteRT / MediaPipe) para filtro anti-spam visual.

### Backend y Servicios Cloud
- **Framework:** Spring Boot 3 (Java 17/21).
- **Seguridad:** Spring Security con JWT (Stateless) y aislamiento de credenciales (Argon2id/BCrypt).
- **Base de Datos:** PostgreSQL con extensión espacial **PostGIS**.
- **Gestión Multimedia:** Patrón de subida desacoplada mediante AWS S3 Presigned URLs (Zero-I/O bottleneck).
- **Migraciones:** Flyway para versionado del esquema DDL.

---

## 🎯 Objetivos de Ingeniería (Evaluables)

### 1. Objetivo General
Diseñar, implementar y validar una plataforma distribuida multiplataforma para la localización, catalogación técnica y verificación colaborativa de infraestructura urbana apta para deportes de deslizamiento, garantizando tolerancia a fallos de conectividad mediante arquitectura *Offline-First* y consultas geoespaciales optimizadas.

### 2. Capa de Datos y Persistencia
- **Modelar el esquema relacional en 3FN** integrando tipos espaciales PostGIS e indexación **GiST** para procesar consultas *Bounding Box* con eficiencia $O(\log N)$.
- **Aislar credenciales y datos sensibles** en entidades independientes ($1:1$ con clave primaria compartida), mitigando fugas en JPA y configurando bloqueos temporales contra fuerza bruta.

### 3. Capa de Servicios (Backend)
- **Desarrollar una API RESTful stateless** asegurada mediante JWT y control de acceso basado en roles (RBAC).
- **Implementar subida multimedia desacoplada** vía Amazon S3 Presigned URLs, evitando la saturación de RAM/Red en la capa de aplicación.
- **Garantizar integridad transaccional** en la verificación comunitaria, mitigando condiciones de carrera y *GPS Spoofing* mediante funciones de proximidad geográfica (`ST_DWithin`).

### 4. Capa Cliente (Android)
- **Construir el cliente nativo** separando estrictamente capas (Data, Domain, Presentation).
- **Integrar el motor cartográfico MapLibre** aplicando técnicas de agregación (*Clustering*) y control de flujo espacial (*Debounce* por nivel de zoom).
- **Optimizar manipulación multimedia** mediante submuestreo (`inSampleSize`), conversión a WebP y saneamiento de metadatos EXIF en hilos secundarios (`Dispatchers.Default`).

### 5. Innovación y Ampliación (Edge AI)
- **Implementar un pipeline de inferencia visual on-device** ejecutado asíncronamente. El modelo validará preliminarmente la imagen (detección de mobiliario urbano/exteriores) antes de autorizar la solicitud de subida a AWS, optimizando el ancho de banda y mitigando la inyección de spam.

---

## 🗄️ Esquema Relacional Base (PostgreSQL + PostGIS)

El modelo de datos garantiza Tercera Forma Normal (3FN) y desacoplamiento de seguridad:

1. **`usuarios`**: Identidad y perfil (UUID, Username, Rol).
2. **`credenciales_usuario`**: Seguridad aislada (Relación 1:1, Hash, control de fuerza bruta).
3. **`spots`**: Núcleo espacial. Contiene metadatos técnicos y la columna `geometria` (Point, SRID 4326) con índice GiST.
4. **`obstaculos`**: Catálogo inmutable de tipos arquitectónicos (Stairs, Handrail, Ledge, etc.).
5. **`spots_obstaculos`**: Tabla intermedia para resolución $N:M$.
6. **`fotos_spot`**: Referencias S3 (Relación 1:N con spots), metadatos MIME y peso en bytes.
7. **`validaciones_spot`**: Máquina de estados comunitaria. Garantiza 1 voto por usuario y spot mediante restricciones UNIQUE.

---

## 🚀 Fases de Desarrollo

*   **Fase 1:** Modelado DDL (PostGIS), DER, contratos OpenAPI y arquitectura de paquetes.
*   **Fase 2:** Backend Base, JWT, aislamiento de seguridad y S3 Presigned URLs.
*   **Fase 3:** Core Android (Clean Arch + Hilt) y persistencia local (Room + StateFlow).
*   **Fase 4:** Motor Cartográfico (MapLibre), UI reactiva y validación de Mock Locations.
*   **Fase 5:** WorkManager (Sincronización Offline), QA, Edge AI y Despliegue.
