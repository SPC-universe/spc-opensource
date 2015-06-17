# SPC Fitness SDK

Código para la comunicación con la pulsera SPC Fitness mediante Bluetooth LE.

## Protocolo de comunicación

El protocolo de comunicación está compuesto por las clases:

- ActivityTrackerManager
- ActivityTracker

## Ejemplo de uso

La aplicación de ejemplo permite:

- Encontrar pulseras cercanas
- Conectar con una pulsera
- Establecer y obtener la fecha y hora
- Establecer y obtener la información personal para el cálculo de calorías (altura, peso, objetivo, ...)
- Obtener la actividad actual y en tiempo real (progresiva)
- Obtener la actividad detallada de un día

## Utilización en un ViewController

```objc
#import "ViewController.h"
#import "ActivityTrackerManager.h"

@interface ViewController () <ActivityTrackerDelegate>

@property (strong, nonatomic) ActivityTrackerManager *activityTrackerManager;
@property (strong, nonatomic) ActivityTracker *activityTracker;

@property (weak, nonatomic) IBOutlet UITextField *timeField;

@end

@implementation FechaViewController

#pragma mark Init

- (void)viewDidLoad {
    [super viewDidLoad];

    self.activityTrackerManager = [ActivityTrackerManager sharedInstance];
    self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activePeripheral delegate:self];
}

#pragma mark Actions

- (IBAction)getTime:(UIButton *)sender {
    [self.activityTracker getTime];
}

- (IBAction)setTime:(UIButton *)sender {
    [self.activityTracker setTime:[NSDate dateWithTimeIntervalSinceNow:0.0]];
}

#pragma mark ActivityTrackerDelegate

- (void)activityTrackerReady
{
    NSLog(@"activityTrackerReady");

    [self.activityTracker getTime];
}

- (void)activityTrackerGetTimeResponse:(NSDate *)date
{
    NSLog(@"getTimeResponse: %@", date);

    self.timeField.text = [NSString stringWithFormat:@"%@", date];
}

- (void)activityTrackerSetTimeResponse
{
    NSLog(@"setTimeResponse");
}

@end
```

## ActivityTrackerManager

Servicio que permite:

- Encontrar dispositivos Bluetooth cercanos
- Acceder al listado de dispositivos encontrados
- Conectar con un dispositivo
- Acceder al dispositivo al activo

### Notificaciones

El servicio envía dos notificaciones:

- ActivityTrackerManagerUpdateNotification: Se ha actualizado la lista de dispositivos
- ActivityTrackerManagerConnectedNotification: Se ha conectado con un dispositivo

## ActivityTracker

Clase que encapsula el protocolo de comunicación y utiliza delegación para notificaciones asíncronas.

### Métodos

```objc
- (void)setTime:(NSDate *)date;
- (void)getTime;
- (void)setPersonalInformationMale:(BOOL)male
                               age:(Byte)age
                            height:(Byte)height
                            weight:(Byte)weight
                            stride:(Byte)stride;
- (void)getPersonalInformation;
- (void)getDetailActivityData:(Byte)day;
- (void)getTotalActivityData:(Byte)day;
- (void)deleteActivityData:(Byte)day;
- (void)startRealTimeMeterMode;
- (void)stopRealTimeMeterMode;
- (void)getCurrentActivityInformation;
- (void)queryDataStorage;
- (void)setTargetSteps:(int)steps;
- (void)getTargetSteps;
- (void)getActivityGoalAchievedRateDay:(Byte)day;
```

### Protocolo ActivityTrackerDelegate

```objc
@protocol ActivityTrackerDelegate <NSObject>

@optional
- (void)activityTrackerReady;

- (void)activityTrackerSetTimeResponse;
- (void)activityTrackerGetTimeResponse:(NSDate *)date;

- (void)activityTrackerSetPersonalInformationResponse;
- (void)activityTrackerGetPersonalInformationResponseMale:(BOOL)male
                                                      age:(Byte)age
                                                   height:(Byte)height
                                                   weight:(Byte)weight
                                                   stride:(Byte)stride
                                                 deviceId:(NSString *)deviceId;

- (void)activityTrackerGetTotalActivityDataResponseDay:(Byte)day
                                                  date:(NSDate *)date
                                                 steps:(int)steps
                                          aerobicSteps:(int)aerobicSteps
                                                   cal:(int)cal;
- (void)activityTrackerGetTotalActivityDataResponseDay:(Byte)day
                                                  date:(NSDate *)date
                                                    km:(int)km
                                          activityTime:(int)activityTime;

- (void)activityTrackerGetDetailActivityDataDayResponseIndex:(int)index
                                                        date:(NSDate *)date
                                                       steps:(int)steps
                                                aerobicSteps:(int)aerobicSteps
                                                         cal:(int)cal
                                                          km:(int)km;
- (void)activityTrackerGetDetailActivityDataSleepResponseIndex:(int)index
                                                          date:(NSDate *)date
                                                  sleepQuality:(int)sleepQuality;
- (void)activityTrackerGetDetailActivityDataResponseWithoutData;

- (void)activityTrackerDeleteActivityDataResponse;

- (void)activityTrackerRealTimeModeResponseSteps:(int)steps
                                    aerobicSteps:(int)aerobicSteps
                                             cal:(int)cal
                                              km:(int)km
                                    activityTime:(int)activityTime;
- (void)activityTrackerStopRealTimeMeterModeResponse;

- (void)activityTrackerGetCurrentActivityInformationResponse;

- (void)activityTrackerQueryDataStorageResponse;

- (void)activityTrackerSetTargetStepsResponse;
- (void)activityTrackerGetTargetStepsResponse:(int)steps;
- (void)activityTrackerGetActivityGoalAchievedRateResponse;

@end
```
