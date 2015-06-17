# ActivityTracker

Clase que encapsula el protocolo de comunicación.

## Funcionamiento

La conexión con el dispositivo se realiza de manera asíncrona utilizando Bluetooth.

Para ejecutar cualquier comando es necesario que este servicio esté vinculado a la aplicación, la comunicación funciona mediante notificaciones por lo tanto, las actividades o fragmentos deveran recojer las notificaciones correspondientes.

##Métodos publicos

```java
public void findDevices()
public void stopFindingDevices()
public boolean connectToDeviceGatt(String address)
public void disconnectFromDeviceGatt()
public void setTime(Calendar calendar)
public void getTime()
public void setPersonalInformation(byte male, byte age, byte height, byte weight,byte stride)
public void getPersonalInformation()
public void getTotalActivityData(byte day)
public void getDetailActivityData(byte day)
public void deleteActivityData(byte day)
public void startRealTimeMeterMode()
public void stopRealTimeMeterMode()
public void getCurrentActivityInformation()
public void queryDataStorage()
public void setTargetSteps(int steps)
public void getTargetSteps()
public void getActivityGoalAchievedRate(byte day)
```

La mayoría de comandos devuelven una sola notificación. Algunos comandos devuelven varias respuestas como:

- ` public void getTotalActivityData(byte day)`
- ` public void getDetailActivityData(byte day)`

Cada una de las notificaciones contiene diferente cantidad de información, dependiendo del tipo de comando que se haya ejecutado, por lo tanto la actividad o el fragmento que capture esa notificación deverá gestionar la información que contiene.

Podemos hacerlo por ejemplo así:

`ActivityTracker`

Parsear la respuesta del dispositivo y enviarla.

```java
public static final String BLE_GET_TARGET_STEPS_RESPONSE = "BLE_GET_TARGET_STEPS_RESPONSE";

private void getTargetStepsResponse(byte[] bytes)
{
    int steps = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);

    Intent intent = new Intent(BLE_GET_TARGET_STEPS_RESPONSE);
    intent.putExtra("steps", steps);
    sendBroadcast(intent);
}
```

`Activity` or `Fragment`

Configurar un recibidor y registrar con un filtro de notificaciones.

```java
@Override
public void onResume() {
    super.onResume();
    getActivity().registerReceiver(receiver, receiverIntentFilter());
}

@Override
public void onPause() {
    super.onPause();
    getActivity().unregisterReceiver(receiver);
}

private static IntentFilter receiverIntentFilter() {
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ActivityTracker.BLE_GET_TARGET_STEPS_RESPONSE);
    return intentFilter;
}

private final BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (ActivityTracker.BLE_GET_TARGET_STEPS_RESPONSE.equals(action)) {
            Bundle bundle = intent.getExtras();
            getGoalTV.setText(Integer.toString(bundle.getInt("steps")));
        }
    }
};
```

