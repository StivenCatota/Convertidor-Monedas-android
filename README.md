# Convertidor de Monedas

Aplicación móvil Android que permite convertir monedas en tiempo real, diseñada para turistas, viajeros frecuentes y estudiantes que viven fuera de su país.

## Descripción del Problema

Cuando una persona viaja a otro país o vive en el extranjero, necesita conocer el valor real de su dinero antes de realizar compras, pagar servicios o administrar su presupuesto diario. Consultar tasas de cambio manualmente o depender de páginas web poco intuitivas genera confusión y pérdida de tiempo en momentos clave.

**Convertidor de Monedas** resuelve este problema ofreciendo una herramienta rápida e intuitiva que permite convertir entre monedas con tasas actualizadas en tiempo real, directamente desde el celular, guardando un historial personal de todas las conversiones.

## Objetivo de la Aplicación

Proporcionar a turistas, viajeros frecuentes y estudiantes internacionales una herramienta móvil simple y confiable para convertir monedas en tiempo real, con historial de conversiones sincronizado en la nube y funcionamiento con tasas de respaldo cuando no hay conexión.

## Historias de Usuario del MVP

| ID | Título | Como... | Quiero... | Para... |
|---|---|---|---|---|
| HU-01 | Conversión para compras | Turista extranjero | Convertir mi moneda natal a la moneda local antes de comprar | Conocer el costo real y evitar confusiones con el tipo de cambio |
| HU-02 | Selección de monedas | Usuario de la aplicación | Seleccionar fácilmente la moneda de origen y destino desde una lista | Realizar conversiones rápidas y sin errores |
| HU-03 | Tasas en tiempo real | Viajero frecuente | Consultar tasas de cambio actualizadas en tiempo real | Obtener conversiones precisas mientras viajo entre países |
| HU-04 | Conversión para gastos estudiantiles | Estudiante extranjero | Convertir mi dinero a la moneda del país donde estudio | Administrar correctamente mis gastos diarios y presupuesto |
| HU-05 | Historial de conversiones | Viajero | Guardar el historial de mis conversiones, con la posibilidad de editarlas o borrarlas | Revisar valores anteriores sin ingresar nuevamente los datos |
| HU-06 | Acceso seguro | Usuario de la aplicación | Iniciar sesión con correo/contraseña o con mi cuenta de Google | Mantener mi historial protegido y disponible solo para mí |

## Capturas de Pantalla

![Pantalla de Conversión](screenshots/convert_screen.png)
![Historial de Conversiones](screenshots/history_screen.png)
![Configuración](screenshots/settings_screen.png)

*ConvertScreen · HistoryScreen · SettingsScreen*

## Arquitectura

El proyecto implementa el patrón **MVVM (Model-View-ViewModel)**, con tres capas:

```
┌───────────────────────────────────────────┐
│                    UI                      │
│  LoginActivity · MainActivity              │
│  ConvertFragment · HistoryFragment         │
│  SettingsFragment · AddConversionFragment  │
└───────────────────┬─────────────────────────┘
                     │ observa / invoca
┌───────────────────▼─────────────────────────┐
│                  LÓGICA                      │
│  HistoryViewModel (ViewModel compartido      │
│  entre fragmentos vía activityViewModels)    │
└───────────────────┬─────────────────────────┘
                     │ delega
┌───────────────────▼─────────────────────────┐
│                  DATOS                       │
│  CurrencyRepository → Firebase Realtime DB   │
│  RetrofitClient / CurrencyApiService → API   │
│  externa de tasas de cambio                  │
│  FirebaseAuth → autenticación                │
└───────────────────────────────────────────────┘
```

### Entidades principales

| Entidad | Almacenamiento | Descripción |
|---|---|---|
| `ConversionRecord` | Firebase Realtime Database | Registro de cada conversión (monedas, monto, resultado, fecha, usuario) |
| `ExchangeRatesResponse` | Solo lectura (API) | Tasas de cambio actualizadas obtenidas de la API externa |
| Usuario | Firebase Authentication | Cuenta del usuario (correo/contraseña o Google) |

## Tecnología Usada

| Componente | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Android Views (XML) + ViewBinding |
| Arquitectura | MVVM |
| Persistencia del historial | Firebase Realtime Database |
| Autenticación | Firebase Authentication + Google Sign-In |
| Consumo de API | Retrofit + Gson |
| Tareas en segundo plano | WorkManager (notificaciones diarias) |
| Control de versiones | Git + GitHub |
| IDE | Android Studio |

## Instalación y Configuración

### Requisitos previos

- Android Studio (versión reciente compatible con AGP y Gradle 8.2)
- JDK 17
- Android SDK 34, mínimo compatible: Android 7.0 (API 24)
- Cuenta de Firebase con acceso al proyecto (o uno propio configurado igual)

### Pasos para ejecutar el proyecto

1. Clona el repositorio:
   ```
   git clone https://github.com/StivenCatota/Convertidor-Monedas-android.git
   ```
2. Abre el proyecto en Android Studio: `File → Open → selecciona la carpeta del proyecto`
3. Coloca tu archivo `google-services.json` (descargado desde Firebase Console) en la ruta `app/google-services.json` — **no se incluye en el repositorio por seguridad**
4. Espera a que Gradle sincronice automáticamente, o dale a **Sync Now**
5. Conecta un dispositivo Android o inicia el emulador
6. Ejecuta la aplicación con el botón ▶ **Run**

Ver [`TECHNICAL_MANUAL.md`](TECHNICAL_MANUAL.md) para el detalle completo de arquitectura, modelo de datos y estructura del repositorio, y [`USER_MANUAL.md`](USER_MANUAL.md) para la guía de uso orientada al usuario final.

## 🧪 Cómo probar las funciones CRUD del historial

1. **Crear:** en la pantalla "Convertir", realiza una conversión — se guarda automáticamente en tu historial en Firebase.
2. **Leer:** ve a la pestaña "Historial" para ver la lista completa, ordenada de más reciente a más antigua.
3. **Actualizar:** toca una conversión de la lista para abrir el formulario de edición.
4. **Borrar:** mantén presionada una conversión — aparecerá un diálogo de confirmación, y luego la opción de "Deshacer".

## Estructura del Proyecto

```
app/src/main/java/com/example/catotaerick/convertidormoneda/
├── LoginActivity.kt              # Inicio de sesión / registro
├── MainActivity.kt               # Actividad contenedora con navegación inferior
├── HistoryActivity.kt            # (legado)
├── CurrencyWorker.kt             # Worker de WorkManager (notificación diaria)
├── AddConversionFragment.kt      # BottomSheet para crear/editar una conversión
├── adapter/HistoryAdapter.kt
├── api/RetrofitClient.kt
├── api/CurrencyApiService.kt
├── model/ConversionRecord.kt
├── model/ExchangeRatesResponse.kt
├── repository/CurrencyRepository.kt
├── ui/ConvertFragment.kt
├── ui/HistoryFragment.kt
├── ui/SettingsFragment.kt
└── viewmodel/HistoryViewModel.kt
```

## Estado Actual del Proyecto

| Fase | Estado |
|---|---|
| Definición del problema y HU | ✅ Completo |
| Diseño de arquitectura (MVVM) | ✅ Completo |
| Prototipo en Figma | ✅ Completo |
| Autenticación (correo/contraseña + Google) | ✅ Completo |
| Integración con API de tasas de cambio | ✅ Completo |
| Historial CRUD con Firebase Realtime Database | ✅ Completo |
| Modo oscuro y ajustes | ✅ Completo |
| Notificaciones diarias | ✅ Completo |
| Release v1.0 firmado (APK) | ✅ Completo |

## Autor

**Stiven Catota**
- GitHub: [@StivenCatota](https://github.com/StivenCatota)

## 📄 Licencia

Este proyecto fue desarrollado con fines académicos.
