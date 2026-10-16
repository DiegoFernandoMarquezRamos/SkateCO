# Anteproyecto de Fin de Ciclo (TFC - DAM): SkateCO

## 1. Abstract
Plataforma móvil nativa en Android (*Offline-First* con Room) y backend en Spring Boot para la geolocalización y catalogación comunitaria de *spots* urbanos de deslizamiento. Resuelve la indexación de infraestructura callejera no mapeada mediante consultas espaciales en PostgreSQL/PostGIS y subida de medios desacoplada en AWS S3.

---

## 2. Justificación y Motivación
La comunidad de deportes de deslizamiento urbano (*skateboarding*, *longboard*, *inline*, BMX) carece de herramientas especializadas para indexar y consultar arquitectura callejera no oficial. Las plataformas cartográficas genéricas (como Google Maps) únicamente registran instalaciones municipales (*skateparks*), ignorando el mobiliario urbano real (*spots* de calle como escaleras, barandillas o bordillos) y sus especificaciones técnicas críticas: rugosidad del pavimento, dimensiones del obstáculo, estado de conservación o nivel de afluencia. Adicionalmente, el descubrimiento y registro de estos puntos suele producirse en entornos urbanos con cobertura de red inestable o nula.

**SkateCO** nace de una motivación e interés directo por resolver esta fragmentación, aportando un **valor social y comunitario real** al digitalizar este patrimonio urbano de forma colaborativa. A nivel de ingeniería de software, el proyecto está cuidadosamente acotado para ser ejecutable de forma realista en un plazo de **3 meses** sin incurrir en costes de infraestructura (*Cloud Free Tier*), centrando su valor diferencial en el uso de tecnologías avanzadas no impartidas directamente en el ciclo (motores espaciales PostGIS, renderizado vectorial con MapLibre, arquitectura *Offline-First* con sincronización diferida y enlaces prefirmados S3).

---

## 3. Objetivos de Ingeniería (Evaluables)

### 3.1. Objetivo General
Diseñar, implementar y validar una plataforma distribuida para la localización, catalogación técnica y verificación colaborativa de infraestructura urbana apta para deportes de deslizamiento, garantizando tolerancia a fallos de conectividad mediante arquitectura *Offline-First*, consultas geoespaciales optimizadas e interfaz dedicada de administración y moderación.

### 3.2. Capa de Datos y Persistencia
- **Modelar el esquema relacional en Tercera Forma Normal (3FN)** integrando tipos espaciales PostGIS e indexación **GiST** para procesar consultas de área (*Bounding Box*) con eficiencia algorítmica $O(\log N)$.
- **Aislar credenciales y datos sensibles** en entidades independientes ($1:1$ con clave primaria compartida), mitigando fugas accidentales en el ORM (JPA) y configurando bloqueos temporales contra ataques de fuerza bruta.

### 3.3. Capa de Servicios (Backend)
- **Desarrollar una API RESTful *stateless*** asegurada mediante JWT y control de acceso basado en roles (RBAC: `USER`, `MODERATOR`, `ADMIN`).
- **Implementar subida multimedia desacoplada** vía Amazon S3 Presigned URLs, evitando cuellos de botella de entrada/salida (Zero-I/O bottleneck) y saturación de memoria RAM/Red en el servidor de aplicación.
- **Garantizar la integridad transaccional** en la verificación comunitaria de *spots*, mitigando condiciones de carrera y falsificación de ubicación (*GPS Spoofing*) mediante funciones de proximidad geográfica (`ST_DWithin`).

### 3.4. Capa Cliente (Android) y Panel de Administración
- **Construir el cliente nativo en Kotlin** aplicando una separación estricta de responsabilidades mediante *Clean Architecture* (Data, Domain, Presentation) y el patrón MVVM.
- **Integrar el motor cartográfico MapLibre** aplicando técnicas de agregación espacial (*Clustering*) y control de flujo de peticiones (*Debounce* según eventos de cámara y nivel de zoom).
- **Desarrollar un Panel de Administración y Moderación integrado** accesible según los permisos del token JWT, permitiendo auditar *spots* reportados, gestionar el catálogo maestro de obstáculos y moderar usuarios.
- **Optimizar la manipulación multimedia en el dispositivo** mediante submuestreo (`inSampleSize`), compresión al formato WebP y saneamiento de metadatos EXIF de privacidad en hilos secundarios (`Dispatchers.Default`).

### 3.5. Objetivo Evolutivo / Ampliación Opcional (Edge AI)
- *(Supeditado a la finalización íntegra del MVP en las Fases 1 a 4)*: **Implementar un pipeline de inferencia visual *on-device*** ejecutado asíncronamente (LiteRT / MediaPipe). El modelo validará preliminarmente la fotografía en el teléfono (detección de entorno exterior / mobiliario urbano) antes de solicitar la URL prefirmada a AWS, ahorrando ancho de banda y previniendo la inyección de spam visual.

---

## 4. Arquitectura y Stack Tecnológico

El sistema se rige por principios de *Clean Architecture*, segregación de interfaces (SOLID) e integridad transaccional ACID, diseñado para operar en entornos de conectividad degradada y bajo restricciones estrictas de recursos (*Cloud Free Tier*).

### 4.1. Cliente Móvil (Android)
- **Lenguaje:** Kotlin.
- **Arquitectura:** Clean Architecture + MVVM con interfaz reactiva.
- **Persistencia Local (*Offline-First*):** Room (SQLite) como *Single Source of Truth* (SSOT) expuesta mediante flujos reactivos (`StateFlow` / `Coroutines`).
- **Sincronización Diferida:** Jetpack WorkManager para garantizar la subida eventual de creaciones y votos realizados sin conexión.
- **Cartografía:** MapLibre Native SDK + teselas vectoriales de OpenStreetMap (OSM).
- **Inyección de Dependencias:** Dagger Hilt.
- **Edge AI (Alcance evolutivo):** LiteRT / MediaPipe para filtro visual anti-spam en local.

### 4.2. Backend y Servicios Cloud
- **Framework:** Spring Boot 3 (Java 21).
- **Seguridad:** Spring Security 6 con autenticación JWT (*Stateless*) y hashing robusto de contraseñas (Argon2id / BCrypt).
- **Base de Datos:** PostgreSQL con extensión geoespacial **PostGIS**.
- **Gestión Multimedia:** Almacenamiento de objetos en AWS S3 mediante patrón *Presigned URLs*.
- **Control de Esquema (Migraciones):** Flyway para el versionado determinista del DDL.

---

## 5. Esquema Relacional Base (PostgreSQL + PostGIS)

El modelo de datos garantiza la Tercera Forma Normal (3FN) y el aislamiento de vectores de seguridad:

1. **`usuarios`**: Identidad pública y perfil (UUID, Username, Email, Rol RBAC, Fecha de registro).
2. **`credenciales_usuario`**: Seguridad aislada (Relación $1:1$ con `usuarios`, Hash de contraseña, contador de intentos fallidos y *timestamp* de bloqueo temporal).
3. **`spots`**: Núcleo espacial del dominio. Contiene metadatos técnicos (título, descripción, estado de suelo, riesgo policial, estado de verificación) y la columna espacial `geometria` (`Geometry(Point, 4326)`) indexada mediante árbol **GiST**.
4. **`obstaculos`**: Catálogo maestro inmutable de elementos arquitectónicos (*Stairs*, *Handrail*, *Ledge*, *Manual Pad*, *Bank*, etc.).
5. **`spots_obstaculos`**: Tabla asociativa para resolver la relación $N:M$ entre *spots* y tipos de obstáculos presentes.
6. **`fotos_spot`**: Referencias a objetos en S3 (Relación $1:N$ con `spots`), clave del objeto, metadatos MIME y tamaño en bytes.
7. **`validaciones_spot`**: Máquina de estados comunitaria y auditoría de proximidad. Garantiza 1 voto o reporte por usuario y *spot* mediante restricción compuesta `UNIQUE (usuario_id, spot_id)`.

---

## 6. Metodología de Trabajo, Herramientas y Calidad (QA)

Para asegurar la trazabilidad del desarrollo, la mantenibilidad del código y el cumplimiento de los estándares de ingeniería evaluados en el ciclo, se aplicará el siguiente marco de trabajo:

- **Metodología Ágil:** Gestión del proyecto basada en **Kanban** (mediante **GitHub Projects / Trello**), estructurada en *sprints* de dos semanas con entregables funcionales e incrementales.
- **Herramientas de Diseño y Modelado:**
  - *UI/UX:* Diseño de prototipos de alta fidelidad y flujos de navegación en **Figma**.
  - *Base de Datos y Arquitectura:* Diagramas Entidad-Relación (DER) y diagramas de arquitectura C4/UML.
  - *Contratos de Interfaz:* Diseño *API-First* documentado e interactivo mediante **OpenAPI 3.0 / Swagger UI**.
- **Control de Versiones:** Uso de **Git** alojado en GitHub bajo flujo **Feature Branching** (`main`, `develop`, `feature/*`, `fix/*`) aplicando el estándar **Conventional Commits** (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`) para mantener un historial auditable y semántico.
- **Estrategia de Testing:**
  - *Pruebas Unitarias:* Validación de reglas de negocio y casos de uso con **JUnit 5** y **MockK** / **Mockito**.
  - *Pruebas de Integración:* Validación de consultas espaciales PostGIS y repositorios en el backend utilizando contenedores efímeros con **Testcontainers**, además de pruebas de contratos REST con `MockMvc`.
- **Integración Continua (CI/CD):** Configuración de un *pipeline* automatizado en **GitHub Actions** que ejecutará la compilación del proyecto, validación de migraciones Flyway y batería de tests en cada *Pull Request*.

---

## 7. Planificación Temporal a Alto Nivel (12 Semanas / 3 Meses)

El calendario de trabajo se divide en 5 fases secuenciales diseñadas para llegar a las tutorías de seguimiento y a la defensa de junio con un producto sólido y probado. *(En la memoria final se adjuntará el diagrama de Gantt detallado).*

| Fase | Semanas | Hitos y Entregables Principales |
| :--- | :---: | :--- |
| **Fase 1: Diseño y Fundamentación** | Semanas 1 - 2 | Prototipado UI en Figma, modelado DDL espacial (PostGIS), Diagrama E/R, definición de contratos OpenAPI y configuración de repositorios y CI en GitHub. |
| **Fase 2: Backend Core y Cloud** | Semanas 3 - 5 | Desarrollo de API en Spring Boot 3, migraciones Flyway, seguridad JWT + Argon2id, consultas espaciales GiST (`ST_DWithin`, *Bounding Box*) e integración con AWS S3 Presigned URLs. Tests con Testcontainers. |
| **Fase 3: Cliente Android Base** | Semanas 6 - 7 | Estructura *Clean Architecture* + Hilt, persistencia local *Offline-First* con Room + `StateFlow`, cliente HTTP (Retrofit/OkHttp) con interceptores de autenticación JWT. |
| **Fase 4: Cartografía, UI y Admin** | Semanas 8 - 9 | Integración de MapLibre SDK, *clustering* de marcadores, compresión WebP y limpieza EXIF, validación de *Mock Locations* y vistas del **Panel de Administración/Moderación**. |
| **Fase 5: Sincronización, QA y Defensa** | Semanas 10 - 12 | Sincronización diferida con WorkManager, módulo opcional de Edge AI, pruebas integrales de sistema, despliegue en la nube, redacción de la Memoria técnica y grabación del vídeo de presentación (máx. 7 min). |

---

## 8. Declaración y Protocolo de Uso de Inteligencia Artificial

En cumplimiento con la normativa académica y la rúbrica de evaluación del TFC, se declara de forma transparente el protocolo de uso de herramientas de IA generativa (asistentes LLM) durante el ciclo de vida del proyecto:

1. **Ámbitos de Aplicación:** Asistencia en la generación de código repetitivo (*boilerplate* de DTOs y mapeadores), consulta de sintaxis específica de funciones geoespaciales en PostGIS, redacción de casos de prueba unitarios y revisión ortotipográfica de la documentación.
2. **Auditoría, Comprensión y Responsabilidad Técnica:** Ningún fragmento de código generado por IA se incorpora directamente a la rama principal sin pasar por un proceso estricto de verificación manual. Todo el código es refactorizado para cumplir con la arquitectura del proyecto, auditado frente a vulnerabilidades de seguridad (estándares OWASP, prevención de inyección SQL y fugas de datos sensibles) y validado mediante pruebas automatizadas. El autor asume la responsabilidad técnica total sobre el diseño, implementación y defensa de cada línea de código del sistema.

---

## 9. Referencias Bibliográficas y Webgrafía (Formato APA 7.ª Ed.)

- Amazon Web Services. (2025). *Uploading objects with presigned URLs - Amazon Simple Storage Service User Guide*. AWS Documentation. https://docs.aws.amazon.com/AmazonS3/latest/userguide/PresignedUrlUploadObject.html
- Google Developers. (2025). *Build an offline-first app | Android App Architecture*. Android Developers. https://developer.android.com/topic/architecture/data-layer/offline-first
- Google Developers. (2025). *Schedule tasks with WorkManager*. Android Developers. https://developer.android.com/topic/libraries/architecture/workmanager
- MapLibre Organization. (2025). *MapLibre Native for Android Documentation*. MapLibre. https://maplibre.org/maplibre-native/android/api/
- Martin, R. C. (2017). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.
- Obe, R. O., & Hsu, L. S. (2021). *PostGIS in Action* (3.ª ed.). Manning Publications.
- The PostgreSQL Global Development Group. (2025). *PostgreSQL 16 Documentation: GiST Indexes*. PostgreSQL. https://www.postgresql.org/docs/current/gist.html
- VMware, Inc. (2025). *Spring Boot Reference Documentation (v3.x)*. Spring.io. https://docs.spring.io/spring-boot/index.html
