# ActivityTrackerManager

Servicio que permite:

- Encontrar dispositivos Bluetooth cercanos
- Acceder al listado de dispositivos encontrados
- Conectar con un dispositivo
- Acceder al dispositivo activo

## Notificaciones

El servicio envía dos notificaciones:

- `ActivityTrackerManagerStateUpdatedNotification`: Se ha actualizado el estado de Bluetooth (encendido, apagado...)
- `ActivityTrackerManagerFoundNotification`: Se ha encontrado un dispositivo
- `ActivityTrackerManagerConnectedNotification`: Se ha conectado con un dispositivo
- `ActivityTrackerManagerDisconnectedNotification`: Se ha conectado con un dispositivo

## Utilización en un proyecto

Incluir el framework de Apple `CoreBluetooth`.

## Utilización en un ViewController

Incluir la cabecera `ActivityTrackerManager.h`:

```objc
#import "ActivityTrackerManager.h"
```

Utilizar una `@property` para guardar la referencia:

```objc
@property (strong, nonatomic) ActivityTrackerManager *activityTrackerManager;
```

`ActivityTrackerManager` es un *Singleton*. En `viewDidLoad` obtener la referencia a la instancia compartida mediante el método de clase `sharedInstance`.

```objc
- (void)viewDidLoad {
    [super viewDidLoad];

    self.activityTrackerManager = [ActivityTrackerManager sharedInstance];
}
```

## Encontrar dispositivos

La clase `ActivityTrackerManager` busca automáticamente dispositivos cuando el Bluetooth y la aplicación están activos y almacena el listado.

También está disponible la función:

`- (void)findPeripherals:(NSTimeInterval)timeInterval;`

## Acceder al listado de dispositivos

Acceso ordenado:

`- (CBPeripheral *)peripheralAtIndex:(NSUInteger)index;`

## Conectar un dispositivo

`- (void)connectTo:(CBPeripheral *)peripheral;`

## Dispositivo activo

La clase `ActivityTrackerManager` guarda una referencia al último dispositivo al que hayamos conectado, por conveniencia:

`@property (strong, nonatomic) CBPeripheral *activePeripheral;`
