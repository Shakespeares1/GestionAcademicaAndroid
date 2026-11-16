# Configuración de la API Android

## Backend HTTPS

La variante **debug** usa por defecto `https://gestion-asignaturas-api.onrender.com/`. Para volver al backend local del emulador, compila con `-PGA_API_BASE_URL=https://10.0.2.2:8081/`; `10.0.2.2` es el alias de loopback del equipo anfitrión, donde el backend local escucha en `127.0.0.1:8081` con IP SAN `10.0.2.2`.

- El certificado público de la CA de desarrollo se incluye únicamente en `app/src/debug/res/raw/gestion_academica_dev_ca.pem` y se confía mediante `debug-overrides` de Network Security Configuration.
- La política debug no permite HTTP en claro. OkHttp conserva la validación normal de cadena y hostname.
- En Android 17/API 37, debug declara `ACCESS_LOCAL_NETWORK`; `MainActivity` solicita el permiso antes de entrar al flujo de la app cuando la URL apunta a una dirección local. Si se deniega, no se emiten llamadas Retrofit locales y se ofrecen reintento o ajustes. Release no declara este permiso.
- En el emulador disponible se concedió el permiso y se verificó desde el proceso de la app TCP a `10.0.2.2:8081`, handshake TLS y `GET /actuator/health` HTTP 200. Esto no valida login ni operaciones CRUD.
- La clave privada de la CA, la clave privada del servidor y el keystore PKCS12 permanecen fuera de ambos repositorios, en una carpeta temporal local con permisos restringidos. Nunca copies la clave privada de la CA ni el keystore al APK.
- El backend de integración usa variables de entorno de proceso para habilitar SSL, seleccionar el keystore y proporcionar su contraseña. PostgreSQL continúa usando `gestion_academica_integration_local`, Flyway queda desactivado y Hibernate en `validate`.

## Configuración de URL

- `GA_API_BASE_URL` configura la URL de debug. El valor predeterminado es `https://gestion-asignaturas-api.onrender.com/`; para desarrollo local usa `-PGA_API_BASE_URL=https://10.0.2.2:8081/`.
- `GA_RELEASE_API_BASE_URL` configura la URL de release por separado. Sin esta propiedad, release no tiene URL de backend y el cliente rechazará la configuración vacía.
- Puedes pasar propiedades Gradle con `-PGA_API_BASE_URL=...` para debug y `-PGA_RELEASE_API_BASE_URL=...` para release. No guardes secretos ni credenciales en estas propiedades.
- La CA privada de desarrollo solo se agrega al manifiesto y recursos debug; release no referencia ese certificado ni `network_security_config`.

## Diagnóstico HTTPS

La consulta `GET /actuator/health` fue validada previamente desde el emulador con HTTPS, la CA de desarrollo y la identidad IP del servidor. El receptor y los listeners de diagnóstico temporales se retiraron al cerrar esa investigación. La CA pública, Network Security Configuration debug, validación TLS y bloqueo de tráfico en claro se conservan.

La URL de Render queda configurada solo como predeterminada de debug; `GA_RELEASE_API_BASE_URL` sigue independiente y sin valor predeterminado.
