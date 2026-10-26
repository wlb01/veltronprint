# Spec — Módulo 001: Conexión Bluetooth

> El CONTRATO del módulo. Describe comportamiento, NO implementación (eso va en plan.md).

## 1. Objetivo

Permitir que el usuario elija, entre los dispositivos ya emparejados en Android, la impresora del taller, que la app la recuerde y que pueda comprobar si responde. Ofrecer al resto de la app un único servicio que se conecta a la impresora solo para enviar bytes y se desconecta al terminar.

## 2. Pantallas

### Inicio

Primera pantalla al abrir la app. En este módulo solo contiene la tarjeta de la impresora; los módulos siguientes le agregan contenido (por ejemplo, la cola en el 006).

**Encabezado**
- Título "Veltron Print".

**Tarjeta "Impresora" — sin impresora guardada**
- Ícono de impresora.
- Texto: "No hay impresora vinculada".
- Botón principal: "Vincular impresora".

**Tarjeta "Impresora" — con impresora guardada**
- Nombre de la impresora (en negritas) y, debajo, su dirección MAC en texto pequeño.
- Indicador de estado con uno de estos valores:
  - "Sin probar" (gris)
  - "Probando…" (con indicador de progreso)
  - "Lista" (verde)
  - "No responde" (rojo)
- Debajo del estado, cuando ya hubo una prueba: "Última prueba: HH:MM".
- Botón principal: "Probar conexión" (deshabilitado mientras dice "Probando…").
- Botón secundario: "Cambiar impresora".

**Orientación:** en horizontal y en vertical la tarjeta se muestra centrada con un ancho máximo cómodo de lectura; no se estira a todo el ancho de la tablet.

### Elegir impresora

Se abre desde "Vincular impresora" o "Cambiar impresora".

**Encabezado**
- Botón de regresar y título "Elegir impresora".

**Ayuda**
- Texto: "Empareja primero la impresora en los ajustes de Bluetooth de Android. El PIN suele ser 0000 o 1234."
- Botón secundario: "Abrir ajustes de Bluetooth".

**Lista de dispositivos emparejados**
- Una fila por dispositivo: nombre y, debajo, dirección MAC.
- La impresora guardada actualmente, si existe, aparece marcada como "Actual".
- Al tocar una fila, se guarda ese dispositivo y se regresa a Inicio.

**Estado vacío**
- Texto: "No hay dispositivos emparejados." y el mismo botón "Abrir ajustes de Bluetooth".

**Orientación:** en horizontal, la ayuda queda a la izquierda y la lista a la derecha; en vertical, la ayuda queda arriba y la lista debajo.

### Aviso de permiso (Android 12 o superior)

Se muestra en lugar del contenido de la pantalla cuando falta el permiso de Bluetooth.
- Texto: "Veltron Print necesita el permiso de Dispositivos cercanos para conectarse a la impresora."
- Botón principal: "Dar permiso" (abre la solicitud del sistema).
- Si el usuario ya lo negó de forma permanente, el botón cambia a "Abrir ajustes de la app".

## 3. Flujo del usuario

**Flujo principal — vincular la impresora por primera vez**
1. Usuario abre la app →
2. Sistema verifica el permiso de Bluetooth; si falta, muestra el Aviso de permiso →
3. Usuario toca "Dar permiso" y lo acepta →
4. Sistema muestra Inicio con la tarjeta "No hay impresora vinculada" →
5. Usuario toca "Vincular impresora" →
6. Sistema verifica que Bluetooth esté encendido; si está apagado, muestra el diálogo del sistema para encenderlo →
7. Sistema muestra "Elegir impresora" con la lista de dispositivos emparejados →
8. Usuario toca la impresora →
9. Sistema la guarda, regresa a Inicio y la tarjeta muestra su nombre, su MAC y el estado "Sin probar".

**Flujo — probar la conexión**
1. Usuario toca "Probar conexión" →
2. Sistema cambia el estado a "Probando…" y deshabilita el botón →
3. Sistema se conecta, envía el comando de inicialización y se desconecta →
4. Sistema muestra "Lista" y "Última prueba: HH:MM"; si falló, muestra "No responde", la hora y el mensaje de error correspondiente.

**Flujo — la impresora no está emparejada todavía**
1. En "Elegir impresora", usuario toca "Abrir ajustes de Bluetooth" →
2. Sistema abre los ajustes de Bluetooth de Android →
3. Usuario empareja la impresora y regresa a la app →
4. Sistema vuelve a cargar la lista y la impresora ya aparece.

**Flujo — cambiar de impresora**
1. Usuario toca "Cambiar impresora" →
2. Sistema muestra "Elegir impresora" con la actual marcada →
3. Usuario toca otro dispositivo →
4. Sistema reemplaza la impresora guardada, regresa a Inicio y el estado vuelve a "Sin probar".

## 4. Lógica y reglas

- La app guarda una sola impresora: su nombre y su dirección MAC.
- La impresora guardada se conserva al cerrar la app y al reiniciar el equipo.
- La lista de "Elegir impresora" muestra todos los dispositivos emparejados en Android, ordenados alfabéticamente por nombre.
- Un dispositivo sin nombre se muestra como "Dispositivo sin nombre".
- La app no busca dispositivos cercanos ni empareja; el emparejamiento se hace en los ajustes de Android.
- La lista se vuelve a cargar cada vez que la pantalla "Elegir impresora" vuelve a primer plano.
- La app se conecta a la impresora solo cuando necesita enviarle datos y se desconecta en cuanto termina, haya tenido éxito o error.
- Solo existe una conexión a la vez; si llega otro envío mientras uno está en curso, espera su turno.
- Si la conexión no se establece en 10 segundos, el envío termina como "No responde".
- "Probar conexión" envía únicamente el comando de inicialización de la impresora: no imprime nada y no avanza el papel.
- El estado de la tarjeta es el último conocido durante la sesión; al abrir la app siempre empieza en "Sin probar".
- El servicio de envío devuelve un resultado que indica si los datos se enviaron o cuál de los errores de la sección 5 ocurrió.
- En Android 11 o menor no se pide ningún permiso al usuario, porque ahí los permisos de Bluetooth se otorgan al instalar la app.

## 5. Casos de error

| Error | Cuándo ocurre | Mensaje exacto al usuario |
|-------|---------------|----------------------------|
| Sin Bluetooth | El equipo no tiene Bluetooth | "Este equipo no tiene Bluetooth. Veltron Print no puede funcionar aquí." |
| Bluetooth apagado | El usuario rechaza encender Bluetooth en el diálogo del sistema | "Bluetooth está apagado. Enciéndelo para usar la impresora." |
| Permiso negado | El usuario niega el permiso de Dispositivos cercanos | "Veltron Print necesita el permiso de Dispositivos cercanos para conectarse a la impresora." |
| No responde | La conexión falla o pasan 10 segundos sin conectar | "La impresora no responde. Verifica que esté encendida, cerca del equipo y que ningún otro dispositivo esté conectado a ella." |
| Ya no emparejada | La impresora guardada fue desemparejada en los ajustes de Android | "La impresora guardada ya no está emparejada. Vuelve a emparejarla o elige otra." |
| Conexión perdida | La conexión se cae mientras se envían los datos | "Se perdió la conexión con la impresora mientras se enviaban los datos." |

Los mensajes de error se muestran dentro de la tarjeta de la impresora, debajo del estado, hasta la siguiente prueba exitosa.

## 6. Criterios de aceptación

- [ ] Al abrir la app sin impresora guardada, Inicio muestra "No hay impresora vinculada" y el botón "Vincular impresora".
- [ ] En Android 12 o superior sin permiso, la app muestra el Aviso de permiso y no la tarjeta de la impresora.
- [ ] Tras negar el permiso de forma permanente, el botón del aviso dice "Abrir ajustes de la app" y abre los ajustes de Veltron Print.
- [ ] En Android 11 (Honor X8) la app no pide ningún permiso y funciona igual.
- [ ] Con Bluetooth apagado, tocar "Vincular impresora" muestra el diálogo del sistema para encenderlo.
- [ ] "Elegir impresora" lista todos los dispositivos emparejados, en orden alfabético, con nombre y MAC.
- [ ] Sin dispositivos emparejados, se muestra "No hay dispositivos emparejados." con el botón "Abrir ajustes de Bluetooth".
- [ ] "Abrir ajustes de Bluetooth" abre los ajustes de Bluetooth de Android.
- [ ] Al regresar de los ajustes de Android, la lista incluye el dispositivo recién emparejado sin reiniciar la app.
- [ ] Al tocar la impresora, la app regresa a Inicio mostrando su nombre, su MAC y "Sin probar".
- [ ] La impresora guardada sigue apareciendo después de cerrar la app por completo y volverla a abrir.
- [ ] Con la impresora encendida, "Probar conexión" muestra "Lista" y la hora de la prueba.
- [ ] "Probar conexión" no imprime nada ni avanza el papel.
- [ ] Con la impresora apagada, "Probar conexión" muestra "No responde" y su mensaje exacto en máximo 10 segundos.
- [ ] Mientras dice "Probando…", el botón "Probar conexión" está deshabilitado.
- [ ] Después de una prueba, la impresora acepta inmediatamente una conexión desde otro equipo (la app no la deja ocupada).
- [ ] Si la impresora se desempareja en Android, "Probar conexión" muestra el mensaje de "Ya no emparejada".
- [ ] "Cambiar impresora" muestra la impresora actual marcada como "Actual" y permite reemplazarla.
- [ ] Inicio y Elegir impresora se ven correctamente en horizontal y en vertical, en la tablet y en el Honor.
- [ ] Ambas pantallas se ven correctamente en modo claro y en modo oscuro.
- [ ] Ningún contenido queda debajo de las barras del sistema.

## Módulos conectados

- Recibe datos de: ninguno.
- Entrega datos a: 002 (Motor ESC/POS), 006 (Cola y reintento) y 008 (Ajustes y prueba), mediante el servicio de envío de bytes.
