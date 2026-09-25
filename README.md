# RutaPaquete

Aplicación Android en Kotlin para gestionar paquetes, asignarlos a clientes registrados y consultar su información mediante un código de rastreo. La versión 2.0 integra registro, inicio de sesión por roles, administración de paquetes y edición del perfil del cliente.

Esta entrega funciona de forma local con Room: las cuentas y los paquetes permanecen en el dispositivo. Para demostrar ambos roles se cierra una sesión y se inicia otra en el mismo emulador o teléfono.

## 1. Funcionamiento de la aplicación

### 1.1. Preparación y primer acceso

Al abrir la aplicación aparece el inicio de sesión. Se puede acceder con una cuenta existente, crear una cuenta cliente o, si todavía no existe un administrador, seleccionar **Configurar administrador local**.

El responsable del dispositivo configura al administrador una sola vez, completando sus datos y eligiendo su usuario y contraseña. No existe una contraseña predeterminada. Después del alta, la opción de configuración desaparece y el registro normal continúa creando únicamente clientes. Si había cuentas guardadas en la primera versión, se conservan y reciben el rol de cliente.

Pasos para comenzar una demostración:

1. Configurar al administrador desde la pantalla de acceso y guardar sus credenciales.
2. Crear al menos una cuenta cliente con **Crear nuevo usuario**.
3. Iniciar sesión como administrador, crear un paquete y elegir al cliente destinatario.
4. Cerrar sesión e ingresar como ese cliente.
5. Escribir el código del paquete en el buscador para consultar sus detalles.
6. Abrir **Mi perfil** para actualizar la información personal.

Las cuentas no son temporales ni desaparecen al cerrar la aplicación. Se conservan en la base local. Borrar los datos de la app o desinstalarla elimina esa información; cambiar de teléfono no la transfiere automáticamente.

### 1.2. Registro y validaciones

El formulario solicita usuario, nombre, apellidos, edad, dirección o ubicación, teléfono, contraseña y confirmación. Presenta errores junto a los campos y enfoca el primer dato inválido. **Enviar** guarda los datos válidos y regresa al inicio de sesión con el usuario precargado; **Cancelar** y Atrás regresan sin registrar la cuenta. Durante el guardado se bloquean los controles para evitar envíos repetidos.

| Campo | Validación y tratamiento |
| --- | --- |
| Usuario | De 3 a 30 caracteres: letras sin acentos, números, punto, guion o guion bajo. Se eliminan los espacios exteriores y se convierte a minúsculas. Debe ser único. |
| Nombre y apellidos | Obligatorios; admiten espacios y acentos. La interfaz limita cada campo a 100 caracteres. |
| Edad | Número entero entre 18 y 120, inclusive. |
| Dirección o ubicación | Texto obligatorio, introducido por el usuario; máximo 240 caracteres en la interfaz. No se utiliza GPS. |
| Teléfono | De 7 a 15 dígitos; admite también +, espacios, paréntesis y guiones. |
| Contraseña | De 8 a 128 caracteres; no puede estar formada únicamente por espacios. |
| Confirmación | Coincidencia exacta con la contraseña, incluyendo mayúsculas y espacios. No se almacena. |

Las contraseñas se transforman en hashes con un salt aleatorio por cuenta. La base no almacena la contraseña en texto plano.

### 1.3. Inicio y cierre de sesión

El inicio exige usuario y contraseña. La comprobación utiliza los datos de Room y muestra **Usuario o contraseña incorrectos** cuando las credenciales no coinciden. La cuenta determina la pantalla de destino:

| Rol | Destino y permisos |
| --- | --- |
| Administrador | Panel administrativo, consulta de clientes y CRUD de paquetes. |
| Cliente | Buscador de sus paquetes, detalle del envío y edición de su propio perfil. |

La sesión conserva en memoria el identificador de la cuenta autenticada. Al cerrar sesión se borra ese identificador y se vuelve al acceso. Si el proceso termina, se debe iniciar sesión nuevamente; las cuentas y los paquetes permanecen guardados. Las pantallas protegidas verifican el rol en la base de datos y las operaciones del repositorio vuelven a comprobar los permisos.

El registro y la verificación de contraseñas muestran un indicador de progreso. El cálculo criptográfico puede tardar varios segundos, especialmente en el emulador; se ejecuta fuera del hilo de interfaz y los controles se bloquean mientras termina para evitar operaciones repetidas.

### 1.4. Panel de administrador

La pantalla **Administración** muestra los paquetes activos, el número total, un buscador y el botón **Crear paquete**. Cada tarjeta presenta código, descripción, destinatario y estado; **Ver / editar paquete** abre la ficha completa.

El filtro permite localizar paquetes por código, descripción, usuario, nombre o apellidos del cliente. Si no hay paquetes o ningún resultado coincide, aparece un mensaje específico. **Ver clientes registrados** cambia a la lista de clientes y muestra nombre completo, usuario, edad, teléfono y dirección. Los hashes y salts de contraseña no se incluyen en esa consulta. **Ver paquetes** devuelve a la lista de envíos.

El administrador puede revisar a los clientes y asignarles paquetes, pero no dispone de funciones para modificar sus contraseñas o cambiar sus roles.

### 1.5. Crear y asignar paquetes

**Crear paquete** abre un formulario con los siguientes datos:

| Dato | Funcionamiento |
| --- | --- |
| Código de rastreo | Obligatorio y único, entre 3 y 60 caracteres. Acepta letras, números y guiones; debe comenzar con letra o número. Se guarda en mayúsculas, sin espacios exteriores. |
| Cliente destinatario | Selector de cuentas cliente existentes, identificadas por nombre y usuario. No permite asignar un paquete a un administrador. |
| Descripción | Entre 3 y 300 caracteres; identifica el envío o su contenido. |
| Origen y destino | Entre 2 y 240 caracteres cada uno. |
| Estado | Creado, En tránsito, En reparto, Entregado o Incidencia. |
| Entrega estimada | Opcional; fecha válida en formato AAAA-MM-DD. No se calcula automáticamente. |
| Notas para el cliente | Opcionales, hasta 500 caracteres; forman parte de la información visible del envío. |

Si no hay clientes registrados, el formulario informa que primero se necesita una cuenta cliente. No se guardan paquetes sin destinatario. Un código duplicado se rechaza, incluso si solo cambia entre mayúsculas y minúsculas.

Al guardar se registran automáticamente las fechas de creación y última modificación y los identificadores del administrador responsable. La cuenta se vincula por su identificador interno; si el cliente cambia su nombre de usuario, conserva sus paquetes.

El editor conserva los datos pendientes y el destinatario seleccionado al volver desde otra aplicación o recrearse la pantalla. **Cancelar** abandona esos cambios sin guardarlos.

### 1.6. Consultar, actualizar y eliminar: CRUD completo

| Operación | Acción en la aplicación | Resultado |
| --- | --- | --- |
| Create / Crear | **Crear paquete → Guardar paquete** | Alta de un envío asociado a un cliente. |
| Read / Leer | Lista y filtro administrativos; búsqueda del cliente por código | Consulta de información de acuerdo con el rol. |
| Update / Actualizar | **Ver / editar paquete → Guardar paquete** | Cambio de código, descripción, origen, destino, estado, fecha estimada, notas o destinatario. |
| Delete / Eliminar | **Ver / editar paquete → Eliminar paquete → Confirmar** | Retiro del paquete de las listas y búsquedas. |

La edición mantiene el identificador y la fecha de creación del paquete. Actualiza la fecha de modificación y el administrador que hizo el cambio. El estado se selecciona manualmente para representar el avance conocido del envío.

La reasignación cambia inmediatamente el cliente autorizado: el destinatario anterior deja de encontrar el paquete y el nuevo puede consultarlo. El cambio de código requiere comunicar el nuevo código al cliente.

La eliminación solicita confirmación. **Cancelar** conserva el paquete. Al confirmar se realiza una baja lógica: el registro permanece en la base con su fecha de eliminación, pero no se muestra al administrador ni al cliente. Su código queda reservado para impedir reutilizaciones ambiguas. Esta versión no tiene una pantalla de restauración de paquetes eliminados.

### 1.7. Rastreo del cliente

La pantalla de rastreo saluda al cliente por su nombre y presenta el campo **Código de rastreo**, **Buscar paquete**, **Mi perfil** y **Cerrar sesión**.

La búsqueda requiere un código no vacío y comprueba simultáneamente código y propietario. Si el paquete existe, está activo y pertenece a la sesión, se abre **Detalle del envío**, con:

- Código de rastreo y descripción.
- Nombre del destinatario.
- Estado actual, origen y destino.
- Fecha estimada, o **Por definir**.
- Notas del administrador, o **Sin notas adicionales**.
- Fecha de creación y última actualización.

Para un código inexistente, eliminado o asignado a otra cuenta, el resultado es el mismo: **Ese paquete no existe.** Conocer el código de otra persona no da acceso a su información. La pantalla de detalle también verifica la propiedad; no depende únicamente de haber pasado por el buscador.

El botón **Volver** regresa al rastreo. El cliente no puede crear, modificar, reasignar ni eliminar paquetes.

### 1.8. Edición del perfil del cliente

**Mi perfil** permite cambiar usuario, nombre, apellidos, edad, dirección y teléfono. Mantiene las validaciones del registro, incluida la unicidad del usuario y la mayoría de edad.

Para guardar se solicita la **contraseña actual**. Los campos **Nueva contraseña** y **Confirmar nueva contraseña** son opcionales: si ambos se dejan vacíos se conserva la contraseña anterior; si se completa alguno se valida la nueva contraseña y su coincidencia. Una contraseña actual incorrecta impide guardar cualquier cambio.

Después de guardar se regresa al rastreo con el nombre actualizado. Si cambió el usuario o la contraseña, las nuevas credenciales se utilizan en el siguiente inicio de sesión. El identificador de la cuenta permanece igual, por lo que las asignaciones de paquetes se conservan. Cambiar la dirección del perfil no modifica el destino de envíos ya registrados; si cambia una entrega, el administrador debe editar ese paquete. **Cancelar** abandona la edición sin modificar la cuenta. El rol no es editable.

### 1.9. Flujo de navegación y alcance

~~~text
Inicio de sesión
├── Crear cliente / Configuración inicial del administrador
│   └── Guardar o cancelar → Inicio de sesión
├── Cuenta administradora → Administración
│   ├── Clientes registrados
│   ├── Crear paquete y asignar cliente
│   └── Ver / editar paquete → Guardar cambios o confirmar eliminación
└── Cuenta cliente → Rastreo
    ├── Buscar código → Detalle propio o «Ese paquete no existe.»
    └── Mi perfil → Guardar cambios o cancelar
~~~

Las siete Activities del flujo principal son acceso, registro, administración, editor de paquetes, rastreo, detalle y perfil. La creación del administrador utiliza el formulario de registro en modo de configuración inicial.

La entrega termina con estas funciones locales. El rastreo muestra los datos que registra el administrador; no obtiene posiciones en tiempo real, no consulta empresas de paquetería y no envía notificaciones. No se incluye recuperación de contraseñas, múltiples administradores desde la interfaz, edición de cuentas ajenas ni borrado de clientes.

## 2. Idea de negocio y problema que resuelve

### 2.1. Problema

Un pequeño negocio que entrega pedidos puede llevar los datos de clientes y envíos en mensajes, hojas de cálculo o anotaciones separadas. Eso dificulta relacionar un paquete con su destinatario, consultar su último estado y mantener la información consistente cuando cambia una dirección o se corrige una asignación.

RutaPaquete reúne el registro de clientes, la gestión de paquetes y su consulta en un mismo flujo. El administrador captura la información operativa y el cliente consulta el envío mediante un código. La vinculación con una cuenta limita la consulta a los paquetes que le corresponden.

### 2.2. Usuarios y propuesta de valor

| Usuario | Necesidad | Respuesta de RutaPaquete |
| --- | --- | --- |
| Responsable de operación | Registrar envíos y saber a quién pertenecen | Formulario con código único y selección de un cliente existente. |
| Personal que administra los envíos | Corregir datos o actualizar el estado | Lista, filtro, ficha editable y confirmación de eliminación. |
| Cliente | Conocer los datos y el estado de su paquete | Consulta por código con detalle limitado a su cuenta. |
| Cliente con datos desactualizados | Corregir su información de contacto | Perfil editable con confirmación de contraseña. |

La propuesta combina trazabilidad básica, control de acceso y una interfaz sencilla en español. El valor operativo depende de que el administrador mantenga actualizados los datos; la aplicación no verifica por sí misma que un paquete haya llegado a un lugar ni que se haya entregado.

### 2.3. Proceso que representa

El cliente registra sus datos y el responsable crea un envío relacionado con su cuenta. El código identifica el paquete y sirve para comunicarlo al destinatario. Conforme avanza la operación, el administrador actualiza el estado y las notas. El cliente consulta esa información y puede corregir su perfil sin perder el vínculo con sus envíos.

Un ejemplo de uso es un negocio que prepara un pedido de libros, lo registra con origen y destino, lo asigna a una clienta y cambia el estado a **En tránsito** cuando sale. La clienta introduce el código y ve esa actualización. Si otra cuenta utiliza el mismo código, no recibe información sobre el pedido.

### 2.4. Valor de la versión entregada

Esta versión demuestra de principio a fin el registro de cuentas, los dos roles, el CRUD de paquetes, la relación entre clientes y envíos y la consulta restringida. Permite evaluar el flujo y las validaciones en un teléfono o emulador sin depender de un servicio externo.

Los beneficios previstos son reducir registros duplicados, evitar asignaciones a personas inexistentes, facilitar la corrección de información y ofrecer una consulta consistente. Son objetivos del producto; esta entrega no presenta mediciones comerciales ni afirma que ya se hayan reducido tiempos o costos en una empresa real.

### 2.5. Alcance comercial y evolución posible

El mercado potencial incluye pequeños comercios y operaciones de reparto que necesiten un seguimiento sencillo. Antes de un lanzamiento comercial sería necesario validar con negocios reales qué estados utilizan, qué información necesita el cliente y cuántos envíos gestionan.

La versión entregada es una demostración local. Para que un administrador y varios clientes trabajen desde teléfonos distintos se necesitaría una API con base de datos central, autenticación y autorización en servidor y sincronización. Room podría mantenerse como almacenamiento de apoyo en Android. También sería necesario definir recuperación de cuentas, respaldo, disponibilidad del servicio y tratamiento de datos personales.

Una posible evolución comercial sería ofrecer el sistema como servicio para negocios, con planes según usuarios o volumen de envíos; es una hipótesis de negocio, no una función de cobro ni un modelo validado. La integración con transportistas, avisos de cambio de estado, lectura de QR y ubicación en tiempo real serían ampliaciones posteriores, fuera de esta entrega.

## 3. Herramientas y aspectos técnicos

### 3.1. Entorno y tecnologías

| Herramienta o componente | Uso y configuración del proyecto |
| --- | --- |
| Android Studio | Edición, sincronización, ejecución, depuración e inspección de datos. |
| Kotlin | Lógica de Activities, validadores, repositorios y modelos. El catálogo conserva Kotlin 2.2.10 para el plugin Compose. |
| XML y Material Components 1.10.0 | Formularios, campos con errores, botones, tarjetas y diálogos. |
| View Binding | Acceso tipado a las vistas generadas desde los layouts. |
| Room 3.0.3 y SQLite Framework 2.7.1 | Persistencia local con `AndroidSQLiteDriver`. |
| KSP 2.3.10 | Generación del código de Room y exportación de esquemas. |
| Corrutinas y `lifecycleScope` | Operaciones suspendidas; cálculo de hashes en `Dispatchers.Default`. |
| Gradle Wrapper 9.5.0 y AGP 9.3.1 | Compilación y tareas de verificación. |
| JVM 25 del daemon / compatibilidad Java 11 | Entorno declarado de Gradle y nivel Java configurado para el módulo. |
| JUnit 4.13.2, AndroidX JUnit 1.3.0, Espresso 3.7.0 y Rules 1.7.0 | Pruebas locales y en dispositivo. |

El módulo es `app`, el nombre visible es **RutaPaquete**, `versionCode` es 2 y `versionName` es 2.0. `applicationId` y `namespace` son `com.example.myapplication`. Se declara compatibilidad desde API 24 y se compila con objetivo API 37. Las versiones completas están en `gradle/libs.versions.toml`.

### 3.2. Organización del código

La lógica principal se encuentra en `app/src/main/java/com/example/myapplication/`. Las vistas están en `app/src/main/res/layout/`.

| Archivo o carpeta | Responsabilidad |
| --- | --- |
| `MainActivity.kt` | Acceso, configuración inicial y navegación según rol. |
| `RegisterActivity.kt` | Registro cliente y alta inicial del administrador. |
| `AdminActivity.kt` | Lista y filtro de paquetes, consulta de clientes. |
| `ParcelEditorActivity.kt` | Alta, asignación, edición y confirmación de baja. |
| `TrackingActivity.kt` | Búsqueda por código y acceso al perfil. |
| `ParcelDetailActivity.kt` | Información de un paquete perteneciente al cliente. |
| `ProfileActivity.kt` | Edición de datos y cambio opcional de contraseña. |
| `Session.kt` | Identificador de sesión en memoria. |
| `ui/ProtectedActivity.kt` | Comprobación de rol, ejecución de operaciones, progreso y manejo común de errores. |
| `ui/ScreenInsets.kt` | Ajuste frente al teclado y barras del sistema. |
| `data/Registration.kt` y `data/ParcelForm.kt` | Datos de formularios, validadores y normalización. |
| `data/UserRepository.kt` | Cuentas, credenciales, rol y actualización del perfil. |
| `data/ParcelRepository.kt` | Permisos, asignación, consultas, creación, edición y baja lógica. |
| `data/PasswordHasher.kt` | Salt aleatorio, PBKDF2 y comparación de hashes. |
| `data/local/User.kt` y `Parcel.kt` | Entidades de usuarios y paquetes. |
| `data/local/UserDao.kt` y `ParcelDao.kt` | Consultas e inserciones/actualizaciones parametrizadas. |
| `data/local/AppDatabase.kt` | Singleton Room y migración de esquema 1 a 2. |

Los layouts `activity_admin.xml`, `activity_parcel_editor.xml`, `activity_parcel_detail.xml` y `activity_profile.xml` amplían las vistas de acceso, registro y rastreo. `item_parcel.xml` e `item_client.xml` definen las tarjetas del panel. Los textos están en `res/values/account_strings.xml` y `workspace_strings.xml`; `themes.xml` mantiene la identidad visual clara, con verde azulado y controles Material.

Los ejercicios previos de `MyFirstApp`, `LinearLayout.kt` y sus layouts se conservan fuera de la navegación principal. También se conserva la configuración Compose de esos ejercicios. El flujo de RutaPaquete utiliza XML. `AndroidManifest.xml` declara únicamente `MainActivity` como launcher y mantiene las demás Activities como no exportadas.

### 3.3. Base de datos y migración

La base privada `rutapaquete.db` usa el esquema 2 y contiene dos tablas:

| Tabla | Datos principales y restricciones |
| --- | --- |
| `usuarios` | `id` autogenerado, `usuario` único, nombre, apellidos, edad, dirección, teléfono, hash, salt y rol. |
| `paquetes` | `id` autogenerado, código único, `clienteId`, descripción, origen, destino, estado, fecha estimada, notas, fechas de creación/actualización, administradores responsables y fecha opcional de eliminación. |

La relación es **un cliente → varios paquetes**, con un solo destinatario por paquete. `clienteId` tiene índice y clave foránea a `usuarios.id`; la eliminación física de un usuario referenciado se restringe. El repositorio comprueba además que el destinatario tenga rol cliente.

`MIGRATION_1_2` añade el rol con valor predeterminado `CLIENTE` y crea la tabla y los índices de paquetes. Conserva identificadores, datos y credenciales de las cuentas anteriores. No se configura una migración destructiva. Los esquemas exportados están en `app/schemas/com.example.myapplication.data.local.AppDatabase/`.

La creación del primer administrador usa una transacción que comprueba que aún no exista uno. El guardado y la baja de paquetes se realizan en transacciones de escritura. El código de rastreo sigue reservado después de una baja lógica porque el índice único abarca también los registros retirados.

### 3.4. Seguridad y decisiones técnicas

Las cuentas usan `PBKDF2WithHmacSHA1`, 1 300 000 iteraciones, salt aleatorio de 16 bytes y hash de 256 bits. La comparación se realiza con `MessageDigest.isEqual` y fuera del hilo de interfaz. La confirmación de contraseña no se almacena y los campos de contraseña no conservan su contenido al recrear una pantalla.

La sesión guarda únicamente el identificador; el rol se obtiene de Room. El repositorio exige rol administrador para listar clientes y gestionar paquetes, y rol cliente para consultar envíos propios o actualizar el perfil. El detalle vuelve a comprobar la pertenencia por identificador, y la búsqueda filtra por código, propietario y ausencia de baja.

El cambio de perfil requiere verificar la contraseña actual. Cambiar usuario o contraseña no cambia el identificador ni las asignaciones. El registro público no permite seleccionar roles; la configuración inicial del administrador solo funciona mientras no exista uno y debe realizarla el responsable del dispositivo.

No se solicitan permisos de Internet ni GPS. Las copias de seguridad están desactivadas en el manifiesto. Esta autenticación es local y no sustituye un servicio de identidad para varios dispositivos. La base no está cifrada por la aplicación: utiliza el almacenamiento privado de Android y las contraseñas se guardan como derivados criptográficos.

### 3.5. Ejecución, pruebas y resultados

Abrir la raíz del proyecto en Android Studio, sincronizar Gradle, instalar el SDK requerido y ejecutar `app` en un emulador o teléfono compatible. El AVD utilizado para la verificación es `RutaPaquete_API_37`. La primera sincronización puede necesitar Internet para descargar dependencias.

Si el emulador restaura una sesión dañada, no termina de iniciar o indica que los servicios `activity` o `package` no están disponibles, utilizar **Cold Boot** desde Device Manager. Este reinicio evita cargar la instantánea anterior y conserva los datos. No es necesario utilizar **Wipe Data**, que sí elimina las cuentas y los paquetes del dispositivo.

Desde PowerShell en la raíz:

~~~powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:lintDebug
~~~

La prueba instrumentada requiere un dispositivo conectado. En macOS y Linux el comando equivalente comienza con `./gradlew`. El APK de desarrollo se genera en `app/build/outputs/apk/debug/app-debug.apk`.

Las pruebas cubren validaciones de cuentas y paquetes, formato de fechas, hash de contraseñas, persistencia, migración, roles, códigos duplicados, acceso a paquetes propios y ajenos, reasignación, baja y modificación del perfil. El recorrido de interfaz reproduce alta del administrador, consulta de clientes, CRUD, rastreo y cambio de datos/contraseña; también comprueba el formulario con teclado visible y la conservación del destinatario al pausar y recrear el editor.

| Prueba | Comprobaciones principales |
| --- | --- |
| `RegistrationValidatorTest` | Campos obligatorios, edad, teléfono, usuario y coincidencia de contraseñas. |
| `ParcelValidatorTest` | Normalización y formato del código, destinatario, descripción, estado, fechas y contraseña opcional del perfil. |
| `PasswordHasherTest` | Verificación correcta e incorrecta de contraseñas y uso de salt. |
| `AccountFlowTest` | Registro, rechazo de edad menor y contraseñas diferentes, cancelación, duplicados, login, cierre de sesión, teclado y recreación del acceso. |
| `DatabasePersistenceTest` | Recuperación de las cuentas después de cerrar y abrir la base. |
| `ParcelRepositoryTest` | Roles, CRUD, destinatarios válidos, aislamiento entre clientes, reasignación, perfil, credenciales, persistencia y migración de la versión 1. |
| `SessionGuardTest` | Rechazo del acceso al rastreo sin una sesión autenticada. |
| `WorkspaceFlowTest` | Recorrido visual de administrador y cliente, pausa/recreación del editor, cancelación y confirmación de baja, perfil y denegación del panel administrativo a un cliente. |

Se conservan también las pruebas básicas de ejemplo del proyecto. La suite utiliza datos ficticios: no requiere cuentas reales ni información de envíos particulares.

El ejecutor `RutaTestRunner` utiliza una base separada, `rutapaquete-instrumented.db`, para los recorridos de interfaz. Las pruebas de persistencia y migración crean bases de prueba identificadas por nombre; sus datos no se mezclan con la base normal de uso. Las capturas instrumentadas se guardan en la carpeta externa de la app, bajo `evidencias/v2/`. La propiedad `android.injected.androidTest.leaveApksInstalledAfterRun=true` conserva la instalación al terminar y las animaciones se desactivan durante las pruebas para hacer reproducible el recorrido.

La verificación final del **25 de septiembre de 2026** terminó correctamente en el emulador `RutaPaquete_API_37` (Android 17, API 37):

| Verificación | Resultado |
| --- | --- |
| Compilación del APK de desarrollo | Correcta: `BUILD SUCCESSFUL`. |
| Pruebas locales JUnit | 18 aprobadas, 0 fallos y 0 errores. |
| Pruebas instrumentadas | 9 aprobadas, 0 fallos y 0 omitidas. |
| Revisión Android Lint | 0 errores y 27 advertencias sobre dependencias, recursos y configuración del proyecto. |
| Recorrido y evidencias | Registro, acceso, administrador, editor de paquetes, rastreo, detalle y perfil comprobados; capturas reales del emulador. |

Las nueve pruebas instrumentadas tardaron aproximadamente 15 minutos en esta ejecución; el tiempo depende del equipo y del cálculo de contraseñas. Se comprobó el acceso normal a la app después de finalizar las pruebas. Estos resultados corresponden al AVD indicado y no certifican todos los modelos ni todas las versiones desde API 24.

Los reportes originales están en `app/build/reports/tests/testDebugUnitTest/`, `app/build/reports/androidTests/connected/` y `app/build/reports/lint-results-debug.html`. Se conservaron también las siguientes copias del entregable en la carpeta local `output/`:

| Archivo | Contenido |
| --- | --- |
| [RutaPaquete-2.0-debug.apk](output/RutaPaquete-2.0-debug.apk) | Aplicación instalable de desarrollo, versión 2.0. No es una publicación en una tienda. |
| [Índice de evidencias](output/evidencias/v2/INDICE.md) | Capturas de las pantallas, controles de acceso, perfil y CRUD. |
| [Reporte de pruebas locales](output/pruebas-v2/unitarias/index.html) | Resultado de las 18 pruebas JUnit. |
| [Reporte de pruebas instrumentadas](output/pruebas-v2/instrumentadas/index.html) | Resultado de las nueve pruebas del emulador. |
| [Reporte de Android Lint](output/pruebas-v2/lint.html) | Detalle de las advertencias de revisión estática. |

SHA-256 del APK entregado: `D096E0CCCB1DD1704A005791DF9D1609313B77CC98AF288B2E549838B2B343D5`.

`output/` está excluida de Git: estos enlaces corresponden a los archivos generados localmente que acompañan la entrega. Para iniciar una demostración se deben crear las cuentas propias siguiendo la sección 1.1; las cuentas ficticias de las pruebas no se utilizan en la ejecución normal.

### 3.6. Material auxiliar y límites del entregable

`README.md` describe la entrega. Los PDF de la raíz son material académico de referencia. Los scripts `scripts/build_report.py` y `scripts/package_delivery.py` se conservan como utilidades de la entrega anterior; su informe de diez páginas corresponde al alcance inicial y no documenta por sí solo las nuevas pantallas de la versión 2.0. No intervienen en la aplicación.

La persistencia es local y la sesión dura mientras vive el proceso. Los listados administrativos cargan los registros locales en memoria, adecuados para la demostración académica; una operación de mayor volumen necesitaría paginación. La baja registra quién y cuándo realizó el último cambio, pero no se implementa un historial completo de cada modificación. La ubicación, el estado y la entrega estimada son datos capturados manualmente.

La entrega incluye el flujo administrador y cliente descrito en la primera parte. La sincronización entre equipos, integración con transportistas y funciones comerciales de la segunda parte son posibles evoluciones, no servicios incluidos.
