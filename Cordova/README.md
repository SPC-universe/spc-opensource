# Plugin Cordova para la pulsera SPC Fitness

## Clonar Proyecto

    $ git clone https://github.com/SPC-universe/spc-fitness-cordova.git

## Crear Proyecto Cordova

```bash
    cordova create <PATH> [ID [NAME [CONFIG]]] [options] [PLATFORM...]
    cordova create carpetaContenedora com.example.id nombreApp
```

#### Instalar el plugin
    
Dentro de la carpeta que se ha creado ejecutar

```bash
    cordova plugin add <pluginid>|<directory>|<giturl>
    cordova plugin add ../rutaDelPlugin
```

#### Instalar las plataformas iOS y Android

```bash
    cordova platform add ios
    cordova platform add android
```
    
#### Ejecutar la aplicación

```bash
    cordova run ios
    cordova run android
```

#### Mas informacion sobre Cordova

For more information on setting up Cordova see [the documentation](http://cordova.apache.org/docs/en/4.0.0/guide_cli_index.md.html#The%20Command-Line%20Interface)

For more info on plugins see the [Plugin Development Guide](http://cordova.apache.org/docs/en/4.0.0/guide_hybrid_plugins_index.md.html#Plugin%20Development%20Guide)
    
## Funcionalidad

En el archivo `www/js/index.js` se deben implementar estas llamadas

#### findDevices

Busca los dispositivos cercanos y envia una respuesta con un los Ids de los dispositivos en formato `JSONarray` (`deviceList`).

```js
    findDevices: function() {
        SPCFit.findDevices(function(deviceList) {
            ...
        });
    }
```

#### connect

Recibe un `String` (`deviceId`) y se conecta a el, envia una respuesta de confirmación.

```js
    connect: function(deviceId) {
        SPCFit.connect(deviceId, function() {
            ...
        });
    }
```

#### setTime

Recibe un `String` (`time`) en formato `yyyy-MM-dd HH:mm:ss` y devuelve una respuesta de confirmación (`success`) en caso de poder cambiarla o de error (`error`) en caso contrario.

```js
    setTime: function() {
        var time = $('#timeResponse').val();

        SPCFit.setTime(time, function(success) {
            ...
        }, function(error) {
            ...
        });
    }
```

#### getTime

Devuelve un `String` (`time`) en formato `yyyy-MM-dd HH:mm:ss` con la hora del dispositivo.

```js
    getTime: function() {
        SPCFit.getTime(function(time) {
            ...
        });
    }
```

#### setPersonalInformation

Recibe un `JSONObject` (`info`) con la información del usuario y la cambia.

```js
    setPersonalInformation: function() {
        SPCFit.setPersonalInformation(info);
    }
```

#### getPersonalInformation

Devuelve un `JSONObject` (`info`) con la información del usuario.

```js
    getPersonalInformation: function() {
        SPCFit.getPersonalInformation(function(info) {
            ...
        });
    }
```

#### setTargetSteps

Recive un `int` (`targetSteps`) y cambia el objetivo del dispositivo.

```js
    setTargetSteps: function() {
        SPCFit.setTargetSteps(targetSteps);
    }
```

#### getTargetSteps

Devuelve un `int` (`targetSteps`) con el objetivo del dispositivo.

```js
    getTargetSteps: function() {
        SPCFit.getTargetSteps(function(targetSteps) {
            ...
        });
    }
```

#### getCurrentActivityInformation

Devuelve un `JSONObject` (`activity`) con la información de la actividad actual.

```js
    getCurrentActivityInformation: function() {
        SPCFit.getCurrentActivityInformation(function(activity) {
            ...
        });
    }
```

#### getTotalActivityData

Recibe un 'int' (`day`) y devuelve un `JSONObject` (`activity`) con la información total de ese dia.

La variable day corresponde al indice de dias con información (exceptuando el 0 que siempre es el dia actual) que tenga el dispositivo ej.

El dispositivo tiene información de hoy y antes de ayer pero no de ayer, por lo tanto el indice 0 corresponde a hoy y el indice 1 a antes de ayer

```js
    getTotalActivityData: function() {
        SPCFit.getTotalActivityData(day, function(activity) {
            ...
        });
    }
```

#### getDetailActivityData

Recibe un 'int' (`day`) y devuelve un `JSONObject` (`activity`) con la información detallada de ese dia.

```js
    getDetailActivityData: function() {
        SPCFit.getDetailActivityData(day, function(activity) {
            ...
        });
    }
```

#### startRealTimeMeterMode

Recibe un 'String' (`'app.realtimeResponse'`) que sera el nombre de la funcion por la cual respondera a las llamadas del modo Real Time e inicia El estado Real Time.

```js
    startRealTimeMeterMode: function() {
        SPCFit.startRealTimeMeterMode('app.realtimeResponse');
    }
```


#### realtimeResponse

Esta función no tiene por que llamarse así, deve corresponder con el parametro enviado en la funcion startRealTimeMeterMode.

Si la pulsera esta en estado Real Time, cada vez que se mueva la pulsera la informacion actualizada de la actividad se recibira en esta función. 

Devuelve un `JSONObject` (`activity`) con la información de la actividad.

```js
    realtimeResponse: function(activity) {
        $('#realtimeResponse').val(JSON.stringify(activity));
    }
```

#### stopRealTimeMeterMode

Para el estado Real Time.

```js
    stopRealTimeMeterMode: function() {
        SPCFit.stopRealTimeMeterMode();
    }
```

