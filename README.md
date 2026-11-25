<div align="center">

# SkateCO

**`spots urbanos · skate · Android`**

[![Android CI](https://github.com/DiegoFernandoMarquezRamos/SkateCO/actions/workflows/android-ci.yml/badge.svg?branch=main)](https://github.com/DiegoFernandoMarquezRamos/SkateCO/actions/workflows/android-ci.yml)
![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)
![Estado](https://img.shields.io/badge/estado-prototipo%20en%20desarrollo-orange)

</div>

```bash
$ whoami
SkateCO — proyecto de fin de ciclo para descubrir y catalogar spots urbanos.

$ git status --short
Android: prototipo de interfaz en Kotlin y Jetpack Compose
Base de datos: esquema inicial PostgreSQL + PostGIS
Backend: planificado; todavía no integrado

$ ./gradlew test
```

## ¿Qué es SkateCO?

Una aplicación Android para explorar y documentar lugares aptos para skate y otros deportes urbanos. El repositorio contiene actualmente un prototipo de interfaz, navegación entre pantallas y un primer diseño SQL; la autenticación real, la persistencia, la sincronización y los servicios backend siguen en desarrollo.

## Lo que encontrarás

- Pantallas de acceso, mapa, detalle de spot, creación y perfil/moderación.
- Navegación Android con Jetpack Compose y Material 3.
- Esquema SQL inicial con PostgreSQL/PostGIS y datos de ejemplo para Madrid.
- CI en GitHub Actions que ejecuta los tests Android.

## Stack actual

| Área | Tecnología |
| --- | --- |
| Aplicación | Kotlin, Android, Jetpack Compose, Material 3 |
| Navegación | Android Navigation Compose |
| Modelo de datos inicial | PostgreSQL, PostGIS, SQL |
| Calidad | Gradle, JUnit, GitHub Actions |

### Arquitectura prevista

El anteproyecto contempla un backend con Spring Boot y Java, además de funcionalidades offline-first y servicios geoespaciales. Son objetivos de desarrollo, no componentes ya integrados en esta versión.

## Ejecutar la aplicación

1. Abre `Aplicacion/` en Android Studio con JDK 17 y el SDK de Android requerido por Gradle.
2. Ejecuta el módulo `app` en un emulador o dispositivo compatible (Android 12 / API 31 o superior).
3. Para ejecutar los tests desde terminal:

   ```bash
   cd Aplicacion
   ./gradlew test
   ```

El esquema inicial de base de datos está en `base_de_datos_SCO.sql` y requiere PostgreSQL con las extensiones `uuid-ossp` y `postgis`.

## Estado del proyecto

Este repositorio es un proyecto académico en evolución. La interfaz sirve como prototipo; las acciones de autenticación y guardado no sustituyen todavía a un backend ni a persistencia real.

## Licencia

Aún no se ha definido una licencia para reutilización del código.
