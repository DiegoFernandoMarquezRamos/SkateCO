# Especificación de Interfaz y Flujo de Pantallas (UI/UX)

**Aplicación:** SCO (`com.skateco.sco`)  
**Stack Cliente:** Android Nativo (Kotlin, Jetpack Compose, Material 3)  
**Arquitectura:** Clean Architecture + MVVM (`StateFlow`)  
**Persistencia y Red:** Room (Single Source of Truth), Retrofit/OkHttp, WorkManager

---

## VISTA-01: Autenticación y Acceso (`Login / Gateway`)

### 1. Objetivo funcional y roles

- **Objetivo:** Pantalla de entrada a la aplicación. Permite iniciar sesión o registrarse para desbloquear la creación de _spots_ y los votos comunitarios, o entrar directamente en modo lectura mediante el acceso como invitado (_"Continue as Guest"_).
- **Roles con acceso:**
  - **Usuario Invitado:** Pantalla inicial por defecto si no hay sesión guardada en el dispositivo.
  - **Skater (`USER`) / Moderador (`ADMIN`):** Solo acceden a esta vista tras cerrar sesión o si caduca su token JWT.

### 2. Componentes interactivos y trazabilidad

1. **Campo `Email` (`OutlinedTextField`):**
   - _Comportamiento:_ Valida formato de correo electrónico en tiempo real antes de habilitar el envío.
   - _Requisito cubierto:_ **RF-01** (Registro e inicio de sesión).
2. **Campo `Password` (`OutlinedTextField` con icono de visibilidad):**
   - _Comportamiento:_ Oculta los caracteres por defecto (`PasswordVisualTransformation`) permitiendo alternar su visibilidad.
   - _Requisito cubierto:_ **RF-01**, **RF-02** (Aislamiento y control de intentos fallidos en backend).
3. **Botón principal `SIGN IN` (Color acento `#CCFF00`):**
   - _Comportamiento:_ Envía las credenciales al endpoint de autenticación. Muestra un indicador de carga deshabilitando el botón mientras espera respuesta. Si el backend devuelve bloqueo temporal por 5 intentos fallidos, muestra el aviso correspondiente en un `Snackbar`.
   - _Requisito cubierto:_ **RF-01**, **RF-02**.
4. **Enlace secundario `Continue as Guest (Explore Map)` (`TextButton`):**
   - _Comportamiento:_ Inicializa una sesión anónima local (`Role.GUEST`) y navega directamente a `VISTA-02` en modo solo lectura.
   - _Requisito cubierto:_ **RF-03**, **RF-06**.

### 3. Comportamiento en modo Offline

- **Inicio de sesión:** Si no hay red, `NetworkMonitor` intercepta el intento antes de lanzar la petición HTTP y muestra el aviso _"Sin conexión a Internet. Conéctate para iniciar sesión"_.
- **Acceso como Invitado:** Totalmente operativo; carga `VISTA-02` leyendo los _spots_ almacenados en la caché local de Room.

### 4. Capturas de referencia (Figma)

- `FIG_VISTA01_LOGIN_DEFAULT.png`: Estado inicial en modo oscuro (`#121214`) con el logotipo de SCO y botón activo.
- `FIG_VISTA01_LOGIN_ERROR.png`: Estado de validación con aviso de error en credenciales.

---

## VISTA-02: Mapa Principal y Explorador (`Main Map & Explorer`)

### 1. Objetivo funcional y roles

- **Objetivo:** Pantalla principal de la aplicación. Renderiza el mapa vectorial oscuro con los _spots_ registrados, permite filtrar por tipo de obstáculo, buscar por nombre o zona y centrar la cámara en la posición GPS del usuario.
- **Roles con acceso:**
  - **Usuario Invitado:** Consulta del mapa, filtros y detalle de _spots_. Si pulsa el botón `+`, se muestra un diálogo solicitando iniciar sesión.
  - **Skater (`USER`):** Acceso completo a exploración y alta de nuevos _spots_.
  - **Moderador (`ADMIN`):** Acceso completo más acceso al panel de revisión de reportes (`VISTA-05`).

### 2. Componentes interactivos y trazabilidad

1. **Lienzo Cartográfico (`MapLibre Native SDK` con teselas vectoriales OSM oscuras):**
   - _Comportamiento:_ Al mover o hacer zoom en el mapa (con _debounce_ de 300 ms), solicita los _spots_ dentro del área visible (_Bounding Box_). Agrupa pines cercanos mediante _clustering_ dinámico según el nivel de zoom.
   - _Requisito cubierto:_ **RF-03** (Carga espacial `GiST`), **RF-04** (Clustering).
2. **Barra de Búsqueda Superior (`SearchBar`):**
   - _Comportamiento:_ Filtra _spots_ por nombre o descripción tanto en servidor como en la caché local.
   - _Requisito cubierto:_ **RF-03**.
3. **Fila horizontal de Chips de Filtro (`LazyRow` de `FilterChip`):**
   - _Elementos:_ `Todos`, `Escaleras`, `Barandilla`, `Bordillo / Cajón`, `Manual Pad`, `Plano Inclinado`, `Gap`, `Bowl` (sincronizados con la tabla `tipos_obstaculo`).
   - _Comportamiento:_ Filtra reactivamente los marcadores visibles en el mapa para mostrar solo _spots_ que contengan los obstáculos seleccionados.
   - _Requisito cubierto:_ **RF-05** (Filtrado por tipo de obstáculo).
4. **Botón flotante `Mi Ubicación` (`FloatingActionButton` secundario):**
   - _Comportamiento:_ Solicita permiso `ACCESS_FINE_LOCATION` y centra la cámara en las coordenadas GPS actuales.
   - _Requisito cubierto:_ **RF-03**.
5. **Tarjeta inferior de vista previa (`SpotPreviewCard`):**
   - _Comportamiento:_ Se despliega al tocar un pin individual. Muestra foto en miniatura, nombre del _spot_, estado de verificación y etiquetas de obstáculos. Al pulsarla abre `VISTA-03`.
   - _Requisito cubierto:_ **RF-06** (Consulta de detalle).
6. **Botón flotante principal `+ Añadir Spot` (`FAB` en `#CCFF00`):**
   - _Comportamiento:_ Abre `VISTA-04` para usuarios autenticados o lanza el aviso de inicio de sesión para invitados.
   - _Requisito cubierto:_ **RF-07**.

### 3. Comportamiento en modo Offline

- La UI observa `Flow<List<SpotEntity>>` desde **Room** (_Single Source of Truth_). Si no hay cobertura, el mapa muestra todos los _spots_ cacheados previamente junto a las teselas almacenadas en disco y un indicador superior discreto (_"Modo sin conexión"_).

### 4. Capturas de referencia (Figma)

- `FIG_VISTA02_MAP_OVERVIEW.png`: Mapa oscuro con clustering, chips de obstáculos y botón `+`.
- `FIG_VISTA02_MAP_SPOT_SELECTED.png`: Mapa con pin seleccionado y tarjeta inferior desplegada.

---

## VISTA-03: Ficha Técnica del Spot (`Spot Detail Bottom Sheet / View`)

### 1. Objetivo funcional y roles

- **Objetivo:** Mostrar la ficha completa de un _spot_ seleccionado: galería fotográfica en WebP, estado del suelo, inventario detallado de obstáculos físicos (`obstaculos_spot`), contador de validaciones comunitarias y acciones de verificación o reporte.
- **Roles con acceso:**
  - **Usuario Invitado:** Lectura completa de la ficha y botón de navegación externa. Los botones de validar o reportar piden autenticación.
  - **Skater (`USER`):** Puede validar el _spot_ por proximidad GPS, guardarlo en favoritos o reportarlo.
  - **Moderador (`ADMIN`):** Además de las acciones de usuario, dispone de controles rápidos para aprobar o archivar el _spot_.

### 2. Componentes interactivos y trazabilidad

1. **Galería de Fotografías (`HorizontalPager` + visor ampliable):**
   - _Comportamiento:_ Carga las imágenes WebP almacenadas en Object Storage mediante Coil con caché en disco.
   - _Requisito cubierto:_ **RF-06**.
2. **Desglose de Obstáculos y Terreno (`FlowRow` de etiquetas):**
   - _Comportamiento:_ Muestra el estado del suelo (`LISO`, `RUGOSO`, `AGRIETADO`) y cada elemento físico registrado en `obstaculos_spot` junto a su detalle (ej. _"Barandilla — Redonda de 6 escalones"_).
   - _Requisito cubierto:_ **RF-06**.
3. **Botón primario `Validar Spot (GPS)` (`Button` en `#CCFF00`):**
   - _Comportamiento:_ Comprueba en el dispositivo que la ubicación no procede de un proveedor simulado (`isFromMockProvider`) y envía las coordenadas al backend, donde `ST_DWithin` verifica que el skater está a menos de 100 metros del _spot_. Limita a 1 voto por usuario y _spot_.
   - _Requisito cubierto:_ **RF-11** (Validación comunitaria anti-spoofing).
4. **Botón `Cómo llegar` (`OutlinedButton`):**
   - _Comportamiento:_ Lanza un `Intent` implicito (`geo:lat,lng`) para abrir la ubicación en la app de navegación externa instalada en el teléfono.
   - _Requisito cubierto:_ **RF-06**.
5. **Botón `Reportar` (`IconButton` / menú contextual):**
   - _Comportamiento:_ Permite marcar un _spot_ como duplicado, inexistente o destruido para su revisión por moderadores.
   - _Requisito cubierto:_ **RF-12**.

### 3. Comportamiento en modo Offline

- Si el _spot_ está cacheado en Room, toda su información y fotografías descargadas se muestran sin conexión.
- El botón `Validar Spot (GPS)` queda deshabilitado con un aviso explicativo cuando no hay red, ya que la verificación de proximidad (`ST_DWithin`) y el control de voto único requieren validación transaccional en el servidor.

### 4. Capturas de referencia (Figma)

- `FIG_VISTA03_SPOT_DETAIL_MAIN.png`: Ficha desplegada con fotografía, etiquetas de obstáculos y botón de validación GPS.

---

## VISTA-04: Crear Nuevo Spot (`Create Spot Form`)

### 1. Objetivo funcional y roles

- **Objetivo:** Permitir a los skaters catalogar un nuevo _spot_ urbano adjuntando fotografías validadas localmente, fijando su posición exacta en el mapa y añadiendo uno o varios obstáculos físicos reales.
- **Roles con acceso:** Exclusivo para **Skater (`USER`)** y **Moderador (`ADMIN`)**.

### 2. Componentes interactivos y trazabilidad

1. **Selector Fotográfico con Escaneo Edge AI (`PhotoPicker` + `LiteRT / MediaPipe`):**
   - _Comportamiento:_ Permite seleccionar o capturar hasta 3 fotografías. Antes de aceptar la imagen, el modelo local **LiteRT** la analiza en segundo plano para confirmar que muestra mobiliario urbano o exterior, descartando imágenes en negro o _spam_ visual. Tras pasar el filtro, la imagen se submuestrea (`inSampleSize`), se comprime a **WebP** y se eliminan sus metadatos **EXIF**.
   - _Requisito cubierto:_ **RF-09** (Compresión WebP y limpieza EXIF), **RF-10** (Filtro Edge AI on-device).
2. **Campos `Nombre del Spot` y `Descripción` (`OutlinedTextField`):**
   - _Comportamiento:_ Validación de longitud mínima y máxima antes de permitir el guardado.
   - _Requisito cubierto:_ **RF-07**.
3. **Selector de Ubicación GPS (`MiniMap` con marcador ajustable):**
   - _Comportamiento:_ Toma las coordenadas actuales del dispositivo (bloqueando _Mock Locations_) y permite ajustar ligeramente el pin sobre el callejero.
   - _Requisito cubierto:_ **RF-07**.
4. **Selector de Estado del Suelo (`SegmentedButton`):**
   - _Elementos:_ `Liso`, `Rugoso`, `Agrietado` (mapeado al campo `estado_suelo`).
   - _Requisito cubierto:_ **RF-07**.
5. **Constructor de Obstáculos Físicos (`tipos_obstaculo` + nota breve):**
   - _Comportamiento:_ Permite añadir uno o varios elementos físicos al _spot_ seleccionando su tipo del catálogo (`Escaleras`, `Barandilla`, `Bordillo`, etc.) y añadiendo un detalle opcional (ej. _"8 escalones"_).
   - _Requisito cubierto:_ **RF-07**.
6. **Botón primario `GUARDAR SPOT` (`Button` en `#CCFF00`):**
   - _Comportamiento:_ Guarda el registro en Room, solicita la URL prefirmada al backend, sube la imagen WebP directamente a Object Storage y registra el _spot_ en PostgreSQL.
   - _Requisito cubierto:_ **RF-07**, **RF-08**, **RF-09**.

### 3. Comportamiento en modo Offline

- **Flujo Offline-First nativo:** Si no hay cobertura móvil en la calle al pulsar `GUARDAR SPOT`, la app guarda los datos en Room con estado `PENDIENTE_SYNC`, almacena los archivos WebP en `context.filesDir` y encola un trabajo en **WorkManager** (`NetworkType.CONNECTED`). El _spot_ aparece de inmediato en el mapa local del usuario con un icono de sincronización pendiente y se sube automáticamente en segundo plano al recuperar la conexión (**RF-08**).

### 4. Capturas de referencia (Figma)

- `FIG_VISTA04_CREATE_FORM.png`: Formulario completo en modo oscuro con selector de foto, chips de obstáculos y botón de guardado.

---

## VISTA-05: Perfil y Panel de Moderación (`Profile & Admin Panel`)

### 1. Objetivo funcional y roles

- **Objetivo:** Mostrar la información básica del usuario, sus _spots_ creados o guardados y el cierre de sesión. Si el token JWT incluye el rol `ADMIN`, habilita la pestaña de moderación comunitaria.
- **Roles con acceso:**
  - **Usuario Invitado:** Muestra una tarjeta informativa invitando a iniciar sesión (`VISTA-01`).
  - **Skater (`USER`):** Muestra sus _spots_ registrados, guardados y botón de cerrar sesión.
  - **Moderador (`ADMIN`):** Desbloquea la pestaña **Moderación** para auditar _spots_ reportados y gestionar el catálogo `tipos_obstaculo`.

### 2. Componentes interactivos y trazabilidad

1. **Pestañas de contenido (`TabRow`):**
   - _Elementos:_ `Mis Spots` | `Guardados` | `Moderación (Solo ADMIN)`.
   - _Requisito cubierto:_ **RF-01**, **RF-12**.
2. **Lista de Moderación (`LazyColumn` visible solo para `ADMIN`):**
   - _Comportamiento:_ Lista los _spots_ en estado `PENDIENTE` o reportados por usuarios. Cada tarjeta incluye dos acciones rápidas: **Aprobar** (cambia estado a `PUBLICADO`) y **Archivar** (cambia estado a `ARCHIVADO` ocultándolo del mapa público).
   - _Requisito cubierto:_ **RF-12** (Panel de moderación).
3. **Gestión de Catálogo (`Añadir tipo de obstáculo`, solo `ADMIN`):**
   - _Comportamiento:_ Formulario breve para insertar nuevas categorías en `tipos_obstaculo`.
   - _Requisito cubierto:_ **RF-12**.
4. **Botón `Cerrar Sesión` (`OutlinedButton`):**
   - _Comportamiento:_ Elimina el token JWT almacenado localmente y redirige a `VISTA-01`.
   - _Requisito cubierto:_ **RF-01**.

### 3. Comportamiento en modo Offline

- Las listas de `Mis Spots` y `Guardados` se consultan directamente desde Room.
- Las acciones administrativas de moderación (`Aprobar` / `Archivar`) y la edición del catálogo requieren conexión activa para mantener la consistencia en PostgreSQL.

### 4. Capturas de referencia (Figma)

- `FIG_VISTA05_ADMIN_MODERATION.png`: Vista de moderación con tarjetas de _spots_ reportados y botones de aprobar/archivar.
