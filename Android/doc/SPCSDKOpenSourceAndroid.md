# SPC SDK OpenSource Android Example

En este documento se explica el funcionamiento y uso del SDK por medio de un ejemplo.

# Funcionamiento general
Para gestionar la comunicación con el dispositivo esta aplicación usa 3 tipos de métodos asíncronos: ` `, `Callback` y `BroadcastReceiver`.
Tanto el funcionamiento interno del modelo como la comunicación con la clase que lo gestiona (`ExampleManager`) se realiza mediante `Callback` y en menor medida con `Handler` y `BroadcastReceiver`, sin embargo en la aplicación de ejemplo se han utilizado íntegramente `BroadcastReceiver` para comunicar las actividades y los fragmentos con la clase que gestiona el modelo (`ExampleManager`).

# Clases (Controller)
Las clases contenidas en controller sirven para mostrar las vistas e interactuar con el usuario.

### MainActivity
Layout -> `activity_main.xml` y `listitem_device.xml`

Actividad principal que lista los dispositivos. Mediante un botón inicia la búsqueda de dispositivos, una vez seleccionado uno cambia a la actividad `ShowDeviceActivity`. Esta actividad también se encarga de gestionar los eventos de bluetooth.

### ShowDeviceActivity
Layout -> `activity_show_device.xml`

Esta actividad muestra en su cabecera el id/name de la pulsera seleccionada y su dirección MAC, por medio de un botón muestra el estado de la conexión y posibilita conectarse o desconectarse de la pulsera. También es la encargada de mostrar los diferentes fragmentos que contienen la funcionalidad de la aplicación, seleccionándolos mediante un Tab. Esta actividad también se encarga de gestionar los eventos de bluetooth.

### GetSetTimeFragment
Layout -> `fragment_get_set_time.xml`

Este fragmento se encarga de realizar las llamadas `setTime` y `getTime` mediante un par de botones y gestionar las respuestas correspondientes, mostrando la información requerida o mostrando una alerta de que la acción ha sido completada.


### RealTimeMeterModeFragment
Layout -> `fragment_real_time_meter_mode.xml`

Este fragmento se encarga de realizar la llamada `startRealTimeMeterMode` y gestiona la respuesta mostrando en pantalla la información que le envía la pulsera.

### GetSetPersonalInfoFragment
Layout -> `fragment_get_set_personal_info.xml`

Este fragmento se encarga de realizar las llamadas `setPersonalInformation` y `getPersonalInformation` mediante un par de botones y gestionar las respuestas correspondientes, mostrando la información requerida o mostrando una alerta de que la acción ha sido completada.

### GetSetGoalFragment
Layout -> `fragment_get_set_goal.xml`

Este fragmento se encarga de realizar las llamadas `setTargetSteps` y `getTargetSteps` mediante un par de botones y gestionar las respuestas correspondientes, mostrando la información requerida o mostrando una alerta de que la acción ha sido completada.

### DetailInfoFragment
Layout -> `fragment_detail_info.xml`

Este fragmento se encarga de realizar la llamada `getDetailActivityData` y gestiona las respuestas que envía la pulsera mostrando en pantalla la información.


# Clases (Model)
El paquete model incorpora un paquete `SPCFitSDK` con las clases necesarias para la comunicación con la pulsera y una clase  de ejemplo (`ExampleManager`).

### ExampleManager
Clase de ejemplo que gestiona la comunicación de los controladores con el modelo.

En el constructor de esta clase se instancia una variable de tipo `ActivityTrackerManager` al que se le pasan como parámetros un `Context` y un `ActivityTrackerCallback`.

Este `ActivityTrackerCallback` es el canal por el que el modelo devuelve las respuestas del dispositivo, por cada una de esas respuestas se lanza un `Intent` para que los controladores que hayan registrado la acción de ese `Intent` sean notificados del evento producido.

Una vez que se ha logrado una conexión con un dispositivo, el método connected del `ActivityTrackerCallback` recibe un `ActivityTracker` que se guarda en una variable para que los controladores puedan realizar las llamadas al dispositivo a través de esta.


### ActivityTrackerManager
Esta clase se encarga de buscar dispositivos, conectarse y desconectarse de ellos.

#### Métodos públicos:

- `ActivityTrackerManager(Context context, ActivityTrackerCallback activityTrackerCallback)`
	Este método es el constructor, guarda los parámetros que recibe en variables de la clase, obtiene el adaptador de bluetooth y comprueba que el móvil tenga bluetooth o bluetoothLE y lanza la correspondiente notificación (`NO_BLUETOOTH` o `NO_BLE`).

- `boolean connect(final String address, final String serialNumber)`
	Método publico para ejecutar al método privado`connectToDeviceGatt`. Devuelve true si ha podido iniciar la conexión y false si no puede conectarse al bluetooth.

- `boolean connectToSerialNumber(final String serialNumber)`
	Método para conectarse a un dispositivo con el número de serie (`serialNumber`), el funcionamiento se basa en rastrear los dispositivos y comprobar si el `device.getName()` se corresponde con el numero de serie, en caso de buscar durante 20 segundos y no encontrarlo lanza una notificación (`DEVICE_NOT_FOUND`), en caso de encontrarlo se conecta a el.
	Devuelve true si ha podido iniciar la búsqueda y false si no puede conectarse al bluetooth.

- `disconnect()`
	Este método cierra la conexión con el dispositivo.

- `boolean foundDevices()`
	Método que inicia una búsqueda de dispositivos filtrando por el nombre del dispositivo y cuando encuentra uno lanza una notificación con la información del mismo. Devuelve true si ha podido iniciar la búsqueda y false si no ha podido conectarse al bluetooth.

### ActivityTracker
Esta clase se encarga de gestionar la comunicación con el dispositivo.
Los métodos de comunicación con la pulsera, a parte de los parámetros necesarios del propio método, incluyen un parámetro priority que sirve para establecer la prioridad a la hora de tomar posición en la cola de llamadas a la pulsera. Estas prioridad puede ser alta (`ActivityTracker.HIGH_PRIORITY`) o baja (`ActivityTracker.LOW_PRIORITY`).
Ejemplo: Si en la cola hay X llamadas con prioridad baja y entra una nueva de prioridad baja también se pondrá al final sin embargo si es una de prioridad alta sera la primera en ejecutarse.

#### Métodos públicos:

- `ActivityTracker(String address, String serialNumber, ActivityTrackerCallback activityTrackerCallback)`
	Este método es el constructor, guarda los parámetros que recibe en variables de la clase, obtiene el adaptador de bluetooth , inicia la cola y establece el modelo de pulsera.

- `int getModel()`
Método que devuelve que tipo de pulsera es *SPC_FIT_PRO*, *SPC_FIT* o *SPC_FIT_PULSE*.

- `safeBondingSavePassword(String password, int priority)`
	Este método envía a la pulsera el código para realizar el bonding, la pulsera inicia el proceso de bonding y se invoca el método `safeBondingSavePassword` del Callback.
	**9602N** si se ejecuta este método siempre se invoca el método `safeBondingSavePassword` del Callback con error a false, aunque al no tener implementado el sistema de bonding es información errónea.
	**9603N** durante el proceso de bonding (10 segundos) si se mueve la pulsera 2 veces se invocara el método `safeBondingStatus` del Callback con error a false, y la vinculación estará realizada, si no se mueve no pasa nada.
	**9604N** esta pulsera invoca el método `safeBondingSavePassword` del Callback, durante el proceso de bonding (10 segundos) si se pulsa la pantalla se invocara el método `safeBondingStatus` del Callback con error a false, y la vinculación estará realizada, si no se pulsa no pasa nada.

- `safeBondingSendPassword(String password, int priority)`
	Este método envía a la pulsera un código para verificar, la pulsera comprueba si es correcto o no y se invoca el método `safeBondingSendPassword` del Callback.
	**9602N** si se ejecuta este método siempre se invoca el método `safeBondingSendPassword` del Callback con error a false, aunque al no tener implementado el sistema de bonding es información errónea.

- `safeBondingStatus(int priority)`
	**9602N** si se ejecuta este método siempre se invoca el método `safeBondingStatus` del Callback con error a false, aunque al no tener implementado el sistema de bonding es información errónea.
	**9603N** al ejecutarse si no esta vinculada se invoca el método `safeBondingStatus` del Callback con error a true, y si esta vinculada se invoca el método `safeBondingStatus` del Callback con error a false.
	**9604N** si se ejecuta este método no se recibe respuesta por parte de la pulsera.

- `getTime(int priority)`
	Este método envía a la pulsera una petición para obtener la fecha, la pulsera response la fecha que ella tiene y se invoca el método `getTime` del Callback.

- `setTime(Calendar calendar, int priority)`
	Este método envía una fecha, la pulsera response responde que la ha cambiado y se invoca el método `setTime` del Callback.

- `getPersonalInformation(int priority)`
	Este método pregunta a la pulsera por la información personal, la pulsera response y se invoca el método `getPersonalInformation` del Callback.

- `setPersonalInformation(byte sex, byte age, byte height, byte weight, byte stride, int priority)`
	Este método envía información personal a la pulsera, esta la guarda, responde y se invoca el método `setPersonalInformation` del Callback.

- `getTargetSteps(int priority)`
	Este método pregunta a la pulsera por el objetivo, la pulsera response y se invoca el método `getTargetSteps` del Callback.

- `setTargetSteps(int goal, int priority)`
	Este método envía un objetivo a la pulsera, esta la guarda, responde y se invoca el método `setTargetSteps` del Callback.

- `getSleepMonitorMode(int priority)`
	Exclusivo para pulseras **9603N** y **9604N**.
	Este método pregunta a la pulsera si esta en modo sueño, esta responde y se invoca el método `getSleepMonitorMode` del Callback.

- `switchSleepMonitorMode(int priority)`
	Exclusivo para pulseras **9603N** y **9604N**.
	Este método le pide a la pulsera cambiar de modo sueño a modo normal y viceversa, esta realiza la acción y se invoca el método `switchSleepMonitorMode` del Callback.

- `startRealTimeMeterMode(int priority)`
	Este método le pide a la pulsera ponerse en modo en vivo, esta realiza la acción y cada vez que detecte un movimiento se invocará el método `realTimeMeterMode` del Callback.

	**9602N** y **9603N** si se ejecuta este método envía una respuesta inmediata sin que se hayan tenido que producir pasos con todos los datos a 0.

	**9604N** si se ejecuta este método envía una respuesta inmediata sin que se hayan tenido que producir pasos con la información actual.

- `stopRealTimeMeterMode(int priority)`
	Este método le pide a la pulsera que pare el modo en vivo, esta realiza la acción y se invoca el método `stopRealTimeMeterMode` del Callback.

- `getCurrentActivityInformation(int priority)`
	Este método le pide a la pulsera la información de la actividad actual, esta responde y se invoca el método `getCurrentActivityInformation` del Callback.

- `getTotalActivityData(int day, int priority)`
	Este método le pide a la pulsera la información de la actividad de un día, siendo hoy el día 0 y los días anteriores que hubieran registrado una actividad 1, 2 etc.. con un máximo de 29, esta responde y se invoca el método `getTotalActivityData0` del Callback con parte de la información y el método `getTotalActivityData1` del Callback con la parte restante.

- `getDetailActivityData(int day, int priority)`
	Este método le pide a la pulsera la información de la actividad detallada de un día, siendo hoy el día 0 y los días anteriores que hubieran registrado una actividad 1, 2 etc.. con un máximo de 29, esta devuelve una cantidad de respuestas indeterminada. Si no hay información detallada se invoca el método `getDetailActivityDataWithOutData` del Callback. Si hay información enviará 96 respuestas referidas a la actividad del día por cada 15 minutos, estas respuestas pueden contener información de la actividad , en cuyo caso se invocaría el método `getDetailActivityDataActivityData` o por el contrario información de sueño y se invocaría el método `getDetailActivityDataSleepQuality`.

- `startECGMode(int priority)`
	Exclusivo para pulseras **9604N**.
	Este método le pide a la pulsera que active el modo de electrocardiograma, cuando este modo esta activo la pulsera envía la información cuando se este tomando el pulso y se invoca los métodos `ECGMode` y `ECGModeRate` del Callback.

- `stopECGMode(int priority)`
	Exclusivo para pulseras **9604N**.
	Este método le pide a la pulsera que pare el modo de electrocardiograma, esta realiza la acción y se invoca el método `stopECGMode` del Callback.

- `getECGData(int index, int priority)`
	Exclusivo para pulseras **9604N**.
	Este método le pide a la pulsera la información de electrocardiograma que tenga almacenada en un indice, siendo el 0 el mas actual y con un indice máximo de 9, esta responde y se invoca el método `getECGData` del Callback.

- `deleteECGData(int priority)`
	Exclusivo para pulseras **9604N**.
	Este método le pide a la pulsera borrar los datos de electrocardiograma que tenga almacenados, esta realiza la acción y se invoca el método `deleteECGData` del Callback.

### ActivityTrackerCallback
Esta clase abstracta define las respuestas que puede invocar el ActivityTracker.

- `connected(ActivityTracker tracker)`
- `disconnected()`
- `deviceToConnectNotFound()`
- `safeBondingSavePassword()`
- `safeBondingSendPassword(boolean error)`
- `safeBondingStatus(boolean error)`
- `getTime(Calendar calendar)`
- `setTime()`
- `getPersonalInformation(int male, int age, int height, int weight, int stride, String deviceId)`
- `setPersonalInformation()`
- `getTargetSteps(int goal)`
- `setTargetSteps()`
- `getSleepMonitorMode(boolean sleep)`
- `switchSleepMonitorMode()`
- `realTimeMeterMode(int steps, int aerobicSteps, int cal, int km, int activityTime)`
- `stopRealTimeMeterMode()`
- `getCurrentActivityInformation(int steps, int aerobicSteps, int cal, int km, int activityTime)`
- `getTotalActivityData0(byte dayIndex, Calendar calendar, int steps, int aerobicSteps, int cal)`
- `getTotalActivityData1(byte dayIndex, Calendar calendar, int km, int activityTime)`
- `getDetailActivityDataWithOutData()`
- `getDetailActivityDataActivityData(int index, Calendar calendar, int steps, int aerobicSteps, int cal, int km)`
- `getDetailActivityDataSleepQuality(int index, Calendar calendar, HashMap<Calendar, Integer> hashMap)`
- `ECGMode(byte[] bytes)`
- `ECGModeRate(int heartRate)`
- `stopECGMode()`
- `deleteECGData()`
- `getECGData(Calendar calendar, int heartRate)`

### ActivityTrackerSeeker
Esta clase se encarga internamente del proceso de buscar dispositivos.

### Queue
Esta clase se encarga internamente de encolar las llamadas al dispositivo para que se produzcan una a una.

### QueueAction
Esta clase define el objeto que requiere la cola para su funcionamiento.

### QueueFIFOEntry
Esta clase define la prioridad de los objetos QueueAction en la cola.