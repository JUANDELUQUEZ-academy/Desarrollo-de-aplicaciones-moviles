# RutaPaquete - aplicación Android en Kotlin

Proyecto universitario: formulario de registro, autenticación local y vista inicial de rastreo.
Nombre provisional de la aplicación: **RutaPaquete**.

## Cómo seguir el avance desde Android Studio

Selecciona la vista **Project** en el panel izquierdo y abre este README en la raíz.
Android Studio detecta los cambios guardados en disco. Si el editor solicita recargar un archivo
modificado externamente, recárgalo. Los archivos de lógica están en
`app/src/main/java/com/example/myapplication` y las vistas en `app/src/main/res/layout`.

## Estado del desarrollo (20 de septiembre de 2026)

- [x] Fase 1: MainActivity como único launcher, vista XML y View Binding.
- [x] Código de entidad User, DAO, AppDatabase Singleton y repositorio.
- [x] Código de validación de registro, hash de contraseña y sesión local.
- [x] Activities de login, registro y rastreo; vistas XML integradas.
- [x] Compilar y verificar la integración completa de Room y KSP.
- [x] 12 pruebas unitarias aprobadas (10 validaciones, 1 hash y 1 prueba original).
- [ ] Ejecutar pruebas unitarias, de base de datos y de navegación.
- [ ] Verificar visualmente en emulador y capturar evidencias reales.
- [ ] Preparar PDF y ZIP entregables.

La versión completa y su APK de pruebas ya compilan. El análisis Lint detectó dos
ids ausentes en los layouts de ejercicios anteriores; se agregaron sin cambiar sus diseños.
Lint ya terminó con 0 errores. Falta ejecutar las pruebas instrumentadas en el emulador.

## Alcance

1. Login: usuario y contraseña; botones Iniciar sesión y Crear nuevo usuario.
2. Registro: usuario único, nombre, apellidos, edad (18 a 120), dirección/ubicación textual,
   teléfono, contraseña y confirmación. Enviar guarda; Cancelar vuelve sin guardar.
3. Registro válido: vuelve al login con mensaje de éxito y usuario precargado.
4. Login válido: abre rastreo. Login inválido: muestra mensaje genérico.
5. Rastreo: texto orientativo, código y botón Buscar paquete sin consulta real.
6. Cerrar sesión vuelve al login. Reiniciar el proceso termina la sesión pero conserva las cuentas.

## Archivos y responsabilidades

| Archivo | Propósito |
| --- | --- |
| MainActivity.kt | Login, consulta al repositorio, mensaje posterior al registro y navegación |
| RegisterActivity.kt | Formulario, errores por campo e inserción mediante lifecycleScope |
| TrackingActivity.kt | Vista de rastreo y cierre de sesión |
| Session.kt | Identificador de usuario autenticado en memoria |
| data/local/User.kt | Tabla usuarios con id autoincrementable e índice UNIQUE |
| data/local/UserDao.kt | Insertar, consultar, comprobar usuario existente y listar |
| data/local/AppDatabase.kt | Room Singleton, esquema versión 1 |
| data/UserRepository.kt | Validación defensiva, registro y verificación de credenciales |
| data/Registration.kt | Reglas de validación compartidas y normalización de usuario |
| data/PasswordHasher.kt | PBKDF2 y salt aleatorio por cuenta |
| ui/ScreenInsets.kt | Espacio para barras del sistema y teclado |
| res/layout/activity_main.xml | Vista de login |
| res/layout/activity_register.xml | Formulario desplazable |
| res/layout/activity_tracking.xml | Vista de rastreo |
| res/values/account_strings.xml | Textos de interfaz |
| app/schemas | Historial de esquema Room generado por KSP |

Los ejercicios en MyFirstApp se conservan fuera del flujo principal.

## Decisiones de datos y seguridad

- Room guarda la base `rutapaquete.db` en el almacenamiento privado de la app.
- No hace falta Internet ni GPS para esta etapa.
- Usuario: se recortan extremos y se convierte a minúsculas; entre 3 y 30 caracteres.
- Nombres/dirección aceptan espacios y acentos; no pueden estar vacíos.
- Teléfono: entre 7 y 15 dígitos, admitiendo +, espacios, paréntesis y guiones.
- Contraseña: 8 a 128 caracteres, sin recortar ni convertir mayúsculas.
- Confirmación: comparación exacta; no se almacena.
- PBKDF2-HMAC-SHA1 con 1.300.000 iteraciones, salt de 16 bytes y salida de 256 bits.
  Esta variante es compatible con API 24 y 25. Se calcula fuera del hilo de interfaz.
- No se registran contraseñas en logs. Los campos de contraseña no guardan estado.
- No se habilitan copias de seguridad de cuentas.
- Autenticación local educativa: no sustituye un servicio de autenticación de producción.

## Herramientas

Android Studio, Kotlin, XML, Material Components, View Binding, Gradle Wrapper,
KSP, Room, SQLite Framework, lifecycleScope, JUnit y Espresso.
Las versiones se encuentran en `gradle/libs.versions.toml`.
Se conserva la configuración Compose de los ejercicios, aunque el flujo nuevo utiliza XML.

## Historial de trabajo

- Fase 1: único launcher, View Binding y pantalla XML; compilación verificada.
- Integración: creadas capas de datos, validaciones, Activities y vistas.
- Verificación: APK de aplicación y APK de pruebas generados; 12 pruebas unitarias aprobadas.
- Corrección: ids main añadidos a activity_linear_layout y activity_test_layout para evitar fallos de sus Activities.
- Emulador: RutaPaquete_API_37 (Pixel 6, Android 17/API 37.0, imagen revisión 6)
  creado e iniciado con WHPX; dispositivo `emulator-5554`. Descarga verificada por SHA1.
- Corrección: dos errores de sintaxis detectados durante edición simultánea fueron reparados
  (atributo layout_marginTop y listener del botón Enviar). Compilación completa aprobada.
- Primera ejecución instrumentada: 5 pruebas, 2 aprobadas y 3 fallaron por Espresso 3.5.1
  incompatible con InputManager de Android 17. Actualizado a Espresso 3.7.0,
  JUnit AndroidX 1.3.0 y rules 1.7.0; segunda ejecución en curso.
- Preparados scripts/build_report.py y scripts/package_delivery.py. El informe exige
  capturas reales y output/pruebas/resumen.json; el ZIP valida exclusiones e integridad.

## Prompt para retomar si se interrumpe la sesión

> Continúa la actividad de RutaPaquete en C:\juan_tecmi\desarrollo-moviles.
> Lee primero README.md y revisa git diff, sin sobrescribir cambios ajenos.
> Completa los puntos pendientes de este README: termina las pruebas instrumentadas,
> corrige los fallos que aparezcan, verifica las tres pantallas en el emulador y genera
> evidencias reales, el PDF universitario y el ZIP sin build. Usa los archivos ya creados.
> Trabaja sin Gemini, mantén este README actualizado y entrega enlaces a los resultados.
> No publiques ni envíes la entrega a la universidad automáticamente.

## Rúbrica y entrega

La actividad solicita User (id, nombre, apellidos, direccion, telefono), DAO con inserción
y consultas, RoomDatabase Singleton, validación de campos y corrutinas.
Se agregan usuario, edad y credenciales para cubrir el flujo solicitado.

Entrega prevista: proyecto ZIP sin carpetas build, README, APK de prueba y PDF con código
y capturas reales de validación y registro exitoso. No usar información personal real
para las demostraciones.
