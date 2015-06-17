# ActivityTrackerManager

Clase que permite la comunicación entre las actividades o fragmentos y el servicio encargado de gestionar la comunicación con el periférico `ActivityTracker`.

`ActivityTrackerManager` es un *Singleton* que posee una instancia al servicio `ActivityTracker` y se comunica con el.

##Como vincular el servicio

Para poder vincular este servicio hace falta el contexto de la aplicación, que se le pasa como parametro al instanciar la clase y un objeto de tipo `ServiceConnection` con los metodos `onServiceConnected` y `onServiceDisconnected`

```java
public static ActivityTrackerManager getInstance(Context context){
    if (activityTrackerManager == null)
    {
        activityTrackerManager = new ActivityTrackerManager(context);
    }
    return activityTrackerManager;
}

private final ServiceConnection activityTrackerServiceConnection = new ServiceConnection(){
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service)
    {
        activityTracker = ((ActivityTracker.LocalBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
        activityTracker = null;
    }
};
```

Una vez tengamos estos dos objetos en el constructor se realiza la llamada para vincular el servicio.

```java
private ActivityTrackerManager(Context context){
    this.context=context;

    activityTrackerManager=new ActivityTrackerManager();

    Intent intent = new Intent(this.context, ActivityTracker.class);
    this.context.bindService(intent, activityTrackerServiceConnection, Context.BIND_AUTO_CREATE);
}
``

##Metodos para comunicarse con el servicio

public void findDevices()
public void stopFindingDevices()
public void connectToDevice(BluetoothDevice device()
public void setTime(Calendar calendar)
public void getTime()
public void startRealTimeMeterMode()
public void getPersonalInformation()
public void setPersonalInformation(byte male, byte age, byte height, byte weight,byte stride)
public void getDetailActivityData(byte day)
public void getTargetSteps()
public void setTargetSteps(int steps)
public void disconnect()
