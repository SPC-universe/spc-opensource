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

### `FechaViewController.h`

```objc
#import <UIKit/UIKit.h>

@interface FechaViewController : UIViewController

@end
```

### `FechaViewController.m`

```objc
#import "FechaViewController.h"
#import "ActivityTrackerManager.h"

@interface FechaViewController () <ActivityTrackerDelegate>

@property (strong, nonatomic) ActivityTrackerManager *activityTrackerManager;
@property (strong, nonatomic) ActivityTracker *activityTracker;

@property (weak, nonatomic) IBOutlet UITextField *timeField;

@end

@implementation FechaViewController

#pragma mark Init

- (void)viewDidLoad {
    [super viewDidLoad];

    self.activityTrackerManager = [ActivityTrackerManager sharedInstance];
    self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activityTracker.peripheral delegate:self];
    [self.activityTracker discoverServices];
}

#pragma mark Actions

- (IBAction)getTime:(UIButton *)sender {
    [self.activityTracker getTime];
}

- (IBAction)setTime:(UIButton *)sender {
    [self.activityTracker setTime:[NSDate dateWithTimeIntervalSinceNow:0.0]];
}

#pragma mark ActivityTrackerDelegate

- (void)activityTrackerReady:(ActivityTracker *)activityTracker
{
    NSLog(@"activityTrackerReady: %@", activityTracker);

    [self.activityTracker getTime];
}

- (void)activityTrackerGetTimeResponse:(NSDate *)date
{
    NSLog(@"getTimeResponse: %@", date);

    self.timeField.text = [NSString stringWithFormat:@"%@", date];
}

- (void)activityTrackerSetTimeResponse:(BOOL)error
{
    NSLog(@"setTimeResponse: %@", error ? @"YES" : @"NO");
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

- `ActivityTrackerManagerStateUpdatedNotification`: Se ha actualizado el estado de Bluetooth (encendido, apagado...)
- `ActivityTrackerManagerFoundNotification`: Se ha encontrado un dispositivo
- `ActivityTrackerManagerConnectedNotification`: Se ha conectado con un dispositivo
- `ActivityTrackerManagerDisconnectedNotification`: Se ha conectado con un dispositivo

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
- (void)getCurrentActivityInformation;
- (void)getTotalActivityData:(Byte)day;
- (void)getDetailActivityData:(Byte)day;
- (void)deleteActivityData:(Byte)day;
- (void)startRealTimeMeterMode;
- (void)stopRealTimeMeterMode;
- (void)queryDataStorage;
- (void)setTargetSteps:(int)steps;
- (void)getTargetSteps;
- (void)getActivityGoalAchievedRate:(Byte)day;

// Comandos para nuevas pulseras
- (void)safeBondingSavePassword:(NSString *)password;
- (void)safeBondingSendPassword:(NSString *)password;
- (void)safeBondingStatus;

- (void)switchSleepMonitorMode;

- (void)startECGMode;
- (void)stopECGMode;
- (void)deleteECGData;
- (void)getECGData:(Byte)index;
```

### Protocolo `ActivityTrackerDelegate.h`

Protocolo con las posibles respuestas al delegado asignado durante la creación de un ActivityTracker:

```objc
self.activityTrackerManager = [ActivityTrackerManager sharedInstance];
self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activityTracker.peripheral delegate:self];
[self.activityTracker discoverServices];
```

```objc
@class ActivityTracker;

@protocol ActivityTrackerDelegate <NSObject>

@optional
- (void)activityTrackerReady:(ActivityTracker *)activityTracker;

- (void)activityTrackerSafeBondingSavePasswordResponse;
- (void)activityTrackerSafeBondingSendPasswordResponse:(BOOL)error;
- (void)activityTrackerSafeBondingStatusResponse:(BOOL)error;

- (void)activityTrackerSetTimeResponse;
- (void)activityTrackerGetTimeResponse:(NSDate *)date;

- (void)activityTrackerSetPersonalInformationResponse;
- (void)activityTrackerGetPersonalInformationResponseMan:(BOOL)man
                                                     age:(Byte)age
                                                  height:(Byte)height
                                                  weight:(Byte)weight
                                              stepLength:(Byte)stepLength
                                                deviceId:(NSString *)deviceId;

- (void)activityTrackerGetTotalActivityDataResponseDay:(int)day
                                                  date:(NSDate *)date
                                                 steps:(int)steps
                                          aerobicSteps:(int)aerobicSteps
                                              calories:(int)calories;
- (void)activityTrackerGetTotalActivityDataResponseDay:(int)day
                                                  date:(NSDate *)date
                                              distance:(int)distance
                                          activityTime:(int)activityTime;

- (void)activityTrackerGetDetailActivityDataDayResponseIndex:(int)index
                                                        date:(NSDate *)date
                                                       steps:(int)steps
                                                aerobicSteps:(int)aerobicSteps
                                                    calories:(int)calories
                                                    distance:(int)distance;

- (void)activityTrackerGetDetailActivityDataSleepResponseIndex:(int)index
                                                  sleepQualities:(NSArray *)sleepQualities;

- (void)activityTrackerGetDetailActivityDataResponseWithoutData;

- (void)activityTrackerDeleteActivityDataResponse;

- (void)activityTrackerRealTimeModeResponseSteps:(int)steps
                                    aerobicSteps:(int)aerobicSteps
                                        calories:(int)calories
                                        distance:(int)distance
                                    activityTime:(int)activityTime;
- (void)activityTrackerStopRealTimeMeterModeResponse;
- (void)activityTrackerSwitchSleepMonitorModeResponse;

- (void)activityTrackerStartECGModeResponse;
- (void)activityTrackerECGModeResponseDate:(NSDate *)date
                                      data:(NSArray *)data;
- (void)activityTrackerStopECGModeResponse;
- (void)activityTrackerDeleteECGDataResponse;
- (void)activityTrackerGetECGDataResponseDate:(NSDate *)date
                                    heartRate:(int)heartRate;

- (void)activityTrackerGetCurrentActivityInformationResponseSteps:(int)steps
                                                     aerobicSteps:(int)aerobicSteps
                                                         calories:(int)calories
                                                         distance:(int)distance
                                                     activityTime:(int)activityTime;

- (void)activityTrackerQueryDataStorageResponse:(NSArray *)dataStorage;

- (void)activityTrackerSetTargetStepsResponse;
- (void)activityTrackerGetTargetStepsResponse:(int)steps;
- (void)activityTrackerGetActivityGoalAchievedRateResponseDay:(Byte)dayIndex
                                                         date:(NSDate *)date
                                             goalAchievedRate:(int)goalAchievedRate
                                                activitySpeed:(int)activitySpeed
                                                           ex:(int)ex
                                          goalFinishedPercent:(int)goalFinishedPercent;

- (void)activityTrackerResetToFactorySettingsResponse;
- (void)activityTrackerResetMCUResponse;
- (void)activityTrackerFirmwareUpdateResponse;

@end
```
