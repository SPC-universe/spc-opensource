# ActivityTracker

Clase que encapsula el protocolo de comunicación y utiliza delegación para notificaciones asíncronas.

## Funcionamiento

La conexión con el dispositivo se realiza de manera asíncrona utilizando Bluetooth.

Para ejecutar cualquier comando primero es necesario instanciar la clase `ActivityTracker` con un `CBPeripheral` al que se haya conectado anteriormente utilizando el método de `ActivityTrackerManager` `- (void)connectTo:(CBPeripheral *)peripheral;` y asignarle un delegado obligatoriamente que recibirá todas las respuestas asíncronas.

Podemos hacerlo por ejemplo así:

```objc
self.activityTrackerManager = [ActivityTrackerManager sharedInstance];
self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activePeripheral delegate:self];
```

A partir de ahí podemos llamar a una de las funciones disponibles, que retornan inmediatamente.

Y cuando el dispositivo ejecute el comando devuelva una o varias respuestas será invocado en el delegado el método response correspondiente siguiendo el protocolo `ActivityTrackerDelegate`. Por ejemplo al comando `- (void)getTime;` le seguirá en el futuro la respuesta `- (void)activityTrackerGetTimeResponse:(NSDate *)date;` con el parámetro date con la fecha actual del dispositivo.

La mayoría de comandos devuelven una sola respuesta. Algunos comandos devuelven varias respuestas como:

- `- (void)getTotalActivityData:(Byte)day;` 2 respuestas (revisar protocolo)
- `- (void)getDetailActivityData:(Byte)day;` hasta 96 respuestas (con información almacenada cada 15 minutos)

## Métodos

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

## Protocolo ActivityTrackerDelegate

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
