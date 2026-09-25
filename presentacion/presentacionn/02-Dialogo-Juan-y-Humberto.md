# RutaPaquete: diálogo de la presentación

**Formato:** exposición en vivo de Juan y Humberto. **Duración objetivo:** 8–10 minutos, sin contar el video. **Presentación:** 12 diapositivas.

Juan presenta las diapositivas 1, 2, 4, 7, 8 y 11. Humberto presenta las diapositivas 3, 5, 6, 9 y 10, y abre la transición de la 12. Ambos participan en el cierre. Los tiempos son orientativos e incluyen pequeñas pausas para señalar las capturas.

El video posterior no lleva audio, música ni voces grabadas. Este documento es el diálogo de la exposición presencial, no una locución para incorporar al video. El mismo diálogo está incluido en las notas de la presentación.

## Diapositiva 1. RutaPaquete

**Juan. Aproximadamente 35 segundos.**

Buen día, profesora. Somos Juan y Humberto y vamos a presentar RutaPaquete, nuestra aplicación Android para administrar y consultar envíos. La desarrollamos en Kotlin y esta versión ya permite registrar clientes, gestionar paquetes y consultar su información según la cuenta que inicia sesión. Primero explicaremos el problema que buscamos atender, después mostraremos las funciones y la parte técnica. Al final presentaremos una demostración grabada de la aplicación, sin audio, como respaldo del funcionamiento.

## Diapositiva 2. El problema y la propuesta

**Juan. Aproximadamente 50 segundos.**

La idea parte de una situación común en un negocio pequeño: los pedidos y los datos de sus clientes pueden quedar repartidos entre mensajes, hojas de cálculo y anotaciones. Eso dificulta saber a quién corresponde un paquete y cuál es su estado más reciente. RutaPaquete reúne esa información y relaciona cada envío con una cuenta cliente. Buscamos facilitar la consulta y las correcciones, además de evitar que otra persona consulte un paquete solo por conocer su código. Esta versión demuestra la idea de negocio. Todavía no hemos medido sus beneficios en una empresa real. Humberto explicará cómo dividimos las responsabilidades.

## Diapositiva 3. Administrador y cliente

**Humberto. Aproximadamente 40 segundos.**

La aplicación tiene dos roles. El administrador puede consultar a los clientes registrados y gestionar los paquetes. El cliente puede buscar sus propios envíos y actualizar su información personal. La aplicación determina el rol al iniciar sesión y abre la pantalla correspondiente. En el primer uso, el responsable configura una cuenta administradora con sus propias credenciales. Después desaparece esa opción y el registro normal crea únicamente clientes. No entregamos una contraseña predeterminada. Para esta demostración, ambos roles utilizan la misma base de datos dentro del mismo dispositivo.

## Diapositiva 4. Registro e inicio de sesión

**Juan. Aproximadamente 55 segundos.**

El registro solicita usuario, nombre, apellidos, edad, dirección, teléfono y contraseña con su confirmación. Agregamos el nombre de usuario porque es el dato que necesitamos para iniciar sesión. La aplicación rechaza los campos obligatorios vacíos, los usuarios repetidos y las contraseñas que no coinciden. La edad debe estar entre dieciocho y ciento veinte años. Cuando los datos son válidos, el formulario guarda la cuenta y regresa al acceso. Si cancelamos, no guarda el registro. Al iniciar sesión, la aplicación comprueba las credenciales y muestra un mensaje de error si son incorrectas. Los formularios permiten desplazarse cuando aparece el teclado.

## Diapositiva 5. CRUD de paquetes

**Humberto. Aproximadamente 50 segundos.**

El módulo administrativo implementa las cuatro operaciones del CRUD. Create corresponde a crear un paquete con un código único y un cliente destinatario. Read permite consultar y filtrar los paquetes guardados. Update permite corregir su información, cambiar el estado o modificar el destinatario. Delete retira un paquete después de pedir confirmación. En este último caso utilizamos una baja lógica: el registro permanece en la base, pero deja de aparecer en las consultas. Además, reservamos su código para no reutilizarlo. Si el administrador cancela el diálogo de eliminación, el paquete continúa disponible. Todo este proceso funciona desde las pantallas de la aplicación.

## Diapositiva 6. Asignación y reasignación

**Humberto. Aproximadamente 40 segundos.**

Cada paquete pertenece a un cliente registrado. El administrador lo selecciona de una lista, y la aplicación no permite guardar un envío sin destinatario ni asignarlo a una cuenta administradora. Guardamos la relación mediante el identificador interno del cliente. Por eso, si cambia su nombre de usuario, no pierde sus paquetes. También podemos reasignar un envío. En ese momento, el destinatario anterior deja de encontrarlo y el nuevo obtiene acceso. Este control relaciona la gestión administrativa con la consulta que utiliza el cliente. Juan mostrará qué ocurre al buscar el código.

## Diapositiva 7. Consulta del cliente

**Juan. Aproximadamente 50 segundos.**

El cliente introduce el código de rastreo y pulsa Buscar paquete. La aplicación comprueba que el envío exista, permanezca activo y pertenezca a esa cuenta. Cuando cumple las tres condiciones, muestra la descripción, el destinatario, el estado, el origen, el destino, la entrega estimada, las notas y las fechas de registro y actualización. Si el código no existe, corresponde a otra persona o el paquete fue eliminado, el mensaje es el mismo: Ese paquete no existe. Así evitamos revelar información de otros clientes. La pantalla de detalle vuelve a verificar la relación con la cuenta antes de mostrar los datos.

## Diapositiva 8. Edición del perfil

**Juan. Aproximadamente 45 segundos.**

Desde Mi perfil, el cliente puede modificar su usuario y sus datos personales. Para guardar necesita confirmar su contraseña actual. También puede elegir una nueva contraseña, pero ese cambio es opcional: si deja los dos campos nuevos vacíos, conserva la anterior. Al guardar, la cuenta mantiene su identificador y sus paquetes, aunque cambie el nombre de usuario. La aplicación utiliza las nuevas credenciales en el siguiente acceso. Es importante distinguir la dirección del perfil del destino de un envío ya creado. Cambiar el perfil no modifica automáticamente ese destino. Si hace falta corregirlo, debe hacerlo el administrador en el paquete.

## Diapositiva 9. Herramientas y base de datos

**Humberto. Aproximadamente 55 segundos.**

Desarrollamos la lógica en Kotlin dentro de Android Studio. Las interfaces usan XML y componentes Material. View Binding nos permite acceder a las vistas desde el código con referencias tipadas. Para almacenar la información utilizamos Room sobre SQLite, con dos tablas principales: usuarios y paquetes. Un cliente puede tener varios paquetes, y cada paquete tiene un destinatario. Los repositorios concentran las operaciones y las comprobaciones de permisos. Las corrutinas permiten ejecutar el trabajo de datos y las comprobaciones de contraseña sin bloquear la interfaz. También incorporamos una migración de la base para añadir los nuevos roles y paquetes conservando las cuentas de la primera versión.

## Diapositiva 10. Seguridad y pruebas

**Humberto. Aproximadamente 50 segundos.**

La aplicación no guarda las contraseñas en texto plano. Conserva un hash y un salt aleatorio por cuenta, y verifica los permisos tanto en las pantallas como en los repositorios. Al cerrar sesión elimina la sesión activa, pero mantiene los registros de la base. En la verificación del veinticinco de septiembre, pasaron dieciocho pruebas locales y nueve pruebas instrumentadas en el emulador, sin fallos. Revisamos validaciones, persistencia, migración, permisos y el recorrido completo de las pantallas. Estos resultados corresponden al emulador probado, no a todos los teléfonos Android. La revisión estática también conserva advertencias que documentamos en el README.

## Diapositiva 11. Alcance actual y evolución

**Juan. Aproximadamente 45 segundos.**

La versión actual completa el flujo local de administrador y cliente. Las cuentas y los paquetes permanecen en el mismo dispositivo al cerrar la aplicación. El administrador actualiza manualmente el estado, por lo que no mostramos una ubicación real del transporte. Para utilizarla entre teléfonos distintos, el siguiente paso sería desarrollar una API, una base central y autenticación en servidor. Después podríamos evaluar notificaciones e integración con transportistas. Esas funciones todavía no forman parte de esta entrega. Nuestra propuesta comercial apunta a pequeños negocios de reparto, pero primero sería necesario validar sus necesidades y el uso real del sistema.

## Diapositiva 12. Demostración de funcionamiento

**Humberto y Juan. Aproximadamente 25 segundos.**

**Humberto:** Ahora queremos presentarles una muestra del funcionamiento de nuestra aplicación. En el video veremos cómo registramos clientes, creamos un paquete y lo consultamos desde la cuenta correspondiente.

**Juan:** También mostraremos la edición del perfil, la reasignación y la eliminación. El video no tiene audio y nos sirve como respaldo para presentar el funcionamiento aunque tengamos un problema con el emulador durante la exposición.

**Acción:** dejar de avanzar las diapositivas y reproducir el video local. No hace falta grabar estas frases dentro del video.

### Después del video

**Juan:** Con esta demostración concluimos el recorrido de RutaPaquete. Gracias por su atención.

**Humberto:** Quedamos atentos a sus preguntas.

## Preparación para exponer

Ensayar una vez con cronómetro. El objetivo aproximado es de nueve minutos, con pausas breves para señalar las capturas. Si el tiempo se alarga, resumir los ejemplos de las diapositivas 2 y 11. No omitir la restricción de consulta por propietario ni la aclaración de que la base funciona localmente.

Una persona puede avanzar las diapositivas mientras la otra habla. Al llegar a la diapositiva 12, tener el archivo de video abierto y pausado al inicio. Los tiempos del diálogo no incluyen la reproducción de la demostración.
