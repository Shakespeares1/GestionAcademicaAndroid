# Configuración de la API Android

- La variante **debug** usa por defecto `https://10.0.2.2:8443/`, dirección de desarrollo del emulador Android para llegar a una máquina anfitriona. Es solo un valor de desarrollo; no representa el backend desplegado y no se asume que haya un servidor escuchando allí.
- Para apuntar debug a otro servidor de desarrollo HTTPS, define la propiedad Gradle `GA_API_BASE_URL` con una URL que termine en `/`. Ejemplo en el archivo global `~/.gradle/gradle.properties`: `GA_API_BASE_URL=https://host-de-desarrollo/`.
- La variante **release** no contiene una URL predeterminada. Si no se configura `GA_API_BASE_URL`, el cliente se niega a construirse al usarse.
- El cliente acepta únicamente URLs HTTPS, no confía en certificados personalizados automáticamente y no omite validación TLS. Para desarrollo con certificado privado, instala/configura confianza correctamente; no agregues un trust-all.
- No guardes secretos, contraseñas, tokens ni credenciales del backend en propiedades Gradle. La URL no es un secreto.
- No hay aún una URL de producción confirmada ni desplegada en esta configuración.
