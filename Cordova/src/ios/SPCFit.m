#import "SPCFit.h"
#import "ActivityTrackerManager.h"

#define YMDHMS NSCalendarUnitYear|NSCalendarUnitMonth|NSCalendarUnitDay|NSCalendarUnitHour|NSCalendarUnitMinute|NSCalendarUnitSecond

@interface SPCFit() <ActivityTrackerDelegate>

@property (strong, nonatomic) NSDateFormatter *gmt;
@property (strong, nonatomic) NSDateFormatter *gmtYmd;
@property (strong, nonatomic) NSDateFormatter *gmtHS;
@property (strong, nonatomic) NSTimeZone *gmtTz;
@property (strong, nonatomic) NSCalendar *gmtCalendar;
@property (strong, nonatomic) NSDateFormatter *Ymdhms;

@property (strong, nonatomic) ActivityTrackerManager *activityTrackerManager;
@property (strong, nonatomic) ActivityTracker *activityTracker;
@property (strong, nonatomic) NSMutableArray *totalActivityData;
@property (strong, nonatomic) NSMutableArray *detailedActivityData;
@property (atomic) int day;

@property (strong, nonatomic) NSMutableDictionary *callbackTable;

@end

@implementation SPCFit

- (void)pluginInitialize
{
    _gmtTz = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
    _gmtCalendar = [NSCalendar currentCalendar];
    [_gmtCalendar setTimeZone:_gmtTz];
 
    _Ymdhms = [[NSDateFormatter alloc] init];
    _Ymdhms.dateFormat = @"yyyy-MM-dd HH:mm:ss";

    _gmt = [[NSDateFormatter alloc] init];
    _gmt.dateFormat = @"yyyy-MM-dd HH:mm:ss";
    [_gmt setTimeZone:_gmtTz];
    
    _gmtYmd = [[NSDateFormatter alloc] init];
    _gmtYmd.dateFormat = @"yyyy-MM-dd";
    [_gmtYmd setTimeZone:_gmtTz];
    
    _gmtHS = [[NSDateFormatter alloc] init];
    _gmtHS.dateFormat = @"HH:mm";
    [_gmtHS setTimeZone:_gmtTz];

    _callbackTable = [[NSMutableDictionary alloc] init];
    _totalActivityData = [[NSMutableArray alloc] initWithCapacity:30];
    _detailedActivityData = [[NSMutableArray alloc] initWithCapacity:30];
    for (int i = 0; i <= 29; ++i) {
        _totalActivityData[i] = [[NSMutableDictionary alloc] init];
        _detailedActivityData[i] = [[NSMutableDictionary alloc] init];
        _detailedActivityData[i][@"responseCount"] = @0;
        _detailedActivityData[i][@"activityDetail"] = [[NSMutableArray alloc] init];
        _detailedActivityData[i][@"sleepQualityDetail"] = [[NSMutableArray alloc] init];
    }

    [self setupNotifications];
    
    _activityTrackerManager = [ActivityTrackerManager sharedInstance];
    [_activityTrackerManager findPeripherals:5.0];
}

#pragma mark Notifications

- (void)dealloc
{
    [self removeNotifications];
}

- (void)setupNotifications {
    NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
    
    [center addObserver:self
               selector:@selector(activityTrackerManagerStateUpdatedNotification:)
                   name:@"ActivityTrackerManagerStateUpdatedNotification"
                 object:nil];
    [center addObserver:self
               selector:@selector(activityTrackerTimeoutNotification:)
                   name:@"ActivityTrackerTimeoutNotification"
                 object:nil];
    [center addObserver:self
               selector:@selector(activityTrackerConnectedNotification:)
                   name:@"ActivityTrackerConnectedNotification"
                 object:nil];
    [center addObserver:self
               selector:@selector(activityTrackerDisconnectedNotification:)
                   name:@"ActivityTrackerDisconnectedNotification"
                 object:nil];
}

- (void)removeNotifications {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)activityTrackerManagerStateUpdatedNotification:(NSNotification *)notification {
    NSLog(@"activityTrackerManagerStateUpdatedNotification: %@", notification);
    
    NSString *state = notification.userInfo[@"state"];
    if ([state isEqualToString:@"CBCentralManagerStatePoweredOn"]) {
        //[self.activityTrackerManager findPeripherals:10.0];
    } else {
        /*UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Aviso"
         message:@"Se requiere Bluetooth activado"
         delegate:self
         cancelButtonTitle:@"Ok"
         otherButtonTitles:nil];
         [alert show];*/
    }
}

- (void)activityTrackerTimeoutNotification:(NSNotification *)notification {
    NSLog(@"activityTrackerTimeoutNotification: %@", notification);

    NSString *callbackId = self.callbackTable[@"findDevices"];
    if (callbackId) {
        NSMutableArray *deviceList = [[NSMutableArray alloc] init];
        for (NSString *deviceId in self.activityTrackerManager.peripherals) {
            [deviceList addObject:deviceId];
        }

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:deviceList];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
}

- (void)activityTrackerConnectedNotification:(NSNotification *)notification {
    NSLog(@"activityTrackerConnectedNotification: %@", notification);
    
    NSString *deviceId = notification.userInfo[@"deviceId"];
    if (deviceId) {
        self.activityTracker = self.activityTrackerManager.activityTrackers[deviceId];
        if (self.activityTracker) {
            [self.activityTracker discoverServices];
        }
    }
}

- (void)activityTrackerDisconnectedNotification:(NSNotification *)notification {
    NSLog(@"activityTrackerDisconnectedNotification: %@", notification);
    
}

#pragma mark Plugin interface

- (void)findDevices:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    NSLog(@"findDevices: %@", callbackId);

    [self.activityTrackerManager findPeripherals:5.0];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"findDevices"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)connect:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    NSString *deviceId = [[command arguments] objectAtIndex:0];
    NSLog(@"connect: %@ %@", callbackId, deviceId);

    [self.activityTrackerManager connectTo:deviceId delegate:self];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"connect"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)setTime:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    NSString *timeString = [[command arguments] objectAtIndex:0];
    NSLog(@"setTime: %@ %@", callbackId, timeString);

    NSDate *time = [self.Ymdhms dateFromString:timeString];
    if (time) {
        [self.activityTrackerManager.activityTracker setTime:time];
        
        CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"setTime"];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    } else {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Invalid date. Format: yyyy-mm-dd hh:mm:ss"];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
}

- (void)getTime:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    NSLog(@"getTime: %@", callbackId);

    [self.activityTracker getTime];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"getTime"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)setPersonalInformation:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    NSDictionary *info = [[command arguments] objectAtIndex:0];

    NSLog(@"setPersonalInformation: %@ %@", callbackId, info);
    BOOL man = [info[@"man"] boolValue];
    int age = [info[@"age"] intValue];
    int height = [info[@"height"] intValue];
    int weight = [info[@"weight"] intValue];
    int stepLength = [info[@"stepLength"] intValue];

    [self.activityTracker setPersonalInformationMale:man age:age height:height weight:weight stride:stepLength];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"setPersonalInformation"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)getPersonalInformation:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    NSLog(@"getPersonalInformation: %@", callbackId);
    
    [self.activityTracker getPersonalInformation];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"getPersonalInformation"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)setTargetSteps:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    int steps = [[[command arguments] objectAtIndex:0] intValue];
    NSLog(@"setTargetSteps: %@ %i", callbackId, steps);

    [self.activityTracker setTargetSteps:steps];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"setTargetSteps"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)getTargetSteps:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    NSLog(@"getTargetSteps: %@", callbackId);
    
    [self.activityTracker getTargetSteps];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"getTargetSteps"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)getCurrentActivityInformation:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    NSLog(@"getCurrentActivityInformation: %@", callbackId);

    [self.activityTracker getCurrentActivityInformation];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"getCurrentActivityInformation"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)getTotalActivityData:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    int day = [[[command arguments] objectAtIndex:0] intValue];
    NSLog(@"getTotalActivityData: %@ %i", callbackId, day);

    self.totalActivityData[day] = [[NSMutableDictionary alloc] init];
    self.totalActivityData[day][@"responseCount"] = @0;

    [self.activityTracker getTotalActivityData:day];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"getTotalActivityData"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)getDetailActivityData:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    int day = [[[command arguments] objectAtIndex:0] intValue];
    NSLog(@"getDetailActivityData: %@ %i", callbackId, day);

    self.day = day;
    self.detailedActivityData[day] = [[NSMutableDictionary alloc] init];
    self.detailedActivityData[day][@"responseCount"] = @0;
    self.detailedActivityData[day][@"activityDetail"] = [[NSMutableArray alloc] init];
    self.detailedActivityData[day][@"sleepQualityDetail"] = [[NSMutableArray alloc] init];

    [self.activityTracker getDetailActivityData:day];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"getDetailActivityData"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)startRealTimeMeterMode:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    NSString *realtimeCallback = [[command arguments] objectAtIndex:0];
    NSLog(@"startRealTimeMeterMode: %@", callbackId);

    [self.activityTracker startRealTimeMeterMode];
    
    self.callbackTable[@"realtimeCallback"] = realtimeCallback;

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"startRealTimeMeterMode"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)stopRealTimeMeterMode:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = [command callbackId];
    NSLog(@"stopRealTimeMeterMode: %@", callbackId);

    [self.activityTracker stopRealTimeMeterMode];

    CDVPluginResult *result = [self saveCallbackForLater:callbackId key:@"stopRealTimeMeterMode"];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

#pragma mark ActivityTrackerDelegate

- (void)activityTrackerReady:(ActivityTracker *)activityTracker
{
    NSLog(@"activityTrackerReady");
    
    self.activityTracker = activityTracker;

    NSString *callbackId = self.callbackTable[@"connect"];
    if (callbackId) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
}

- (void)activityTrackerGetTimeResponse:(NSDate *)date
{
    NSString *dateResult = [self.Ymdhms stringFromDate:date];
    NSLog(@"getTimeResponse: %@", dateResult);
    
    NSString *callbackId = self.callbackTable[@"getTime"];
    if (callbackId) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:dateResult];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
}

- (void)activityTrackerSetTimeResponse
{
    NSLog(@"setTimeResponse");
    
    NSString *callbackId = self.callbackTable[@"setTime"];
    if (callbackId) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
}

- (void)activityTrackerGetPersonalInformationResponseMan:(BOOL)man
                                                     age:(Byte)age
                                                  height:(Byte)height
                                                  weight:(Byte)weight
                                              stepLength:(Byte)stepLength
                                                deviceId:(NSString *)deviceId
{
    NSLog(@"getPersonalInformationResponse: %d %d %d %d %d %@", man, age, height, weight, stepLength, deviceId);
    
    NSDictionary *info = @{ @"man": @(man),
                            @"age": @(age),
                            @"height": @(height),
                            @"weight": @(weight),
                            @"stepLength": @(stepLength) };

    NSString *callbackId = self.callbackTable[@"getPersonalInformation"];
    if (callbackId) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:info];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
}

- (void)activityTrackerSetPersonalInformationResponse
{
    NSLog(@"setPersonalInformationResponse");
    
    NSString *callbackId = self.callbackTable[@"setPersonalInformation"];
    if (callbackId) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
}

- (void)activityTrackerGetTargetStepsResponse:(int)steps
{
    NSLog(@"getTargetStepsResponse: %i", steps);
    
    NSString *callbackId = self.callbackTable[@"getTargetSteps"];
    if (callbackId) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:steps];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
}

- (void)activityTrackerSetTargetStepsResponse
{
    NSLog(@"setTargetStepsResponse");
    
    NSString *callbackId = self.callbackTable[@"setTargetSteps"];
    if (callbackId) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    }
}

- (void)activityTrackerGetCurrentActivityInformationResponseSteps:(int)steps
                                                     aerobicSteps:(int)aerobicSteps
                                                         calories:(int)calories
                                                         distance:(int)distance
                                                     activityTime:(int)activityTime
{
    NSLog(@"getCurrentActivityInformationResponse: %d %d %d %d %d", steps, aerobicSteps, calories, distance, activityTime);
    
    if (steps || aerobicSteps || calories || distance || activityTime) {
        NSDictionary *activity = @{ @"steps": @(steps),
                                @"aerobicSteps": @(aerobicSteps),
                                @"calories": @(calories),
                                @"distance": @(distance),
                                @"activityTime": @(activityTime) };

        NSString *callbackId = self.callbackTable[@"getCurrentActivityInformation"];
        if (callbackId) {
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:activity];
            [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        }
    }
}

- (void)activityTrackerGetTotalActivityDataResponseDay:(int)day
                                                  date:(NSDate *)date
                                                 steps:(int)steps
                                          aerobicSteps:(int)aerobicSteps
                                              calories:(int)calories
{
    NSLog(@"getTotalActivityDataResponse: %d %@ %d %d %d", day, [self.gmt stringFromDate:date], steps, aerobicSteps, calories);

    int responseCount = [self.totalActivityData[day][@"responseCount"] intValue] + 1;
    self.totalActivityData[day][@"responseCount"] = @(responseCount);

    if (date && (steps || aerobicSteps || calories)) {
        self.totalActivityData[day][@"date"] = [self.gmtYmd stringFromDate:date];
        self.totalActivityData[day][@"steps"] = @(steps);
        self.totalActivityData[day][@"aerobicSteps"] = @(aerobicSteps);
        self.totalActivityData[day][@"calories"] = @(calories);
    }

    if (responseCount == 2) {
        NSString *callbackId = self.callbackTable[@"getTotalActivityData"];
        if (callbackId) {
            [self.totalActivityData[day] removeObjectForKey:@"responseCount"];
            NSDictionary *data = self.totalActivityData[day];
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
            [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        }
    }
}

- (void)activityTrackerGetTotalActivityDataResponseDay:(int)day
                                                  date:(NSDate *)date
                                              distance:(int)distance
                                          activityTime:(int)activityTime
{
    NSLog(@"getTotalActivityDataResponse: %d %@ %d %d", day, [self.gmt stringFromDate:date], distance, activityTime);
    
    int responseCount = [self.totalActivityData[day][@"responseCount"] intValue] + 1;
    self.totalActivityData[day][@"responseCount"] = @(responseCount);

    if (date) {
        self.totalActivityData[day][@"date"] = [self.gmtYmd stringFromDate:date];
        self.totalActivityData[day][@"distance"] = @(distance);
        self.totalActivityData[day][@"activityTime"] = @(activityTime);
    }
    
    if (responseCount == 2) {
        NSString *callbackId = self.callbackTable[@"getTotalActivityData"];
        if (callbackId) {
            [self.totalActivityData[day] removeObjectForKey:@"responseCount"];
            NSDictionary *data = self.totalActivityData[day];
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
            [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        }
    }
}

- (void)activityTrackerGetDetailActivityDataResponseIndex:(int)index
                                                     date:(NSDate *)date
                                                    steps:(int)steps
                                             aerobicSteps:(int)aerobicSteps
                                                 calories:(int)calories
                                                 distance:(int)distance
{
    NSLog(@"getDetailActivityDataDayResponse: %d %d %@ %d %d %d %d", self.day, index, [self.gmt stringFromDate:date], steps, aerobicSteps, calories, distance);
    
    int responseCount = [self.detailedActivityData[self.day][@"responseCount"] intValue] + 1;
    self.detailedActivityData[self.day][@"responseCount"] = @(responseCount);
    if (steps || aerobicSteps || calories || distance) {
        NSDictionary *detail = @{ @"date": [self.gmt stringFromDate:date],
                                  @"steps": @(steps),
                                  @"aerobicSteps": @(aerobicSteps),
                                  @"calories": @(calories),
                                  @"distance": @(distance) };
        
        [self.detailedActivityData[self.day][@"activityDetail"] addObject:detail];
    }
    
    if (responseCount == 96) {
        NSString *callbackId = self.callbackTable[@"getDetailActivityData"];
        if (callbackId) {
            self.detailedActivityData[self.day][@"date"] = [self.gmtYmd stringFromDate:date];
            [self.detailedActivityData[self.day] removeObjectForKey:@"responseCount"];
            NSDictionary *data = self.detailedActivityData[self.day];
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
            [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        }
    }
}

- (void)activityTrackerGetDetailActivityDataSleepResponseIndex:(int)index
                                                sleepQualities:(NSArray *)sleepQualities
{
    int responseCount = [self.detailedActivityData[self.day][@"responseCount"] intValue] + 1;
    self.detailedActivityData[self.day][@"responseCount"] = @(responseCount);
    NSDate *date;
    for (NSDictionary *sleepQualityDetailData in sleepQualities) {
        date = sleepQualityDetailData[@"date"];
        int sleepQuality = [sleepQualityDetailData[@"quality"] intValue];

        NSLog(@"getDetailActivityDataSleepResponse: %d %d %@ %d", self.day, index, [self.gmt stringFromDate:date], sleepQuality);
        
        if (date) {
            NSDictionary *detail = @{ @"date": [self.gmt stringFromDate:date],
                                      @"quality": @(sleepQuality) };

            [self.totalActivityData[self.day][@"sleepQualityDetail"] addObject:detail];
        }
    }

    if (date && responseCount == 96) {
        NSString *callbackId = self.callbackTable[@"getDetailActivityData"];
        if (callbackId) {
            self.detailedActivityData[self.day][@"date"] = [self.gmtYmd stringFromDate:date];
            [self.detailedActivityData[self.day] removeObjectForKey:@"responseCount"];
            NSDictionary *data = self.detailedActivityData[self.day];
            CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
            [self.commandDelegate sendPluginResult:result callbackId:callbackId];
        }
    }
}

- (void)activityTrackerGetDetailActivityDataResponseWithoutData
{
    NSLog(@"getDetailActivityDataResponseWithoutData");
}

- (void)activityTrackerRealTimeModeResponseSteps:(int)steps
                                    aerobicSteps:(int)aerobicSteps
                                        calories:(int)calories
                                        distance:(int)distance
                                    activityTime:(int)activityTime
{
    NSLog(@"activityTrackerRealTimeModeResponseSteps: %d %d %d %d %d", steps, aerobicSteps, calories, distance, activityTime);
    
    if (steps || aerobicSteps || calories || distance || activityTime) {
        NSString *realtimeCallback = self.callbackTable[@"realtimeCallback"];
        if (realtimeCallback) {
            NSString *params = [NSString stringWithFormat:@"( { steps: %i, aerobicSteps: %i, calories: %i, distance: %i, activityTime: %i } )",
                                steps, aerobicSteps, calories, distance, activityTime];
            NSString *realtimeCallbackWithParams = [realtimeCallback stringByAppendingString:params];
            [self.commandDelegate evalJs:realtimeCallbackWithParams];
        }
    }
}

#pragma mark Callback Stuff

- (CDVPluginResult *)saveCallbackForLater:(NSString *)callbackId key:(NSString *)key
{
    self.callbackTable[key] = callbackId;
    
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [result setKeepCallbackAsBool:YES];
    
    return result;
}

@end
