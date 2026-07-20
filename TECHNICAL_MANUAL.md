Manual Técnico — Convertidor de Moneda v1.0

1. Descripción del sistema

Convertidor de Moneda es una aplicación Android que permite a cualquier usuario convertir montos entre distintas divisas usando tasas de cambio actualizadas, y llevar un historial personal de sus conversiones guardado en la nube.


Problema que resuelve: consultar tasas de cambio actualizadas y calcular conversiones de forma rápida, sin depender de una calculadora manual ni de datos desactualizados, además de mantener un registro histórico de las conversiones realizadas para poder consultarlas o editarlas después.
Usuario objetivo: cualquier persona que necesite convertir divisas de forma ocasional o frecuente (viajeros, estudiantes, freelancers que cobran en distintas monedas, etc.).
Alcance del MVP:

Autenticación de usuario (correo/contraseña y Google Sign-In)
Conversión de monedas con tasas en tiempo real (con respaldo offline si no hay conexión)
Historial de conversiones por usuario (crear, editar, eliminar)
Modo oscuro
Notificación diaria recordando actualizar tasas
Cierre de sesión





2. Arquitectura de la aplicación

La app sigue el patrón MVVM (Model-View-ViewModel), organizada en tres capas:

┌─────────────────────────────────────────┐
│                   UI                     │
│  Activities / Fragments (LoginActivity,  │
│  MainActivity, ConvertFragment,          │
│  HistoryFragment, SettingsFragment,      │
│  AddConversionFragment)                  │
│  → Observan LiveData y actualizan vistas │
└───────────────────┬───────────────────────┘
│ observa / invoca
┌───────────────────▼───────────────────────┐
│                 LÓGICA                     │
│         HistoryViewModel (ViewModel)       │
│  → Expone LiveData (conversions, apiState, │
│     realRates, selectedRecord)             │
│  → Orquesta llamadas al Repository y a la  │
│     API vía coroutines (viewModelScope)    │
└───────────────────┬───────────────────────┘
│ delega
┌───────────────────▼───────────────────────┐
│                  DATOS                     │
│  CurrencyRepository → Firebase Realtime DB │
│  RetrofitClient / CurrencyApiService → API │
│  externa de tasas de cambio                │
│  FirebaseAuth → autenticación de usuario   │
└─────────────────────────────────────────────┘

Patrón de diseño usado: MVVM, con HistoryViewModel compartido entre fragmentos mediante activityViewModels(), de forma que todas las pantallas (Convertir, Historial, formulario de edición) trabajan sobre el mismo estado y las mismas tasas de cambio sin duplicar llamadas a la red.

La navegación entre pantallas dentro de MainActivity se maneja con Fragment Transactions (supportFragmentManager) controladas por un BottomNavigationView, en vez de Jetpack Navigation Component.

3. Modelo de datos

La persistencia del historial se hace en Firebase Realtime Database, bajo el nodo conversions. La entidad principal es:

ConversionRecord

CampoTipoDescripciónidString?Clave generada automáticamente por Firebase (push().key)fromCurrencyStringMoneda de origen (ej. "USD")toCurrencyStringMoneda de destino (ej. "EUR")amountDoubleMonto original ingresadoresultDoubleResultado calculado de la conversióntimestampLongFecha/hora de la conversión (epoch millis)userIdStringUID del usuario dueño del registro (Firebase Auth)

Relación: cada ConversionRecord pertenece a un único usuario a través de userId (relación 1 a N entre Usuario de Firebase Auth y sus registros de conversión). No hay relación entre registros entre sí. Las lecturas se filtran por userId (orderByChild("userId").equalTo(userId)), de forma que cada usuario solo ve su propio historial.

Usuario (Firebase Auth)  1 ────< N  ConversionRecord
uid                              userId (FK lógica)
fromCurrency
toCurrency
amount
result
timestamp

Adicionalmente, la app consume un modelo de solo lectura desde la API externa:

ExchangeRatesResponse

CampoTipoDescripcióndateStringFecha de las tasasratesMap<String, Double>Tasas de cambio, código de moneda → valor

4. Tecnologías y librerías


Framework: Android nativo (Kotlin)
Lenguaje: Kotlin
Base de datos: Firebase Realtime Database (com.google.firebase:firebase-database-ktx, vía Firebase BOM 32.7.0)
Autenticación: Firebase Authentication (com.google.firebase:firebase-auth-ktx) + Google Sign-In (com.google.android.gms:play-services-auth:20.7.0)
API externa: Currency API (@fawazahmed0) — API pública, no requiere API key
Compilación mínima / objetivo:

minSdk = 24
targetSdk = 34
compileSdk = 34
JDK / Kotlin JVM target: 17





Librerías principales (con versión):

LibreríaVersiónandroidx.core:core-ktx1.12.0androidx.appcompat:appcompat1.6.1com.google.android.material:material1.10.0androidx.constraintlayout:constraintlayout2.1.4androidx.work:work-runtime-ktx2.9.0androidx.activity:activity-ktx1.8.2androidx.fragment:fragment-ktx1.6.2androidx.lifecycle:lifecycle-viewmodel-ktx2.7.0androidx.lifecycle:lifecycle-livedata-ktx2.7.0com.google.firebase:firebase-bom32.7.0com.google.firebase:firebase-database-ktx(vía BOM)com.google.firebase:firebase-auth-ktx(vía BOM)com.google.android.gms:play-services-auth20.7.0com.squareup.retrofit2:retrofit2.9.0com.squareup.retrofit2:converter-gson2.9.0com.google.code.gson:gson2.10.1

Otros componentes usados:


WorkManager (CurrencyWorker) para programar una notificación periódica cada 24 horas recordando al usuario que las tasas se actualizaron
ViewBinding habilitado para acceso a vistas sin findViewById en las pantallas principales


5. Instrucciones para compilar

Requisitos:


Android Studio (versión reciente compatible con AGP y Gradle 8.2)
JDK 17
SDK de Android con la plataforma 34 instalada
Cuenta de Google/Firebase con acceso al proyecto convertidormoneda-34267 (o uno propio configurado igual)


Pasos:


Clonar el repositorio:


git clone https://github.com/StivenCatota/Convertidor-Monedas-android.git


Abrir la carpeta del proyecto en Android Studio (File → Open)
Colocar el archivo google-services.json (descargado desde la consola de Firebase del proyecto) en la ruta app/google-services.json
Esperar a que Android Studio sincronice Gradle automáticamente, o forzarlo con el botón "Sync Now" / ícono del elefante
Ejecutar la app con el botón Run ▶️ sobre un emulador o dispositivo físico con Android 7.0 (API 24) o superior


Variables/archivos de entorno necesarios:


app/google-services.json — credenciales de Firebase (Auth + Realtime Database). No se incluye en el repositorio por seguridad; debe generarse desde Firebase Console.
No se requiere API key para el servicio de tasas de cambio (API pública gratuita).
En Firebase Console debe estar habilitado: Authentication → Sign-in method → Correo/contraseña y Google, y Realtime Database creada, con la huella SHA-1 del proyecto registrada para que funcione el inicio de sesión con Google.


6. Estructura del repositorio

app/src/main/java/com/example/catotaerick/convertidormoneda/
├── LoginActivity.kt              # Pantalla de inicio de sesión / registro
├── MainActivity.kt               # Actividad contenedora con navegación inferior
├── HistoryActivity.kt            # (legado) Actividad de historial
├── CurrencyWorker.kt             # Worker de WorkManager (notificación diaria)
├── AddConversionFragment.kt      # BottomSheet para crear/editar una conversión
├── adapter/
│   └── HistoryAdapter.kt         # Adapter del RecyclerView del historial
├── api/
│   ├── RetrofitClient.kt         # Configuración de Retrofit
│   └── CurrencyApiService.kt     # Definición del endpoint de tasas
├── model/
│   ├── ConversionRecord.kt       # Entidad de conversión (Firebase)
│   └── ExchangeRatesResponse.kt  # Modelo de respuesta de la API + ApiState
├── repository/
│   └── CurrencyRepository.kt     # Acceso CRUD a Firebase Realtime Database
├── ui/
│   ├── ConvertFragment.kt        # Pantalla principal de conversión
│   ├── HistoryFragment.kt        # Pantalla de historial
│   └── SettingsFragment.kt       # Pantalla de ajustes (tema, cerrar sesión)
└── viewmodel/
└── HistoryViewModel.kt       # ViewModel compartido (estado de la app)

app/src/main/res/
├── layout/                       # XML de pantallas y componentes
├── values/, values-night/        # Colores y temas (claro/oscuro)
├── drawable/, drawable-nodpi/    # Íconos vectoriales y logo
└── mipmap-*/                     # Íconos de lanzador

7. Historial de versiones


v1.0 — MVP completo para presentación final. Incluye:   

Login y registro con correo/contraseña y Google Sign-In (con selector de cuenta forzado)
Conversión de monedas con tasas en tiempo real y respaldo offline
Historial de conversiones sincronizado con Firebase (crear, editar, eliminar, con actualización automática de la lista)
Modo oscuro
Notificaciones diarias de recordatorio
Cierre de sesión
Correcciones de estabilidad: incompatibilidad de estilos Material3/MaterialComponents, validación de campos de registro, manejo de rotación de pantalla, manejo de errores de Google Sign-In
Optimización de memoria: logo movido a drawable-nodpi para evitar sobre-escalado en pantallas de alta densidad