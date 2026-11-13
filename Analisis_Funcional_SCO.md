# DOCUMENTO DE ANÁLISIS FUNCIONAL
## Proyecto: SCO (Skate Spot Finder)
**Pila tecnológica frontend:** Android (Jetpack Compose, Material 3)  
**Arquitectura recomendada:** Clean Architecture + MVVM/MVI con Unidirectional Data Flow (UDF)  
**Gestión de datos:** Room Database (Caché Offline-First), Retrofit / Ktor, WorkManager (Sincronización en segundo plano)

---

# VISTA-01: Autenticación y Acceso (Login / Gateway)
* **Figma Node ID:** `1:3` (`SCO Skate Spot Finder`)

### 1. Nombre de la pantalla e ID
`VISTA-01: Autenticación y Acceso (Login / Gateway)`

### 2. Objetivo funcional y roles de acceso
* **Objetivo funcional:** Actuar como pasarela de bienvenida y autenticación del sistema. Permite a los skaters identificarse mediante credenciales (email y contraseña) para acceder a sus spots guardados, valoraciones y funciones de comunidad. Proporciona una vía de bypass explícita (*"Continue as Guest"*) que permite explorar el mapa de spots en modo lectura sin forzar el registro inmediato.
* **Roles con acceso:**
  * **Invitado (Guest):** Acceso total. Pantalla inicial por defecto si no existe token de sesión persistente en el dispositivo.
  * **USER:** Solo visible tras cierre explícito de sesión (*Sign Out*) o revocación de token.
  * **ADMIN:** Idéntico a USER; los permisos de administración se resuelven a nivel de claims JWT tras autenticarse.

### 3. Desglose de componentes interactivos
1. **Input "EMAIL" (`OutlinedTextField`):**
   * *Evento:* `onValueChange = { viewModel.onEmailChange(it) }`
   * *RF cubierto:* **RF-01: Validación de formato de email.** Comprueba sintaxis RFC 5322. Configura `KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)`.
2. **Input "PASSWORD" (`OutlinedTextField`):**
   * *Evento:* `onValueChange = { viewModel.onPasswordChange(it) }`
   * *RF cubierto:* **RF-02: Entrada de credenciales segura.** Aplica `visualTransformation = PasswordVisualTransformation()`. `KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)`.
3. **Toggle de Visibilidad de Password (`IconButton` en trailing icon):**
   * *Evento:* `onClick = { viewModel.togglePasswordVisibility() }`
   * *RF cubierto:* **RF-02.1: Alternancia de máscara de contraseña.** Alterna entre `VisualTransformation.None` y `PasswordVisualTransformation()`.
4. **Casilla "Keep me signed in" (`Checkbox` + `Text`):**
   * *Evento:* `onCheckedChange = { viewModel.onKeepSignedInChange(it) }`
   * *RF cubierto:* **RF-03: Persistencia de sesión prolongada.** Define si el Refresh Token se guarda en `EncryptedSharedPreferences` / DataStore para auto-login.
5. **Enlace "Forgot password?" (`TextButton`):**
   * *Evento:* `onClick = { navController.navigate(AuthDestinations.ForgotPassword) }`
   * *RF cubierto:* **RF-04: Recuperación de credenciales.** Redirige al flujo de envío de correo de restablecimiento.
6. **Botón Principal "SIGN IN ->" (`Button` solid lime `#CCFF00`):**
   * *Evento:* `onClick = { viewModel.signIn() }`
   * *RF cubierto:* **RF-05: Autenticación de usuario.** Envía credenciales al servicio de identidad. Deshabilita el botón durante la carga (`enabled = !uiState.isLoading`) y muestra `CircularProgressIndicator`. En éxito, navega a `VISTA-02`; en error, emite `SnackbarHostState.showSnackbar()`.
7. **Enlace Secundario "Continue as Guest (Explore Map)" (`TextButton`):**
   * *Evento:* `onClick = { viewModel.continueAsGuest(); navController.navigate(AppDestinations.MainMap) }`
   * *RF cubierto:* **RF-06: Modo Explorador Invitado.** Inicializa sesión local anónima en Room/DataStore (`Role.GUEST`) permitiendo consulta de mapa sin token remoto.

### 4. Comportamiento en modo Offline
* **Intento de Sign In:** Se intercepta con `NetworkMonitor`. Si no hay red, la petición HTTP se aborta inmediatamente sin loaders infinitos y se expone un `Snackbar`: *"Sin conexión a Internet. Conéctate a una red móvil o Wi-Fi para iniciar sesión"*.
* **Acceso como Invitado:** Totalmente operativo. Permite acceder a `VISTA-02` aprovechando la base de datos local (Room) con los spots precacheados.

### 5. Capturas a exportar de Figma
* `FIG_VISTA01_LOGIN_DEFAULT.png`: Vista en estado inicial con placeholders y botón activo.
* `FIG_VISTA01_LOGIN_ERROR.png`: Vista con borde de error en input email y mensaje de validación.

---

# VISTA-02: Mapa Principal y Explorador de Spots (Main Map & Explorer)

### 1. Nombre de la pantalla e ID
`VISTA-02: Mapa Principal y Explorador de Spots (Main Map & Explorer)`

### 2. Objetivo funcional y roles de acceso
* **Objetivo funcional:** Constituir el núcleo visual y operativo de la aplicación. Renderiza un mapa interactivo (Google Maps / Mapbox) con marcadores georreferenciados (pines) de spots de skate. Permite filtrar por tipología de terreno, buscar por nombre o zona geográfica, centrar en la ubicación del usuario mediante GPS y acceder al detalle de cada spot o a la creación de uno nuevo.
* **Roles con acceso:**
  * **Invitado (Guest):** Acceso de solo lectura al mapa, búsqueda y filtros. Si intenta pulsar el botón de crear spot (+), el sistema dispara un diálogo modal solicitando registro/login.
  * **USER:** Acceso total (lectura del mapa, filtros, interacción con marcadores y creación de nuevos spots mediante el botón flotante).
  * **ADMIN:** Acceso total, con indicador visual en pines de spots que están pendientes de moderación o reportados.

### 3. Desglose de componentes interactivos
1. **Lienzo del Mapa Interactivo (`GoogleMap` / `MarkerComposable` con Clustering):**
   * *Evento:* `onMapClick = { viewModel.dismissSpotPreview() }`, `onMarkerClick = { spot -> viewModel.selectSpot(spot) }`, `onCameraMove = { cameraPosition -> viewModel.onViewportChanged(cameraPosition) }`
   * *RF cubierto:* **RF-07: Renderizado Geoespacial y Agrupamiento (Clustering).** Despliega pines personalizados con icono de skate. Al tocar un pin individual, expande un card inferior (`SpotPreviewCard`). Al mover la cámara, actualiza la consulta de coordenadas (bounding box).
2. **Barra de Búsqueda Superior (`DockedSearchBar` / `TextField` con icono de lupa):**
   * *Evento:* `onQueryChange = { viewModel.searchSpots(it) }`, `onSearch = { viewModel.executeSearch() }`
   * *RF cubierto:* **RF-08: Búsqueda Semántica de Spots.** Filtra por nombre, ciudad, barrio o características (ej. "Macedonia", "Plaza Colón", "manual pad"). Incluye debounce de 300ms.
3. **Fila Horizontal de Chips de Filtro (`LazyRow` de `FilterChip`):**
   * *Elementos:* Chips: `Todos`, `Skatepark`, `Street`, `Plaza / Ledge`, `Bowl`, `DIY`, `Cubierto (Indoor)`.
   * *Evento:* `onClick = { viewModel.toggleFilter(filterType) }`
   * *RF cubierto:* **RF-09: Filtrado por Tipología de Terreno.** Modifica la lista reactiva de marcadores en el mapa en base a las categorías seleccionadas (`Set<SpotCategory>`).
4. **Botón Flotante "Mi Ubicación" (`FloatingActionButton` secundario con `Icons.Default.MyLocation`):**
   * *Evento:* `onClick = { locationTracker.requestCurrentLocation { latLng -> cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f)) } }`
   * *RF cubierto:* **RF-10: Centrado GPS del Dispositivo.** Gestiona los permisos en tiempo de ejecución (`ACCESS_FINE_LOCATION`). Si están denegados, solicita permisos con `rememberLauncherForActivityResult`.
5. **Tarjeta Inferior de Previsualización (`SpotPreviewCard` / `Card` deslizante animada):**
   * *Elementos:* Miniatura de foto (`AsyncImage`), Nombre del spot, Distancia relativa (ej. "850 m"), Puntuación en estrellas (ej. "4.8 ★") y chips de etiquetas rápidas.
   * *Evento:* `onClick = { navController.navigate("spot_detail/${spot.id}") }`
   * *RF cubierto:* **RF-11: Vista Previa y Enrutamiento a Detalle.** Despliega información resumida antes de cargar la pantalla completa de detalle.
6. **Botón Flotante de Acción Primaria "Añadir Spot" (`FloatingActionButton` en `#CCFF00` con `Icons.Default.Add`):**
   * *Evento:* `onClick = { if (isGuest) showLoginRequiredDialog() else navController.navigate("create_spot") }`
   * *RF cubierto:* **RF-12: Inicio de Registro de Nuevo Spot.** Abre el formulario de creación si el rol es USER o ADMIN. Si es Invitado, bloquea la acción y ofrece iniciar sesión.

### 4. Comportamiento en modo Offline
* **Caché de Mapa y Teselas:** Si el dispositivo pierde la conexión, el mapa renderiza las teselas gráficas almacenadas en la caché local del SDK de mapas.
* **Consulta de Spots Locales:** El repositorio de datos aplica la estrategia *Single Source of Truth* (SSOT): la UI consume `Flow<List<SpotEntity>>` desde Room Database. Todos los spots consultados previamente se muestran en el mapa sin degradación visual.
* **Banner de Estado:** Se sobrepone un chip o barra superior atenuada: *"Modo sin conexión - Mostrando spots guardados localmente"*.
* **Búsqueda restringida:** La barra de búsqueda ejecuta consultas locales mediante SQLite `LIKE %query%` sobre los spots cacheados, inhabilitando la geocodificación remota.

### 5. Capturas a exportar de Figma
* `FIG_VISTA02_MAP_OVERVIEW.png`: Vista completa con el mapa oscuro, pines verdes de spots y chips de filtro superiores.
* `FIG_VISTA02_MAP_SPOT_SELECTED.png`: Vista con un pin seleccionado y la tarjeta inferior `SpotPreviewCard` desplegada.

---

# VISTA-03: Detalle del Spot (Spot Detail View)

### 1. Nombre de la pantalla e ID
`VISTA-03: Detalle del Spot (Spot Detail View)`

### 2. Objetivo funcional y roles de acceso
* **Objetivo funcional:** Ofrecer la ficha técnica completa y multimedia de un skate spot específico. Presenta una galería fotográfica/vídeo, descripción del terreno (calidad del suelo, nivel de seguridad/iluminación, presencia de seguridad/policía), lista detallada de obstáculos (rails, ledges, stairs, gaps), puntuaciones de la comunidad, botón de navegación paso a paso externa (Google Maps) y acciones de interacción (guardar en favoritos, valorar o reportar).
* **Roles con acceso:**
  * **Invitado (Guest):** Puede ver toda la información, galería, mapa y reseñas. Si pulsa "Guardar en favoritos" o "Dejar Reseña", se le solicita login.
  * **USER:** Acceso total. Puede marcar/desmarcar favoritos, subir fotos adicionales, valorar el spot y emitir reportes.
  * **ADMIN:** Acceso total con controles adicionales: botón de "Editar Spot", "Ocultar Spot" o "Aprobar Moderación".

### 3. Desglose de componentes interactivos
1. **Carrusel de Imágenes y Vídeos de Cabecera (`HorizontalPager` con indicador de puntos):**
   * *Evento:* Deslizamiento táctil horizontal (`scroll`). Click en imagen para abrir visor a pantalla completa con zoom (`ZoomableImageModal`).
   * *RF cubierto:* **RF-13: Inspección Multimedia del Spot.** Carga imágenes optimizadas con Coil (`AsyncImage`) mostrando placeholder de esqueleto (*shimmer*).
2. **Botón Flotante "Favorito / Guardar" (`FilledIconButton` circular con `Icons.Default.Bookmark` o `Favorite`):**
   * *Evento:* `onClick = { viewModel.toggleFavorite(spotId) }`
   * *RF cubierto:* **RF-14: Gestión de Spots Guardados.** Actualiza inmediatamente la UI de forma optimista. Si es Invitado, lanza diálogo de autenticación requerida.
3. **Botón de Acción "Cómo llegar / Abrir en Navegador" (`Button` primario con icono de navegación):**
   * *Evento:* `onClick = { val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${spot.lat},${spot.lng}")); context.startActivity(intent) }`
   * *RF cubierto:* **RF-15: Enrutamiento Externo GPS.** Lanza un intent implícito a Google Maps, Waze o app de transporte instalada.
4. **Matriz de Badges de Obstáculos y Terreno (`FlowRow` de `AssistChip` / `SuggestionChip`):**
   * *Elementos:* Chips: `Suelo: Mármol liso`, `Bordillos encerados`, `Escaleras 5-set`, `Barandilla redonda`, `Iluminación nocturna: Sí`, `Bust / Policía: Bajo`.
   * *Evento:* No disparan evento destructivo; tooltip informativo al mantener pulsado.
   * *RF cubierto:* **RF-16: Ficha Técnica de Obstáculos.** Permite al skater evaluar si el spot se adapta a su nivel técnico o modalidad de patinaje.
5. **Sección de Valoraciones y Reseñas Comunitarias (`LazyColumn` anidado con botón "Añadir Opinión"):**
   * *Evento:* `onClick = { viewModel.openAddReviewDialog() }`
   * *RF cubierto:* **RF-17: Sistema de Reseñas y Puntuación.** Despliega diálogo modal con selector de 1 a 5 estrellas, campo de texto y tags de estado actual del spot (ej. *"Suelo mojado"*, *"Bordillo roto"*).
6. **Botón Secundario de Opciones / Menú Tres Puntos (`DropdownMenu`):**
   * *Evento:* Opciones: *"Compartir spot"*, *"Reportar problema / Spot destruido"*, *(Solo ADMIN)* *"Editar atributos"*.
   * *RF cubierto:* **RF-18: Moderación Comunitaria y Difusión.** Dispara el `ShareCompat.IntentBuilder` del sistema o el formulario de reporte de moderación.

### 4. Comportamiento en modo Offline
* **Disponibilidad:** Si el spot fue visitado previamente o guardado en favoritos, toda la ficha técnica (textos, coordenadas, especificaciones) se carga desde Room.
* **Imágenes cacheadas:** Coil sirve las imágenes almacenadas en el disco local (`DiskCache`). Las fotos no descargadas muestran un icono de fallo amigable con aviso de falta de conexión.
* **Mutaciones sin red (Favoritos):** El botón "Favorito" soporta *Optimistic UI*: la base de datos local actualiza el flag `isFavorite = true` y encola una tarea en `WorkManager` con `Constraints(NetworkType.CONNECTED)` para sincronizar la llamada POST/DELETE al recuperar cobertura.
* **Navegación GPS:** El botón "Cómo llegar" sigue operativo porque las coordenadas locales se envían a la app externa de mapas.

### 5. Capturas a exportar de Figma
* `FIG_VISTA03_SPOT_DETAIL_MAIN.png`: Parte superior con carrusel de fotos, título, badges de suelo y botón "Cómo llegar".
* `FIG_VISTA03_SPOT_DETAIL_REVIEWS.png`: Scroll inferior con la sección de comentarios, estrellas de valoración y ficha técnica de obstáculos.

---

# VISTA-04: Registrar / Añadir Nuevo Spot (Create Spot Form)

### 1. Nombre de la pantalla e ID
`VISTA-04: Registrar / Añadir Nuevo Spot (Create Spot Form)`

### 2. Objetivo funcional y roles de acceso
* **Objetivo funcional:** Permitir a los miembros de la comunidad registrar y mapear un nuevo skate spot no catalogado. Incluye captura/subida de evidencias fotográficas, fijación milimétrica de coordenadas sobre un mini-mapa interactivo, tipificación de obstáculos, calidad del suelo y advertencias de seguridad para el resto de patinadores.
* **Roles con acceso:**
  * **Invitado (Guest):** Acceso bloqueado. Esta vista requiere obligatoriamente sesión activa para asociar la autoría del spot a un usuario.
  * **USER:** Acceso estándar. Los spots registrados por usuarios quedan en estado `PENDING_REVIEW` o se publican directamente con marca comunitaria.
  * **ADMIN:** Acceso con privilegios de aprobación inmediata (`STATUS_VERIFIED`) y asignación de tags especiales (Spot Oficial / Skatepark Municipal).

### 3. Desglose de componentes interactivos
1. **Selector de Imágenes / Media Picker (`Card` punteada con icono de cámara y botón "+ Añadir Fotos"):**
   * *Evento:* `onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }`
   * *RF cubierto:* **RF-19: Carga de Archivos Multimedia.** Soporta el `PhotoPicker` moderno de Android (sin permisos invasivos de galería). Permite seleccionar hasta 5 fotos y reordenarlas o eliminarlas mediante icono de cruz (`x`).
2. **Campo de Texto "Nombre del Spot" (`OutlinedTextField`):**
   * *Evento:* `onValueChange = { viewModel.onSpotNameChange(it) }`
   * *RF cubierto:* **RF-20: Nomenclatura del Spot.** Campo obligatorio (mínimo 3 caracteres, máximo 50).
3. **Mini-Selector de Ubicación con Pin (`Box` con vista estática de mapa y botón "Ajustar en mapa"):**
   * *Evento:* `onClick = { viewModel.openLocationPickerMap() }`
   * *RF cubierto:* **RF-21: Georreferenciación Precisa.** Por defecto toma la ubicación GPS actual del dispositivo (`lat`, `lng`). Al pulsar, abre un mapa modal a pantalla completa con retícula central para ajustar el punto exacto.
4. **Selector de Categoría Principal (`ExposedDropdownMenuBox`):**
   * *Elementos:* Opciones: `Plaza / Calle`, `Skatepark`, `DIY (Hecho a mano)`, `Bowl / Rampa`, `Downhill / Cuesta`.
   * *Evento:* `onOptionSelected = { viewModel.selectCategory(it) }`
   * *RF cubierto:* **RF-22: Categorización Estructural.** Asigna la taxonomía base del spot.
5. **Selector Múltiple de Obstáculos Disponibles (`LazyRow` / `FlowRow` con `FilterChip` seleccionables):**
   * *Elementos:* Chips: `Bordillo (Ledge)`, `Barandilla plana`, `Barandilla bajada (Handrail)`, `Escaleras`, `Manual Pad`, `Pirámide`, `Wallride`, `Transición / Quarter`.
   * *Evento:* `onClick = { viewModel.toggleObstacle(obstacleId) }`
   * *RF cubierto:* **RF-23: Etiquetado de Elementos Patinables.** Permite selección múltiple de los elementos reales del lugar.
6. **Slider de Nivel de "Bust" / Seguridad Policial (`Slider` de 1 a 5 con indicador de color):**
   * *Evento:* `onValueChange = { viewModel.setBustLevel(it) }`
   * *RF cubierto:* **RF-24: Nivel de Tolerancia Policial / Seguridad privada.** 1 = Spot libre/sin problemas (Verde), 3 = Te echan tras un rato (Ámbar), 5 = Multa inmediata o seguridad agresiva (Rojo).
7. **Botón Principal "PUBLICAR SPOT" (`Button` solid lime `#CCFF00` con texto Black Bold):**
   * *Evento:* `onClick = { viewModel.submitSpot() }`
   * *RF cubierto:* **RF-25: Envío y Validación del Formulario.** Valida que existan: al menos 1 foto, nombre válido, categoría y coordenadas. Si faltan datos, activa `isError = true` en los campos correspondientes. Si es válido, sube imágenes a almacenamiento (Cloud Storage / S3) y registra el registro en la API.

### 4. Comportamiento en modo Offline
* **Borrador Local (Draft Auto-Save):** Toda la información introducida se almacena automáticamente en Room bajo un registro de tipo `SpotDraftEntity`. Si la app se cierra o pierde la red durante la edición, el skater no pierde sus datos.
* **Cola de Publicación Diferida:** Si el usuario pulsa "PUBLICAR SPOT" sin cobertura:
  1. La app almacena las fotos comprimidas en el almacenamiento interno de la app (`context.filesDir`).
  2. Encola un `OneTimeWorkRequest` en `WorkManager` con restricción de red requerida.
  3. Muestra una notificación local al usuario: *"Spot guardado localmente. Se publicará automáticamente en cuanto recuperes conexión"*.
  4. Redirige al mapa mostrando el nuevo spot con un indicador visual de *"Pendiente de sincronizar"*.

### 5. Capturas a exportar de Figma
* `FIG_VISTA04_CREATE_FORM_TOP.png`: Sección superior con contenedor de carga de fotos, nombre del spot y selector de categoría.
* `FIG_VISTA04_CREATE_FORM_OBSTACLES.png`: Sección inferior con chips de selección de obstáculos, slider de bust y botón "PUBLICAR SPOT".

---

# VISTA-05: Perfil del Skater y Spots Guardados (Profile & Bookmarks)

### 1. Nombre de la pantalla e ID
`VISTA-05: Perfil del Skater y Spots Guardados (Profile & Bookmarks)`

### 2. Objetivo funcional y roles de acceso
* **Objetivo funcional:** Ofrecer el panel personal del usuario. Muestra el avatar del skater, nombre de usuario, reputación dentro de la comunidad (puntos SCO por spots añadidos y votos recibidos), pestañas organizadas para acceder a spots guardados en favoritos, spots creados por el usuario y configuración de la cuenta (edición de perfil, preferencias de notificaciones y cierre de sesión).
* **Roles con acceso:**
  * **Invitado (Guest):** La vista muestra un estado de llamada a la acción (*Login CTA State*): un banner explicativo *"Crea una cuenta o inicia sesión para guardar spots favoritos y registrar tus descubrimientos"* con botones hacia `VISTA-01`.
  * **USER:** Acceso a su propio perfil y listas de spots.
  * **ADMIN:** Acceso a su perfil, con insignia visible *"ADMINISTRADOR"* y acceso a una sección de *"Panel de Moderación de Spots"*.

### 3. Desglose de componentes interactivos
1. **Cabecera de Perfil (Avatar `AsyncImage` circular + Botón Editar):**
   * *Evento:* `onClick = { navController.navigate("profile/edit") }`
   * *RF cubierto:* **RF-26: Visualización y Edición de Identidad.** Muestra imagen de perfil, biografía corta, stance (Goofy/Regular) y ciudad base.
2. **Contador de Estadísticas del Skater (`Row` con 3 métricas clave):**
   * *Elementos:* `[ Spots Creados ]`, `[ Favoritos ]`, `[ Nivel de Reputación / Puntos ]`.
   * *Evento:* Click en cada métrica filtra directamente la pestaña correspondiente inferior.
   * *RF cubierto:* **RF-27: Métricas de Contribución Comunitaria.** Incentiva el mapeo colaborativo mediante gamificación.
3. **Pestañas de Navegación de Listas (`TabRow` con `Tab` interactivos):**
   * *Elementos:* Tab 1: *"Guardados / Favoritos"*, Tab 2: *"Mis Spots Registrados"*, Tab 3 *(Solo ADMIN)*: *"Pendientes de Aprobación"*.
   * *Evento:* `onClick = { viewModel.selectTab(index) }`
   * *RF cubierto:* **RF-28: Segmentación de Contenido Personal.** Alterna la vista entre la colección de spots guardados para futuras sesiones y los spots subidos por el propio usuario.
4. **Lista Vertical de Spots (`LazyColumn` con tarjetas `SpotSummaryCard`):**
   * *Elementos:* Cada tarjeta contiene: foto, nombre, tipo de obstáculo principal, distancia y botón rápido de eliminar de favoritos (`IconButton` papelera o bookmark relleno).
   * *Evento:* Click en la tarjeta navega a `VISTA-03: Detalle del Spot`. Click en el icono de favorito dispara eliminación inmediata con opción de deshacer en `Snackbar`.
   * *RF cubierto:* **RF-29: Gestión de Listas de Spots.** Renderizado optimizado con reciclaje de vistas (`key = { spot.id }`).
5. **Botón de Ajustes y Cerrar Sesión (`IconButton` con engranaje en TopAppBar y botón "Cerrar Sesión"):**
   * *Evento:* `onClick = { viewModel.signOut(); navController.navigate("auth/login") { popUpTo(0) } }`
   * *RF cubierto:* **RF-30: Gestión de Sesión y Logout.** Revoca el token local en DataStore, borra credenciales en caché y limpia el backstack de navegación.

### 4. Comportamiento en modo Offline
* **Spots Guardados Accesibles:** Los spots de la pestaña "Favoritos" se almacenan permanentemente en la base de datos local de Room. El skater puede consultar sus spots preferidos en cualquier momento en medio de la calle sin necesidad de conexión de datos.
* **Modo degradado de perfil:** Se muestran los datos del perfil cacheados de la última sincronización. Si el usuario intenta editar su biografía o cambiar su avatar sin red, se bloquea la acción con un mensaje emergente avisando de que los cambios de perfil requieren conexión activa.

### 5. Capturas a exportar de Figma
* `FIG_VISTA05_PROFILE_BOOKMARKS.png`: Cabecera con avatar, métricas y pestaña activa "Guardados" mostrando la lista de spots.
* `FIG_VISTA05_PROFILE_GUEST_STATE.png`: Estado alternativo para rol Invitado mostrando la pantalla de invitación al registro.
