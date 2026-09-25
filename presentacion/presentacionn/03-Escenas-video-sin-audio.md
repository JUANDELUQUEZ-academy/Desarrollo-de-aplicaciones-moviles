# RutaPaquete: guía del video de demostración

## Formato y preparación

Grabar **12 escenas**, en el orden indicado. El video final debe durar aproximadamente **6–8 minutos**, aparte de los 8–10 minutos de exposición. Es una referencia de edición, no un límite obligatorio.

El video va **sin audio**: desactivar micrófono y captura de sonido del sistema, y exportar sin pista de audio. No añadir música ni voces. Juan y Humberto presentan en vivo. Los rótulos sugeridos son texto que aparece en el video, no frases para grabar con voz.

Preparar todo en el mismo emulador o teléfono. Utilizar datos ficticios y contraseñas exclusivas de demostración, manteniéndolas ocultas. No borrar datos de uso para empezar de cero. Si ya existe un administrador, utilizar esa cuenta y adaptar la escena 2, sin desinstalar la aplicación ni usar Wipe Data.

### Datos de ejemplo para mantener continuidad

| Elemento | Dato para la demostración |
| --- | --- |
| Administrador | `admin.demo`, solo si aún no existe uno. Si ya existe, usar el administrador disponible. |
| Cliente A | `ana.demo`, Ana Ruiz, 25 años, Monterrey, teléfono ficticio `5551234567`. |
| Cliente B | `luis.demo`, Luis García, 30 años, Puebla, teléfono ficticio `5557654321`. |
| Usuario nuevo de Ana | `ana.actualizada`, para demostrar la edición del perfil. |
| Código de paquete | `RP-DEMO-001`, o uno nuevo si ya se utilizó. Un código eliminado tampoco puede reutilizarse. |
| Descripción | Libros de programación. |
| Origen y destino | Monterrey y Puebla. |
| Estado inicial | Creado. Luego cambiarlo a En tránsito. |
| Entrega estimada | Una fecha válida posterior a la grabación, en formato AAAA-MM-DD. |
| Notas | Entregar en recepción. Luego cambiar a En camino al centro de distribución. |

Comprobar previamente que los usuarios de ejemplo no estén ocupados. Si lo están, elegir otros y mantener los mismos nombres durante todo el video. Las cuentas utilizadas en las pruebas automatizadas no están disponibles en la ejecución normal de la app.

## Escena 1. Inicio de la aplicación

**Duración editada:** 15 segundos. **Responsable sugerido:** Juan.

**Grabar:** abrir RutaPaquete y mantener visible la pantalla de inicio durante unos segundos. Mostrar los campos Usuario y Contraseña, Iniciar sesión y Crear nuevo usuario. Si corresponde, mostrar Configurar administrador local.

**Rótulo:** «RutaPaquete: demostración de la aplicación Android».

**Debe quedar claro:** el video muestra la aplicación funcionando, no solamente capturas o diapositivas.

## Escena 2. Configuración del administrador

**Duración editada:** 25–35 segundos. **Responsable sugerido:** Humberto.

**Grabar:** si no existe un administrador, entrar en Configurar administrador local, completar el formulario con datos ficticios y guardar. Mostrar el mensaje de creación y que el botón de configuración ya no aparece en el inicio.

**Si ya existe:** mostrar el acceso y un rótulo que indique que el administrador ya está configurado. No intentar crear otro ni borrar las cuentas actuales. La siguiente escena utiliza el registro normal de clientes.

**Rótulo:** «Configuración inicial: una cuenta administradora por dispositivo».

**Debe quedar claro:** la cuenta administradora no utiliza una contraseña predeterminada y el registro público crea clientes.

## Escena 3. Registro y validaciones de los clientes

**Duración editada:** 60–75 segundos. **Responsable sugerido:** Juan.

**Grabar:** abrir Crear nuevo usuario y pulsar Enviar sin completar el formulario para mostrar los errores. Llenar los datos de Ana. Introducir 17 en edad e intentar guardar. Mostrar el rechazo y corregir la edad a 25. Introducir contraseñas diferentes, intentar guardar y mostrar el error. Corregir la confirmación y completar el registro. Mantener visible el mensaje de éxito.

Abrir otro registro, escribir un usuario de prueba distinto y pulsar Cancelar para mostrar el regreso al acceso sin alta. Registrar después a Luis con sus datos válidos. Se puede acelerar la escritura repetida, dejando visibles los botones y el resultado de cada operación.

**Rótulos:** «Campos obligatorios», «Solo mayores de edad», «Las contraseñas deben coincidir», «Cliente registrado».

**Debe quedar claro:** hay dos clientes distintos y cancelar un formulario no equivale a guardarlo. No ocultar los mensajes de validación con los rótulos.

## Escena 4. Inicio de sesión y roles

**Duración editada:** 20–30 segundos. **Responsable sugerido:** Humberto.

**Grabar:** introducir el usuario administrador con una contraseña incorrecta y mostrar Usuario o contraseña incorrectos. Corregir la contraseña e iniciar sesión. Mantener visible el título Administración.

**Rótulo:** «Las credenciales y el rol determinan el acceso».

**Debe quedar claro:** el acceso depende de una cuenta registrada. La contraseña debe permanecer oculta.

## Escena 5. Clientes disponibles y creación del paquete

**Duración editada:** 45–60 segundos. **Responsable sugerido:** Humberto.

**Grabar:** abrir Ver clientes registrados y mostrar a Ana y Luis. Volver a Ver paquetes y pulsar Crear paquete. Intentar guardar el formulario vacío para mostrar que requiere datos y destinatario. Completar el código, elegir a Ana, introducir descripción, origen, destino, estado, fecha estimada y notas. Desplazarse hasta Guardar paquete y guardar. Mostrar la tarjeta creada.

**Rótulo:** «CREATE: registrar un paquete y asignarlo a un cliente».

**Debe quedar claro:** se selecciona un cliente existente. La aplicación no inventa un destinatario ni guarda un paquete sin asignación.

## Escena 6. Consulta, filtro y actualización administrativa

**Duración editada:** 35–45 segundos. **Responsable sugerido:** Humberto.

**Grabar:** filtrar el listado por el código `RP-DEMO-001`. Abrir Ver / editar paquete, cambiar el estado a En tránsito y actualizar las notas. Guardar, volver a abrir y mostrar que los cambios permanecen. Si se desea comprobar el código único, intentar crear otro paquete con el mismo código y los demás campos válidos, mostrar el rechazo y cancelar ese segundo formulario.

**Rótulo:** «READ y UPDATE: consultar y actualizar la información».

**Debe quedar claro:** la actualización modifica el mismo paquete, sin crear un registro adicional. El código del ejemplo debe conservarse para las siguientes escenas.

## Escena 7. Búsqueda del cliente correcto

**Duración editada:** 30–40 segundos. **Responsable sugerido:** Juan.

**Grabar:** cerrar la sesión administradora e iniciar sesión como Ana. Introducir el código y pulsar Buscar paquete. Mostrar el detalle completo con un desplazamiento lento si es necesario: destinatario, estado, origen, destino, entrega estimada, notas y fechas. Mantener visibles el estado En tránsito y las notas actualizadas.

**Rótulo:** «El cliente consulta la información de su paquete».

**Debe quedar claro:** la información coincide con lo que guardó el administrador. No representa GPS ni actualizaciones automáticas de una paquetería.

## Escena 8. Búsqueda desde otra cuenta

**Duración editada:** 25–35 segundos. **Responsable sugerido:** Juan.

**Grabar:** volver al rastreo, cerrar sesión e iniciar como Luis. Buscar exactamente el mismo código. Mantener visible «Ese paquete no existe.» durante al menos tres segundos. Buscar también un código inventado para mostrar el mismo mensaje.

**Rótulo:** «Conocer el código no permite consultar paquetes ajenos».

**Debe quedar claro:** el paquete existe, pero sigue asignado a Ana. El sistema no revela sus datos a Luis.

## Escena 9. Perfil y persistencia

**Duración editada:** 45–60 segundos. **Responsable sugerido:** Juan.

**Grabar:** cerrar la sesión de Luis e ingresar como Ana. Abrir Mi perfil. Cambiar el usuario a `ana.actualizada`, el nombre a Ana María y la dirección. Introducir la contraseña actual y una nueva contraseña con su confirmación. Guardar y mostrar el saludo actualizado.

Cerrar la aplicación por completo, volver a abrirla e iniciar sesión con el usuario y la contraseña nuevos. Buscar el mismo paquete y mostrar que Ana conserva el acceso. Para que la prueba de cierre sea inequívoca, se puede usar Forzar detención desde la información de la app. No pulsar Borrar almacenamiento ni desinstalar.

**Rótulos:** «Datos personales editables», «Nueva contraseña verificada», «La cuenta y sus paquetes permanecen guardados».

**Debe quedar claro:** el cambio de usuario conserva la relación con los paquetes. La dirección del perfil no cambia automáticamente el destino del envío.

## Escena 10. Reasignación del paquete

**Duración editada:** 35–50 segundos. **Responsable sugerido:** Humberto.

**Grabar:** cerrar sesión, ingresar como administrador, abrir el paquete y cambiar el destinatario de Ana a Luis. Guardar. Entrar como `ana.actualizada` y buscar el código para mostrar el rechazo. Después ingresar como Luis, buscarlo y mostrar el detalle con Luis como destinatario.

**Rótulo:** «La reasignación cambia qué cliente puede consultar el paquete».

**Debe quedar claro:** Ana pierde el acceso y Luis lo obtiene. Señalar mediante rótulos qué cuenta está activa en cada corte.

## Escena 11. Eliminación con confirmación

**Duración editada:** 35–45 segundos. **Responsable sugerido:** Humberto.

**Grabar:** volver a la cuenta administradora, abrir el paquete y pulsar Eliminar paquete. Mostrar el diálogo. Primero pulsar Cancelar y comprobar que el paquete continúa disponible. Volver a pulsar Eliminar paquete y confirmar. Mostrar que desaparece del listado. Ingresar como Luis y buscar el código para mostrar «Ese paquete no existe.».

**Rótulo:** «DELETE: baja lógica después de confirmar».

**Debe quedar claro:** cancelar conserva el envío y confirmar lo retira de las consultas. La baja lógica mantiene el registro interno y reserva el código.

## Escena 12. Cierre de la demostración

**Duración editada:** 10–15 segundos. **Responsables sugeridos:** Juan y Humberto.

**Grabar:** cerrar sesión y mostrar el inicio de RutaPaquete. Mantener la imagen estable para cerrar el video.

**Rótulo:** «RutaPaquete: administrador, cliente y CRUD funcionando en una base local».

**Debe quedar claro:** el recorrido terminó y no queda abierta una sesión de prueba. El agradecimiento y las preguntas se realizan en vivo, después del video.

## Cómo grabar y editar sin perder claridad

1. Hacer una prueba corta para comprobar que el emulador y los textos se vean nítidos. Capturar solo la zona necesaria y ocultar notificaciones o información personal del escritorio.
2. Mantener la aplicación vertical durante las escenas. Si el video final usa formato horizontal, colocar la imagen completa sin estirarla ni cortar campos o botones.
3. Separar la grabación por escenas. Repetir únicamente la toma que falle. Mantener la continuidad de las cuentas, el código y el estado del paquete.
4. Dejar cada resultado importante visible al menos tres segundos. Desplazarse lentamente al mostrar formularios y detalles.
5. Recortar o acelerar tiempos de escritura y esperas. Si se acelera una operación, usar un rótulo como «Espera abreviada». Conservar el momento en que se pulsa guardar y el resultado real. No presentar una espera recortada como respuesta instantánea.
6. Usar rótulos cortos y grandes, fuera de los mensajes o controles que se están mostrando. Identificar los cambios de sesión como «Administrador», «Cliente Ana» y «Cliente Luis».
7. Exportar un MP4 local sin pista de audio. Reproducirlo completo para verificar imagen, orden, duración y ausencia de sonido. No depender de Internet para mostrarlo en clase.
8. Guardar una copia del video, de la presentación y del diálogo en una memoria USB y en el equipo de exposición. Abrir y probar esos archivos antes de presentar. No es necesario enviar ni publicar el video para tener ese respaldo.

## Transición desde la presentación

En la diapositiva 12, Humberto dice en vivo: «Ahora queremos presentarles una muestra del funcionamiento de nuestra aplicación». Juan introduce brevemente las comprobaciones finales y se reproduce el video sin audio. El diálogo completo está en `02-Dialogo-Juan-y-Humberto.md`.
