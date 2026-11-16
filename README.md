# Gestión Académica Android

Aplicación Android desarrollada en Java y XML. Consume la API REST del backend Spring Boot para gestionar usuarios y asignaturas.

## Requisitos

- Android Studio compatible con el Gradle Wrapper incluido.
- JDK 21.
- Android SDK 37.
- Dispositivo Android compatible para instalar el APK.
- Conexión a Internet para comunicarse por HTTPS con el backend público.

## Instalación

El APK debug de entrega está disponible en `entrega/app-debug.apk`.

1. Copia o descarga `entrega/app-debug.apk` en el teléfono Android.
2. Abre el archivo y confirma la instalación. Android puede solicitar autorización para instalar aplicaciones desde esa fuente.
3. Abre **Gestión Académica** e inicia sesión con una cuenta habilitada en el backend.

El APK debug se instaló y ejecutó correctamente en un celular Android real. Esta variante está preparada para pruebas académicas; no es una versión de producción.

## Compilar desde el código fuente

Desde la raíz del proyecto, en PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

El APK resultante se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Para instalarlo con ADB en un dispositivo conectado:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Backend y conexión

La aplicación usa HTTPS para comunicarse con el backend desplegado en Render. El backend utiliza PostgreSQL alojado en Supabase. Android se comunica únicamente con la API REST; no se conecta directamente a la base de datos.

La URL de la API para debug se configura mediante Gradle y, por defecto, apunta al backend público. La variante release utiliza una configuración independiente. Consulta [API_CONFIGURATION.md](API_CONFIGURATION.md) para los detalles de configuración.

## Funcionalidades comprobadas

En las validaciones realizadas para la entrega se comprobaron:

- Inicio de sesión.
- Registro y gestión de usuarios: consultar, crear, editar y eliminar.
- Gestión de asignaturas: consultar, crear, editar y eliminar.
- Cuatro reportes parametrizados.
- Lecturas del acelerómetro y del sensor de proximidad.

## Seguridad

- Las comunicaciones con el backend usan HTTPS.
- La sesión JWT se almacena cifrada con AES-GCM y una clave de Android Keystore.
- La aplicación no guarda la contraseña del usuario.
- No incluyas credenciales, tokens ni secretos en el repositorio.
- El certificado de desarrollo es exclusivo de debug y no debe usarse para una variante release.

## Limitaciones conocidas

- No usar la CA ni el keystore de desarrollo para release.
- Para probar login y módulos protegidos se necesita una cuenta de prueba autorizada. Para pruebas de escritura debe confirmarse primero que la base sea aislada y que los registros de prueba sean identificables.
- La recuperación de contraseña requiere que el backend tenga configurado un proveedor SMTP y una URL HTTPS de restablecimiento funcional.
