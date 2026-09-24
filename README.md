# RutaPaquete

Aplicación Android desarrollada en Kotlin para registrar usuarios, iniciar sesión y presentar el punto de entrada al rastreo de envíos. La versión actual cumple el flujo de cuentas local; la consulta real de paquetes y la administración de envíos se describen más adelante como **alcance futuro**, no como funciones ya disponibles.

## Estado funcional

| Componente | Disponible actualmente | Alcance futuro |
| --- | --- | --- |
| Cuentas de clientes | Alta con validaciones, almacenamiento local e inicio de sesión | Sincronización y gestión centralizada si la aplicación opera en varios dispositivos |
| Rastreo | Pantalla con campo para código y botón «Buscar paquete» | Consultar y mostrar únicamente paquetes vinculados al cliente autenticado |
| Administración | No existe cuenta ni pantalla de administrador | Rol protegido para consultar clientes y gestionar paquetes y asignaciones |
| Paquetes | No existe tabla, servicio ni datos de paquetes | Modelo de paquetes con código único, propietario, estado y detalles |
| Sesión | Identificador de usuario en memoria; cierre de sesión | Control de acceso por rol y sesiones apropiadas para un servicio compartido |

El botón «Buscar paquete» **no ejecuta una búsqueda ni muestra resultados**. Es pulsable y no altera la información del usuario. La dirección del formulario es texto introducido manualmente; no se obtiene por GPS.

## Requisitos y puesta en marcha

- Android Studio con SDK de Android 37 instalado y emulador o dispositivo Android desde API 24.
- Gradle Wrapper incluido en el repositorio: no hace falta instalar Gradle por separado. El proyecto configura Gradle 9.5.0 y una JVM 25 para el daemon; Android Studio puede descargar la cadena de herramientas necesaria durante la sincronización.
- Conexión a Internet durante la primera sincronización para descargar dependencias. La aplicación en ejecución no requiere conexión para las funciones actuales.
- En Android Studio, abrir **esta carpeta raíz** como proyecto, esperar la sincronización de Gradle, seleccionar la configuración `app` y pulsar **Run**. La pantalla inicial debe ser el inicio de sesión «RutaPaquete».

Desde PowerShell en la raíz del proyecto:

~~~powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:lintDebug
~~~

`connectedDebugAndroidTest` requiere un emulador o dispositivo conectado. El APK de desarrollo generado por `assembleDebug` queda en `app/build/outputs/apk/debug/`. En macOS o Linux se utiliza `./gradlew` en vez de `.\gradlew.bat`. Las versiones declaradas y requisitos efectivos están en `app/build.gradle.kts`, `gradle/libs.versions.toml` y `gradle/gradle-daemon-jvm.properties`.

### Recorrido de uso

1. Abrir la aplicación. No se distribuye un usuario precargado.
2. Pulsar **Crear nuevo usuario** y completar los ocho campos del registro con datos de prueba.
3. Pulsar **Enviar**. Si todos los datos son válidos, la cuenta se guarda y la aplicación vuelve al inicio de sesión con un mensaje de confirmación y el nombre de usuario ya escrito. **Cancelar** o Atrás regresan sin guardar.
4. Introducir la contraseña registrada y pulsar **Iniciar sesión**. Un acceso correcto abre la pantalla de rastreo.
5. Escribir un código en el campo de rastreo si se desea observar la interfaz. **Buscar paquete** todavía no produce consulta ni resultado.
6. Pulsar **Cerrar sesión** para volver al inicio. Cerrar y abrir el proceso también termina la sesión en memoria, pero la cuenta permanece en la base local del dispositivo.

Para comprobar desde cero el registro en un emulador puede limpiarse el almacenamiento de la aplicación; esto elimina las cuentas locales de ese emulador. Para verificar persistencia, cerrar y reabrir la app **sin** limpiar sus datos.

## Funcionalidades implementadas

### Inicio de sesión

`MainActivity` es el único punto de entrada declarado como `MAIN/LAUNCHER`. Presenta nombre de usuario, contraseña, **Iniciar sesión** y **Crear nuevo usuario**. Señala campos vacíos antes de consultar datos. El nombre de usuario se normaliza eliminando espacios en los extremos y convirtiéndolo a minúsculas; la contraseña se compara sin alterarla.

La consulta busca una cuenta en Room y compara la contraseña introducida con su hash almacenado. Las credenciales incorrectas muestran un mensaje genérico, sin revelar si existe el usuario. Durante la operación se presenta progreso y se deshabilitan los controles para evitar envíos repetidos. Si el acceso es correcto, se guarda el identificador en `Session`, se limpia el campo de contraseña y se abre `TrackingActivity`; la pantalla de acceso sale de la pila de navegación.

### Registro de clientes

`RegisterActivity` presenta un formulario vertical desplazable, diseñado para poder alcanzar los campos inferiores cuando aparece el teclado. Todos los campos son obligatorios. La interfaz muestra errores junto al campo correspondiente y enfoca el primer dato inválido. Las reglas aplicadas por `RegistrationValidator` y comprobadas nuevamente en `UserRepository` son:

| Campo | Regla actual | Valor almacenado |
| --- | --- | --- |
| Usuario | De 3 a 30 caracteres `a-z`, `0-9`, punto, guion o guion bajo. Se ignoran espacios en los extremos y mayúsculas/minúsculas para identificar duplicados. | Usuario normalizado, único |
| Nombre | No puede estar vacío ni contener únicamente espacios. | Texto sin espacios en los extremos |
| Apellidos | Misma regla que nombre. | Texto sin espacios en los extremos |
| Edad | Número entero de 18 a 120 años, inclusive. | Entero |
| Dirección o ubicación | Texto libre no vacío; no se solicita permiso de ubicación. | Texto sin espacios en los extremos |
| Teléfono | De 7 a 15 dígitos; se permiten además `+`, espacios, paréntesis y guiones. | Texto sin espacios en los extremos |
| Contraseña | De 8 a 128 caracteres y no formada solamente por espacios. | Hash y salt, nunca texto plano |
| Confirmar contraseña | Debe coincidir **exactamente** con la contraseña anterior. | No se almacena |

Antes de insertar se consulta si el usuario existe y, además, el índice `UNIQUE` de la base impide duplicados incluso ante intentos simultáneos. **Enviar** guarda una cuenta válida y vuelve al acceso; **Cancelar** no inserta nada. Durante el guardado se deshabilitan los controles y se muestra progreso. Un error de datos o de almacenamiento no abre la pantalla de rastreo.

### Pantalla inicial de rastreo

`TrackingActivity` requiere un identificador de sesión: si se abre sin uno, redirige al inicio de sesión. Muestra el nombre de la app, un mensaje que pide ingresar un código, un campo de texto de una línea (máximo 60 caracteres), **Buscar paquete** y **Cerrar sesión**. El mensaje de la interfaz indica que la búsqueda estará disponible próximamente. El botón de búsqueda tiene actualmente un controlador vacío: no valida el código, no consulta una tabla ni se conecta a una API. **Cerrar sesión** borra la sesión en memoria y restablece la pila para regresar al inicio.

### Navegación y estado

~~~text
Inicio de sesión
├─ Crear nuevo usuario → Registro
│  ├─ Cancelar / Atrás → Inicio, sin inserción
│  ├─ Datos inválidos → Errores en el formulario
│  └─ Enviar datos válidos → Guardar en Room → Inicio, con confirmación
└─ Iniciar sesión
   ├─ Credenciales incorrectas → Mensaje genérico
   └─ Credenciales correctas → Rastreo
      └─ Cerrar sesión → Inicio
~~~

La sesión solo guarda `userId` en memoria. No persiste después de terminar el proceso y no constituye autenticación remota. Los usuarios sí permanecen en el almacenamiento privado de la aplicación mientras no se borren sus datos o se desinstale.

## Arquitectura y organización

La interfaz principal usa **Activities Kotlin + vistas XML Material Components + View Binding**. `lifecycleScope` ejecuta las operaciones suspendidas del repositorio; el cálculo de contraseñas se desplaza a `Dispatchers.Default`. La separación funcional es: interfaz → repositorio/validación → DAO → Room/SQLite.

| Ruta desde la raíz | Responsabilidad |
| --- | --- |
| `app/src/main/java/com/example/myapplication/MainActivity.kt` | Formulario de acceso, errores, navegación al registro y apertura del rastreo |
| `app/src/main/java/com/example/myapplication/RegisterActivity.kt` | Captura y validación visible del formulario, guardado y retorno al acceso |
| `app/src/main/java/com/example/myapplication/TrackingActivity.kt` | Pantalla de rastreo visual, protección básica de sesión y cierre de sesión |
| `app/src/main/java/com/example/myapplication/Session.kt` | Identificador de la cuenta autenticada en memoria |
| `app/src/main/java/com/example/myapplication/data/Registration.kt` | Datos recibidos del formulario, normalización de usuario y reglas de validación |
| `app/src/main/java/com/example/myapplication/data/UserRepository.kt` | Alta, control de duplicados y verificación de credenciales |
| `app/src/main/java/com/example/myapplication/data/PasswordHasher.kt` | Generación de salt y cálculo/comparación de hashes |
| `app/src/main/java/com/example/myapplication/data/local/User.kt` | Entidad Room de usuarios |
| `app/src/main/java/com/example/myapplication/data/local/UserDao.kt` | Inserción y consultas suspendidas |
| `app/src/main/java/com/example/myapplication/data/local/AppDatabase.kt` | Base Room versión 1 con una única instancia por proceso |
| `app/src/main/java/com/example/myapplication/ui/ScreenInsets.kt` | Ajuste del contenido frente a barras del sistema y teclado |
| `app/src/main/res/layout/activity_main.xml` | Vista del acceso |
| `app/src/main/res/layout/activity_register.xml` | Vista desplazable del registro |
| `app/src/main/res/layout/activity_tracking.xml` | Vista inicial del rastreo |
| `app/src/main/res/values/account_strings.xml` | Textos en español de estas pantallas |
| `app/src/main/res/values/colors.xml` y `themes.xml` | Colores y tema visual Material |
| `app/src/main/AndroidManifest.xml` | Declaración de Activities, launcher, tema y política de copias |
| `app/schemas/com.example.myapplication.data.local.AppDatabase/1.json` | Esquema exportado de Room versión 1 |

`MainActivity`, `RegisterActivity` y `TrackingActivity` son las únicas pantallas del recorrido principal. Las Activities y layouts de `MyFirstApp`, `LinearLayout.kt` y `activity_*layout.xml` proceden de ejercicios de interfaz conservados en el proyecto; no aparecen en el flujo normal y están marcados como no exportados en el manifiesto. El proyecto conserva también dependencias y archivos de tema Compose de esos ejercicios, aunque las tres pantallas principales se implementan en XML. Los iconos de lanzamiento están en `res/mipmap-*` y `res/drawable`.

### Base de datos actual

Room 3 crea `rutapaquete.db` en el almacenamiento privado del dispositivo, mediante `AndroidSQLiteDriver`. `AppDatabase` es un Singleton con `applicationContext` y versión de esquema 1. Solo hay una tabla: `usuarios`.

| Columna | Tipo en el modelo | Uso |
| --- | --- | --- |
| `id` | `Long`, clave primaria autogenerada | Identificador interno de la cuenta |
| `usuario` | `String`, índice único | Identificación al iniciar sesión |
| `nombre`, `apellidos` | `String` | Datos personales |
| `edad` | `Int` | Edad validada al registrar |
| `direccion`, `telefono` | `String` | Datos proporcionados por el usuario |
| `passwordHash` | `ByteArray` | Derivado de la contraseña |
| `passwordSalt` | `ByteArray` | Salt aleatorio individual |

`UserDao` ofrece `insertar`, `buscarPorUsuario`, `existe` y `obtenerTodos`, ordenado por identificador. **El método para obtener todos existe, pero no hay una pantalla que lo use para administración**; actualmente se emplea en pruebas. Tampoco existen tabla de paquetes, relación cliente-paquete, tabla de eventos ni migraciones posteriores a la versión 1.

### Seguridad y límites de la versión actual

Cada registro usa un salt de 16 bytes generado con `SecureRandom` y un hash de 256 bits mediante `PBKDF2WithHmacSHA1` con 1 300 000 iteraciones. La comparación utiliza `MessageDigest.isEqual`. Las contraseñas y su confirmación no se guardan como texto ni se escriben deliberadamente en los registros de depuración. El cálculo se realiza fuera del hilo de la interfaz.

`AndroidManifest.xml` desactiva las copias de seguridad de la aplicación. Registro y rastreo no se exportan a otras aplicaciones. No se solicitan permisos de Internet o ubicación para el flujo existente. Estas medidas protegen el ejemplo local, pero **no convierten la autenticación en un servicio de producción**: la identidad solo se comprueba contra la base del mismo dispositivo, no hay sincronización entre equipos, recuperación de contraseña, cuenta administradora, control de permisos en servidor ni sesiones persistentes. La dirección es declarada por el usuario, no verificada geográficamente.

## Pruebas y comprobación

El proyecto incluye pruebas locales e instrumentadas:

| Conjunto | Archivos | Cobertura prevista |
| --- | --- | --- |
| Unitarias | `RegistrationValidatorTest.kt`, `PasswordHasherTest.kt`, `ExampleUnitTest.kt` | Formato de usuario, edad, teléfono, contraseñas, hash y prueba de plantilla |
| Instrumentadas | `AccountFlowTest.kt`, `DatabasePersistenceTest.kt`, `SessionGuardTest.kt`, `ExampleInstrumentedTest.kt` | Flujo de cuentas, persistencia Room, redirección sin sesión y prueba de plantilla |

El último resultado unitario conservado en `app/build/test-results/testDebugUnitTest/` registra **12 pruebas aprobadas**. El XML instrumentado disponible en `app/build/outputs/androidTest-results/connected/debug/` es anterior a la actualización de dependencias de AndroidX Test: registra cinco ejecuciones, tres fallos por incompatibilidad de una versión previa de Espresso con el emulador API 37. El catálogo actual declara Espresso 3.7.0, AndroidX JUnit 1.3.0 y Rules 1.7.0, pero **no hay un informe posterior en el proyecto que permita afirmar que las cinco instrumentadas ya pasan**. Para conocer el estado vigente hay que ejecutar nuevamente `:app:connectedDebugAndroidTest`. El emulador se utilizó también para revisar visualmente las tres pantallas.

Al reproducir manualmente conviene comprobar: registro vacío; edad 17 rechazada y 18 aceptada; contraseñas distintas; nombre de usuario duplicado; cancelación sin alta; inicio de sesión correcto e incorrecto; conservación de la cuenta al reiniciar; cierre de sesión; acceso directo a rastreo sin sesión; y que **Buscar paquete** no bloquea la pantalla aunque todavía no consulte datos. `Database Inspector` permite observar la tabla `usuarios` durante la ejecución, sin exponer contraseñas en texto claro.

## Archivos auxiliares y material de referencia

- `scripts/build_report.py` prepara un informe PDF de diez páginas a partir de capturas y un resumen de pruebas situados en `output/evidencias/` y `output/pruebas/`. Rechaza insumos faltantes; generar un PDF no sustituye revisarlo visualmente. Este script no forma parte de la ejecución de la app.
- `scripts/package_delivery.py` empaqueta fuentes y el informe en `output/RutaPaquete-proyecto.zip`, verifica el ZIP y genera una suma SHA-256. Excluye compilaciones, cachés, credenciales y archivos locales. Requiere que exista previamente el PDF esperado; no publica ni envía archivos.
- Los PDF de referencia en la raíz explican la consigna del formulario y conceptos de Room. No son dependencias de compilación ni representan funciones implementadas.
- `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml` y `gradle/wrapper/` definen el proyecto y sus dependencias. El módulo de aplicación es `app`; `applicationId` y `namespace` siguen siendo `com.example.myapplication`, aunque el nombre visible es «RutaPaquete». La compatibilidad declarada comienza en API 24; compilación y objetivo usan API 37. `versionCode` es 1 y `versionName` es 1.0.

## Alcance funcional futuro: rastreo y administración

Esta sección es una **especificación para desarrollo posterior**. Ninguna de las funciones descritas aquí —búsqueda real, roles, asignación o CRUD de paquetes— está implementada actualmente.

### Roles y acceso

Se introducirán dos roles: **cliente** y **administrador**. El registro público creará exclusivamente cuentas cliente. La cuenta administradora se dará de alta por un procedimiento controlado; no se incluirá una contraseña fija en el código ni se permitirá elegir «administrador» desde el formulario público. Tras iniciar sesión, cada rol llegará a su vista correspondiente. El cliente podrá consultar solo sus envíos; el administrador podrá revisar los clientes registrados y gestionar los paquetes.

Para evolucionar la base local se añadirá un campo de rol a los usuarios con una **migración explícita** desde el esquema 1 que asigne «cliente» a las cuentas existentes sin borrar datos. La interfaz de administración deberá usar una proyección de datos personales necesaria para su trabajo, sin mostrar `passwordHash` ni `passwordSalt`.

### Modelo propuesto de paquete

Crear una entidad `Paquete` o `Envio` con, como mínimo:

| Dato | Finalidad |
| --- | --- |
| `id` | Identificador interno |
| `codigoRastreo` | Código único e indexado que el usuario escribe para buscar; «código de ruta» se entenderá como este mismo identificador, no como un trayecto geográfico |
| `clienteId` | Referencia obligatoria a una cuenta cliente registrada |
| `descripcion` | Contenido o referencia legible del envío, sin información innecesariamente sensible |
| `origen` y `destino` | Puntos de salida y entrega |
| `estado` | Estado controlado, por ejemplo «creado», «en tránsito», «en reparto» y «entregado» |
| `creadoEn` y `actualizadoEn` | Fechas para ordenar y explicar la información mostrada |
| `fechaEstimada` y eventos de seguimiento | Ampliación opcional para una línea de tiempo más detallada |

La primera relación será **un cliente → muchos paquetes**; cada paquete tendrá exactamente un cliente asignado. La base y el servicio deberán impedir códigos duplicados y referencias a clientes inexistentes. Una futura asignación a varios destinatarios exigiría una tabla de relación adicional y reglas nuevas, no un simple cambio en la búsqueda.

### Consulta del cliente

Al pulsar **Buscar paquete**, el cliente escribirá un código no vacío. El sistema lo normalizará con una regla documentada —por ejemplo, quitar espacios exteriores y usar mayúsculas para un código alfanumérico— y consultará por **código y cliente autenticado conjuntamente**. Un paquete solo aparecerá si su `clienteId` coincide con la sesión; conocer el código de otra persona no dará acceso a su envío. Si no existe, no está asignado a ese cliente o fue eliminado, se mostrará un mensaje neutro como «No se encontró un paquete asociado a tu cuenta», evitando revelar la existencia de paquetes ajenos.

Cuando haya coincidencia, se abrirá una vista de detalle con código, descripción, origen, destino, estado y última actualización; si se implementan eventos, también su historial. Mientras se realiza la consulta se mostrará progreso, y los errores de conexión se distinguirán de un resultado inexistente. El cliente no podrá crear, reasignar, editar ni borrar paquetes.

### Panel y operaciones del administrador

El panel protegido permitirá listar y buscar cuentas cliente registradas para elegir un destinatario. A partir de ahí cubrirá el ciclo completo de paquetes:

| Operación CRUD | Acción prevista | Regla principal |
| --- | --- | --- |
| **Create / Crear** | Registrar un paquete nuevo y asignarlo a un cliente existente | Código único, datos obligatorios y cliente válido |
| **Read / Consultar** | Listar paquetes, filtrarlos y abrir sus detalles; el cliente solo consultará los propios por código | Aplicar el alcance del rol y la pertenencia del paquete |
| **Update / Actualizar** | Editar descripción, trayecto, estado, fechas o cliente asignado | Validar cambios y registrar cuándo se modificó |
| **Delete / Eliminar** | Retirar un paquete desde el panel, con confirmación | Dejar de mostrarlo al cliente; preferir baja lógica y auditoría si se requiere historial |

La reasignación deberá actualizar de inmediato qué cliente puede encontrar el paquete. La eliminación no debe dejar referencias inconsistentes. Conviene registrar quién creó, modificó, reasignó o eliminó cada envío; definir una política de conservación antes de elegir entre borrado físico y baja lógica.

### Arquitectura necesaria para varios dispositivos

Room, tal como está hoy, almacena datos **solo en el dispositivo donde se instaló la app**. Un administrador en otro teléfono no podría ver las cuentas locales de los clientes ni asignarles paquetes. Si la meta es un sistema de rastreo utilizable por varias personas y equipos, el siguiente desarrollo necesitará una **API y base de datos central**, autenticación verificable en servidor y autorización del lado del servidor para cada lectura o modificación. El cliente Android podrá conservar Room como caché, pero una comprobación de permisos solo en la pantalla o una consulta local no bastará para proteger datos compartidos.

Como alternativa académica limitada, toda la demostración podría hacerse en un mismo dispositivo con Room y cambio de cuentas; en ese caso habría que indicar expresamente que las asignaciones no se sincronizan con otros teléfonos. La elección entre prototipo local y servicio compartido debe fijarse antes de implementar el panel.

### Orden recomendado para la siguiente fase

1. Definir reglas de negocio, estados de envío, datos visibles para el cliente y decisión «prototipo local» o «servicio centralizado».
2. Diseñar roles, migración de `usuarios` y modelo de paquetes; añadir pruebas de persistencia, código único y relación con clientes.
3. Crear el acceso de administrador mediante aprovisionamiento controlado y una pantalla protegida de clientes registrados.
4. Implementar alta, consulta, edición, asignación/reasignación y eliminación de paquetes con validaciones y confirmaciones.
5. Conectar **Buscar paquete** a una consulta filtrada por código y propietario, y crear la vista de detalle.
6. Probar permisos y casos límite: código ajeno, no asignado, inexistente o duplicado; cambio de propietario; paquete eliminado; cliente intentando entrar al panel; y persistencia tras reiniciar.

Las notificaciones, lectura de códigos QR, mapa en tiempo real y cálculo de rutas podrían evaluarse después, pero no forman parte ni de la versión actual ni del CRUD definido para la siguiente fase.
