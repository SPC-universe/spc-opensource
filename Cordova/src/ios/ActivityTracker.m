#import "ActivityTracker.h"

@interface ActivityTracker () <CBPeripheralDelegate>

@property (strong, nonatomic) CBService *activityTrackerService;
@property (strong, nonatomic) CBCharacteristic *activityTrackerTX;
@property (strong, nonatomic) CBCharacteristic *activityTrackerRX;

@property (strong, nonatomic) NSMutableArray *cmds;
//@property (strong, nonatomic) NSTimer *discoverServicesTimer;
@property (nonatomic) BOOL isReady;

@end

@implementation ActivityTracker

- (instancetype)initWithPeripheral:(CBPeripheral *)peripheral
                          delegate:(id<ActivityTrackerDelegate>)delegate
{
    self = [super init];
    if (self) {
        _isReady = NO;
        _peripheral = peripheral;
        _activityTrackerService = nil;
        _activityTrackerTX = nil;
        _activityTrackerRX = nil;
        _cmds = [[NSMutableArray alloc] init];
        _logs = [[NSMutableArray alloc] init];
        _delegate = delegate;
        
        //[self log:@"ActivityTracker init: %@ %@", peripheral.name, peripheral.identifier.UUIDString];
        
        /*_discoverServicesTimer = [NSTimer scheduledTimerWithTimeInterval:10.0
         target:self
         selector:@selector(discoverServices)
         userInfo:nil
         repeats:YES];*/
    }
    return self;
}

- (void)discoverServices
{
    if (!self.isReady) {
        _peripheral.delegate = self;
        [self.peripheral discoverServices:@[[CBUUID UUIDWithString:@"FFF0"]]];
    }
}

/*- (BOOL)isReady
 {
 return self.peripheral &&
 self.peripheral.state == CBPeripheralStateConnected &&
 self.activityTrackerService &&
 self.activityTrackerTX &&
 self.activityTrackerRX;
 }*/

#pragma mark Notificaciones

- (void)postUpdateNotification
{
    NSDictionary *userInfo = @{ @"activityTracker": self };
    [[NSNotificationCenter defaultCenter] postNotificationName:@"ActivityTrackerUpdateNotification" object:self userInfo:userInfo];
}

#pragma mark Log

- (void)log:(NSString *)title data:(NSString *)format, ...
{
    va_list args;
    va_start(args, format);
    NSString *data = [[NSString alloc] initWithFormat:format arguments:args];
    va_end(args);
    
    //NSLog(@"log: %@", data);
    
    NSDate *date = [NSDate date];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.logs addObject:@{@"date": date,
                               @"title": title,
                               @"data": data }];
        
        [self postUpdateNotification];
    });
}

#pragma mark Commands

#define CMD 0
#define AA 1
#define BB 2
#define CC 3
#define DD 4
#define EE 5
#define FF 6
#define GG 7
#define HH 8
#define II 9
#define JJ 10
#define KK 11
#define LL 12
#define MM 13
#define NN 14
#define CRC 15
#define CMD_ERROR_MASK 0x7F

#define CMD_SET_DEVICE_ID 0x05

- (void)setDeviceId:(NSString *)deviceId
{
    [self log:@"setDeviceId" data:@"%@", deviceId];
    
    //Byte ids[6] = { 0xA1, 0x56, 0x00, 0x37, 0xEA, 0x30 };
    //Byte ids[6] = { 0xA1, 0x56, 0x00, 0x37, 0xE4, 0x4D };
    
    Byte ids[6] = { 0xA0, 0x56, 0x10, 0x00, 0x00, 0x15 };
    
    /*for (int i = 0; i != 6; ++i) {
     ids[i] = [deviceId characterAtIndex:i];
     }*/
    
    Byte bytes[16] = { CMD_SET_DEVICE_ID, ids[0], ids[1], ids[2], ids[3], ids[4], ids[5], 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)setDeviceIdResponse:(NSData *)data
{
    [self log:@"setDeviceIdResponse" data:@"%@", data];
    
    //[self resetMCU];
    //[self resetToFactorySettings];
    
    return YES;
}

#define CMD_SAFE_BONDING_SAVE_PASSWORD 0x20

- (void)safeBondingSavePassword:(NSString *)password
{
    Byte pass[6] = { 0, 0, 0, 0, 0, 0 };
    for (int i = 0; i != 6; ++i) {
        pass[i] = [password characterAtIndex:i];
    }
    
    Byte bytes[16] = { CMD_SAFE_BONDING_SAVE_PASSWORD, pass[0], pass[1], pass[2], pass[3], pass[4], pass[5], 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)safeBondingSavePasswordResponse:(NSData *)data
{
    [self log:@"safeBondingSavePasswordResponse" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerSafeBondingSavePasswordResponse)]) {
            [self.delegate activityTrackerSafeBondingSavePasswordResponse];
        }
    });
    
    return YES;
}

#define CMD_SAFE_BONDING_SEND_PASSWORD 0x6A

- (void)safeBondingSendPassword:(NSString *)password
{
    Byte pass[6] = { 0, 0, 0, 0, 0, 0 };
    for (int i = 0; i != 6; ++i) {
        pass[i] = [password characterAtIndex:i];
    }
    
    Byte bytes[16] = { CMD_SAFE_BONDING_SEND_PASSWORD, pass[0], pass[1], pass[2], pass[3], pass[4], pass[5], 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)safeBondingSendPasswordResponse:(NSData *)data
{
    [self log:@"safeBondingSendPasswordResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    BOOL error = bytes[CMD] & 0x80;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerSafeBondingSendPasswordResponse:)]) {
            [self.delegate activityTrackerSafeBondingSendPasswordResponse:error];
        }
    });
    
    return YES;
}

#define CMD_SAFE_BONDING_STATUS 0x21

- (void)safeBondingStatus
{
    Byte bytes[16] = { CMD_SAFE_BONDING_STATUS, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)safeBondingStatusResponse:(NSData *)data
{
    [self log:@"safeBondingStatusResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    BOOL error = bytes[CMD] & 0x80;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerSafeBondingStatusResponse:)]) {
            [self.delegate activityTrackerSafeBondingStatusResponse:error];
        }
    });
    
    return YES;
}

#define CMD_SET_TIME 0x01

- (void)setTime:(NSDate *)date
{
    [self log:@"setTime" data:@"%@", date];
    
    Byte year, month, day, hour, minute, second;
    
    [self BCDBytesFromDate:date day:&day month:&month year:&year hour:&hour minute:&minute second:&second];
    
    //[self log:@"%02x/%02x/%02x %02x:%02x:%02x", day, month, year, hour, minute, second];
    
    Byte bytes[16] = { CMD_SET_TIME, year, month, day, hour, minute, second, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)setTimeResponse:(NSData *)data
{
    [self log:@"setTimeResponse" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerSetTimeResponse)]) {
            [self.delegate activityTrackerSetTimeResponse];
        }
    });
    
    return YES;
}

#define CMD_GET_TIME 0x41

- (void)getTime
{
    [self log:@"getTime" data:@""];
    Byte bytes[16] = { CMD_GET_TIME, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)getTimeResponse:(NSData *)data
{
    [self log:@"getTimeResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    Byte year = byteFromBCD(bytes[AA]);
    Byte month = byteFromBCD(bytes[BB]);
    Byte day = byteFromBCD(bytes[CC]);
    Byte hour = byteFromBCD(bytes[DD]);
    Byte minute = byteFromBCD(bytes[EE]);
    Byte second = byteFromBCD(bytes[FF]);
    
    NSDate *date = [self dateFromDay:day month:month year:year hour:hour minute:minute second:second];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerGetTimeResponse:)]) {
            [self.delegate activityTrackerGetTimeResponse:date];
        }
    });
    
    return YES;
}

#define CMD_SET_PERSONAL_INFORMATION 0x02

- (void)setPersonalInformationMale:(BOOL)male
                               age:(Byte)age
                            height:(Byte)height
                            weight:(Byte)weight
                            stride:(Byte)stride
{
    [self log:@"setPersonalInformation" data:@"%d %d %d %d %d", male, age, height, weight, stride];
    
    Byte bytes[16] = { CMD_SET_PERSONAL_INFORMATION, male, age, height, weight, stride, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)setPersonalInformationResponse:(NSData *)data
{
    [self log:@"setPersonalInformation" data:@""];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerSetPersonalInformationResponse)]) {
            [self.delegate activityTrackerSetPersonalInformationResponse];
        }
    });
    
    return YES;
}

#define CMD_GET_PERSONAL_INFORMATION 0x42

- (void)getPersonalInformation
{
    [self log:@"getPersonalInformation" data:@""];
    
    Byte bytes[16] = { CMD_GET_PERSONAL_INFORMATION, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)getPersonalInformationResponse:(NSData *)data
{
    [self log:@"getPersonalInformationResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    BOOL man = bytes[AA];
    int age = bytes[BB];
    int height = bytes[CC];
    int weight = bytes[DD];
    int stepLength = bytes[EE];
    NSString *deviceId = [NSString stringWithFormat:@"%02X%02X%02X%02X%02X%02X8380671",
                          bytes[FF],
                          bytes[GG],
                          bytes[HH],
                          bytes[II],
                          bytes[JJ],
                          bytes[KK]];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerGetPersonalInformationResponseMan:age:height:weight:stepLength:deviceId:)]) {
            [self.delegate activityTrackerGetPersonalInformationResponseMan:man
                                                                        age:age
                                                                     height:height
                                                                     weight:weight
                                                                 stepLength:stepLength
                                                                   deviceId:deviceId];
        }
    });
    
    return YES;
}

#define CMD_GET_TOTAL_ACTIVITY_DATA 0x07

- (void)getTotalActivityData:(Byte)day
{
    [self log:@"getTotalActivityData" data:@"%d", day];
    Byte bytes[16] = { CMD_GET_TOTAL_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)getTotalActivityDataResponse:(NSData *)data
{
    [self log:@"getTotalActivityDataResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    int dayIndex = bytes[BB];
    
    int year = byteFromBCD(bytes[CC]);
    int month = byteFromBCD(bytes[DD]);
    int day = byteFromBCD(bytes[EE]);
    
    //if (year || month || day) {
    NSDate *date = [self gmtDateFromDay:day month:month year:year hour:0 minute:0 second:0];
    
    if (bytes[AA] == 0) {
        int steps = intFrom3Bytes(bytes[FF], bytes[GG], bytes[HH]);
        int aerobicSteps = intFrom3Bytes(bytes[II], bytes[JJ], bytes[KK]);
        int calories = intFrom3Bytes(bytes[LL], bytes[MM], bytes[NN]);
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if ([self.delegate respondsToSelector:@selector(activityTrackerGetTotalActivityDataResponseDay:date:steps:aerobicSteps:calories:)]) {
                [self.delegate activityTrackerGetTotalActivityDataResponseDay:dayIndex
                                                                         date:date
                                                                        steps:steps
                                                                 aerobicSteps:aerobicSteps
                                                                     calories:calories];
            }
        });
        
        return NO;
    }
    if (bytes[AA] == 1) {
        int distance = intFrom3Bytes(bytes[FF], bytes[GG], bytes[HH]);
        int activityTime = intFrom2Bytes(bytes[II], bytes[JJ]);
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if ([self.delegate respondsToSelector:@selector(activityTrackerGetTotalActivityDataResponseDay:date:distance:activityTime:)]) {
                [self.delegate activityTrackerGetTotalActivityDataResponseDay:dayIndex
                                                                         date:date
                                                                     distance:distance
                                                                 activityTime:activityTime];
            }
        });
        
        return YES;
    }
    //}
    
    return NO;
}

#define CMD_GET_DETAIL_ACTIVITY_DATA 0x43

#define D_ACTIVITY_DATA 0x00
#define D_SLEEP_QUALITY_DATA 0xff
#define D_TYPE_INDEX 1
#define D_WITH_DATA 0xf0
#define D_WITHOUT_DATA 0xff
#define D_AA 2
#define D_BB 3
#define D_CC 4
#define D_DD 5
#define D_EE 6
#define D_FF 7
#define D_GG 8
#define D_HH 9
#define D_II 10
#define D_JJ 11
#define D_KK 12
#define D_LL 13
#define D_MM 14

- (void)getDetailActivityData:(Byte)day
{
    [self log:@"getDetailActivityData" data:@"%d", day];
    
    Byte bytes[16] = { CMD_GET_DETAIL_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)getDetailActivityDataResponse:(NSData *)data
{
    [self log:@"getDetailActivityDataResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    if (bytes[D_TYPE_INDEX] == D_WITHOUT_DATA) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if ([self.delegate respondsToSelector:@selector(activityTrackerGetDetailActivityDataResponseWithoutData)]) {
                [self.delegate activityTrackerGetDetailActivityDataResponseWithoutData];
            }
        });
        
        return YES;
    }
    if (bytes[D_TYPE_INDEX] == D_WITH_DATA) {
        int year = byteFromBCD(bytes[D_AA]);
        int month = byteFromBCD(bytes[D_BB]);
        int day = byteFromBCD(bytes[D_CC]);
        
        int index = bytes[D_DD];
        int hour = index / 4;
        int minute = 15 * (index % 4);
        
        //[self log:@"%d => %02d:%02d", index, hour, minute];
        
        if (bytes[D_EE] == D_ACTIVITY_DATA) {
            NSDate *date = [self gmtDateFromDay:day month:month year:year hour:hour minute:minute second:0];
            
            int calories = intFrom2Bytes(bytes[D_GG], bytes[D_FF]);
            int steps = intFrom2Bytes(bytes[D_II], bytes[D_HH]);
            int distance = intFrom2Bytes(bytes[D_KK], bytes[D_JJ]);
            int aerobicSteps = intFrom2Bytes(bytes[D_MM], bytes[D_LL]);
            
            dispatch_async(dispatch_get_main_queue(), ^{
                if ([self.delegate respondsToSelector:@selector(activityTrackerGetDetailActivityDataDayResponseIndex:date:steps:aerobicSteps:calories:distance:)]) {
                    [self.delegate activityTrackerGetDetailActivityDataDayResponseIndex:index
                                                                                   date:date
                                                                                  steps:steps
                                                                           aerobicSteps:aerobicSteps
                                                                               calories:calories
                                                                               distance:distance];
                }
            });
        }
        if (bytes[D_EE] == D_SLEEP_QUALITY_DATA) {
            NSMutableArray *sleepQualities = [[NSMutableArray alloc] init];
            for (int i = 0; i != 8; ++i) {
                int sleepQuality = bytes[D_FF + i];
                
                NSDate *date = [self gmtDateFromDay:day month:month year:year hour:hour minute:(minute + 2*i) second:0];
                
                SleepQualityDetailData *sleepQualityDetail = [[SleepQualityDetailData alloc] init];
                sleepQualityDetail.date = date;
                sleepQualityDetail.quality = sleepQuality;
                
                [sleepQualities addObject:sleepQualityDetail];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                if ([self.delegate respondsToSelector:@selector(activityTrackerGetDetailActivityDataSleepResponseIndex:sleepQualities:)]) {
                    [self.delegate activityTrackerGetDetailActivityDataSleepResponseIndex:index
                                                                           sleepQualities:sleepQualities];
                    //date:date
                    //sleepQuality:sleepQuality
                }
            });
        }
        
        if (index == 95) return YES;
    }
    
    return NO;
}

#define CMD_DELETE_ACTIVITY_DATA 0x04

- (void)deleteActivityData:(Byte)day
{
    [self log:@"deleteActivityData" data:@"%d", day];
    Byte bytes[16] = { CMD_DELETE_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)deleteActivityDataResponse:(NSData *)data
{
    [self log:@"deleteActivityDataResponse" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerDeleteActivityDataResponse)]) {
            [self.delegate activityTrackerDeleteActivityDataResponse];
        }
    });
    
    return YES;
}

#define CMD_START_REAL_TIME_METER_MODE_AND_UPDATES 0x09

- (void)startRealTimeMeterMode
{
    [self log:@"startRealTimeMeterMode" data:@""];
    Byte bytes[16] = { CMD_START_REAL_TIME_METER_MODE_AND_UPDATES, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)realTimeMeterModeResponse:(NSData *)data
{
    [self log:@"realTimeMeterModeResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    BOOL bug = bytes[AA] & CMD_ERROR_MASK;
    
    if (!bug) {
        int steps = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);
        int aerobicSteps = intFrom3Bytes(bytes[DD], bytes[EE], bytes[FF]);
        int calories = intFrom3Bytes(bytes[GG], bytes[HH], bytes[II]);
        int distance = intFrom3Bytes(bytes[JJ], bytes[KK], bytes[LL]);
        int activityTime = intFrom2Bytes(bytes[MM], bytes[NN]);
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if ([self.delegate respondsToSelector:@selector(activityTrackerRealTimeModeResponseSteps:aerobicSteps:calories:distance:activityTime:)]) {
                [self.delegate activityTrackerRealTimeModeResponseSteps:steps
                                                           aerobicSteps:aerobicSteps
                                                               calories:calories
                                                               distance:distance
                                                           activityTime:activityTime];
            }
        });
    } else {
        NSLog(@"Bug!");
    }
    
    return YES;
}

#define CMD_STOP_REAL_TIME_METER_MODE 0x0A

- (void)stopRealTimeMeterMode
{
    [self log:@"stopRealTimeMeterMode" data:@""];
    Byte bytes[16] = { CMD_STOP_REAL_TIME_METER_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)stopRealTimeMeterModeResponse:(NSData *)data
{
    [self log:@"stopRealTimeMeterModeResponse" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerStopRealTimeMeterModeResponse)]) {
            [self.delegate activityTrackerStopRealTimeMeterModeResponse];
        }
    });
    
    return YES;
}

#define CMD_GET_SLEEP_MONITOR_MODE 0x6B

- (void)getSleepMonitorMode
{
    [self log:@"getSleepMonitorMode" data:@""];
    Byte bytes[16] = { CMD_GET_SLEEP_MONITOR_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)getSleepMonitorModeResponse:(NSData *)data
{
    [self log:@"getSleepMonitorModeResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    BOOL sleepMode = bytes[AA];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerGetSleepMonitorModeResponse:)]) {
            [self.delegate activityTrackerGetSleepMonitorModeResponse:sleepMode];
        }
    });
    
    return YES;
}

#define CMD_SWITCH_SLEEP_MONITOR_MODE 0x49

- (void)switchSleepMonitorMode
{
    [self log:@"switchSleepMonitorMode" data:@""];
    Byte bytes[16] = { CMD_SWITCH_SLEEP_MONITOR_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)switchSleepMonitorModeResponse:(NSData *)data
{
    [self log:@"switchSleepMonitorModeResponse" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerSwitchSleepMonitorModeResponse)]) {
            [self.delegate activityTrackerSwitchSleepMonitorModeResponse];
        }
    });
    
    return YES;
}

#define CMD_GET_CURRENT_ACTIVITY_INFORMATION 0x48

- (void)getCurrentActivityInformation
{
    [self log:@"getCurrentActivityInformation" data:@""];
    Byte bytes[16] = { CMD_GET_CURRENT_ACTIVITY_INFORMATION, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)getCurrentActivityInformationResponse:(NSData *)data
{
    [self log:@"getCurrentActivityInformationResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    int steps = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);
    int aerobicSteps = intFrom3Bytes(bytes[DD], bytes[EE], bytes[FF]);
    int calories = intFrom3Bytes(bytes[GG], bytes[HH], bytes[II]);
    int distance = intFrom3Bytes(bytes[JJ], bytes[KK], bytes[LL]);
    int activityTime = intFrom2Bytes(bytes[MM], bytes[NN]);
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerGetCurrentActivityInformationResponseSteps:aerobicSteps:calories:distance:activityTime:)]) {
            [self.delegate activityTrackerGetCurrentActivityInformationResponseSteps:steps
                                                                        aerobicSteps:aerobicSteps
                                                                            calories:calories
                                                                            distance:distance
                                                                        activityTime:activityTime];
        }
    });
    
    return YES;
}

#define CMD_QUERY_DATA_STORAGE 0x46

- (void)queryDataStorage
{
    [self log:@"queryDataStorage" data:@""];
    Byte bytes[16] = { CMD_QUERY_DATA_STORAGE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)queryDataStorageResponse:(NSData *)data
{
    [self log:@"queryDataStorageResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    int queryDataStorage = intFrom4Bytes(bytes[AA], bytes[BB], bytes[CC], bytes[DD]);
    
    NSMutableArray *dataStorage = [[NSMutableArray alloc] init];
    for (int i = 0; i != 32; ++i) {
        BOOL hasData = queryDataStorage & 0x00000001;
        [dataStorage addObject:@(hasData)];
        queryDataStorage >>= 1;
    }
    
    [self log:@"dataStorage" data:@"%@", dataStorage];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerQueryDataStorageResponse:)]) {
            [self.delegate activityTrackerQueryDataStorageResponse:dataStorage];
        }
    });
    
    return YES;
}

#define CMD_SET_TARGET_STEPS 0x0B

- (void)setTargetSteps:(int)steps
{
    [self log:@"setTargetSteps" data:@"%d", steps];
    
    Byte steps1 = 0xff & (steps >> 16);
    Byte steps2 = 0xff & (steps >> 8);
    Byte steps3 = 0xff & steps;
    
    //[self log:@"%x %x %x %x", steps, steps1, steps2, steps3];
    
    Byte bytes[16] = { CMD_SET_TARGET_STEPS, steps1, steps2, steps3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)setTargetStepsResponse:(NSData *)data
{
    [self log:@"setTargetStepsResponse" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerSetTargetStepsResponse)]) {
            [self.delegate activityTrackerSetTargetStepsResponse];
        }
    });
    
    return YES;
}

#define CMD_GET_TARGET_STEPS 0x4B

- (void)getTargetSteps
{
    [self log:@"getTargetSteps" data:@""];
    Byte bytes[16] = { CMD_GET_TARGET_STEPS, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)getTargetStepsResponse:(NSData *)data
{
    [self log:@"getTargetStepsResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    int steps = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerGetTargetStepsResponse:)]) {
            [self.delegate activityTrackerGetTargetStepsResponse:steps];
        }
    });
    
    return YES;
}

#define CMD_GET_ACTIVITY_GOAL_ACHIEVED_RATE 0x08

- (void)getActivityGoalAchievedRate:(Byte)day
{
    [self log:@"getActivityGoalAchievedRateDay" data:@"%d", day];
    Byte bytes[16] = { CMD_GET_ACTIVITY_GOAL_ACHIEVED_RATE, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)getActivityGoalAchievedRateResponse:(NSData *)data
{
    [self log:@"getActivityGoalAchievedRateDayResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    Byte dayIndex = bytes[AA];
    
    Byte year = byteFromBCD(bytes[BB]);
    Byte month = byteFromBCD(bytes[CC]);
    Byte day = byteFromBCD(bytes[DD]);
    
    if (year && month && day) {
        NSDate *date = [self gmtDateFromDay:day month:month year:year hour:0 minute:0 second:0];
        
        Byte goalAchievedRate = bytes[EE];
        
        int activitySpeed = intFrom2Bytes(bytes[FF], bytes[GG]);
        int ex = intFrom3Bytes(bytes[HH], bytes[II], bytes[JJ]);
        int goalFinishedPercent = intFrom2Bytes(bytes[KK], bytes[LL]);
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if ([self.delegate respondsToSelector:@selector(activityTrackerGetActivityGoalAchievedRateResponseDay:date:goalAchievedRate:activitySpeed:ex:goalFinishedPercent:)]) {
                [self.delegate activityTrackerGetActivityGoalAchievedRateResponseDay:dayIndex
                                                                                date:date
                                                                    goalAchievedRate:goalAchievedRate
                                                                       activitySpeed:activitySpeed
                                                                                  ex:ex
                                                                 goalFinishedPercent:goalFinishedPercent];
            }
        });
    }
    
    return YES;
}

/*******
 * ECG *
 *******/

#define CMD_START_ECG_MODE 0x99

- (void)startECGMode
{
    [self log:@"startECGMode" data:@""];
    Byte bytes[16] = { CMD_START_ECG_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

#define CMD_ECG_MODE_UPDATES 0xA9

- (BOOL)ECGModeResponse:(NSData *)data
{
    //[self log:@"ECGModeResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    NSDate *date = [NSDate date];
    NSMutableArray *ECGData = [[NSMutableArray alloc] init];
    for (int i = 1; i != 14; ++i) {
        [ECGData addObject:@(bytes[i])];
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerECGModeResponseDate:data:)]) {
            [self.delegate activityTrackerECGModeResponseDate:date
                                                         data:ECGData];
        }
    });
    
    return YES;
}

#define CMD_ECG_MODE_RATE_UPDATES 0x94

- (BOOL)ECGModeRateResponse:(NSData *)data
{
    //[self log:@"ECGModeRateResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    int heartRate = bytes[AA];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerECGModeRateResponse:)]) {
            [self.delegate activityTrackerECGModeRateResponse:heartRate];
        }
    });
    
    return YES;
}

#define CMD_STOP_ECG_MODE 0x98
#define CMD_STOP_ECG_MODE_OK 0xA8
#define CMD_STOP_ECG_MODE_ERROR 0xBA

- (void)stopECGMode
{
    [self log:@"stopECGMode" data:@""];
    Byte bytes[16] = { CMD_STOP_ECG_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)stopECGModeResponse:(NSData *)data
{
    [self log:@"stopECGModeResponse" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    /*dispatch_async(dispatch_get_main_queue(), ^{
     if ([self.delegate respondsToSelector:@selector(activityTrackerStopRealTimeMeterModeResponse)]) {
     [self.delegate activityTrackerStopRealTimeMeterModeResponse];
     }
     });*/
    
    return YES;
}

#define CMD_DELETE_ECG_DATA 0x97
#define CMD_DELETE_ECG_DATA_ERROR 0xA7

- (void)deleteECGData
{
    [self log:@"deleteECGData" data:@""];
    Byte bytes[16] = { CMD_DELETE_ECG_DATA, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)deleteECGDataResponse:(NSData *)data
{
    [self log:@"deleteECGDataResponse" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerDeleteECGDataResponse)]) {
            [self.delegate activityTrackerDeleteECGDataResponse];
        }
    });
    
    return YES;
}

#define CMD_GET_ECG_DATA 0x96
#define CMD_GET_ECG_DATA_ERROR 0xA6

- (void)getECGData:(Byte)index
{
    [self log:@"getECGData" data:@""];
    Byte bytes[16] = { CMD_GET_ECG_DATA, index, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)getECGDataResponse:(NSData *)data
{
    [self log:@"getECGDataResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    Byte year = byteFromBCD(bytes[BB]);
    Byte month = byteFromBCD(bytes[CC]);
    Byte day = byteFromBCD(bytes[DD]);
    Byte hour = byteFromBCD(bytes[EE]);
    Byte minute = byteFromBCD(bytes[FF]);
    Byte second = byteFromBCD(bytes[GG]);
    Byte heartRate = bytes[HH];
    
    NSDate *date = [self gmtDateFromDay:day month:month year:year hour:hour minute:minute second:second];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerGetECGDataResponseDate:heartRate:)]) {
            [self.delegate activityTrackerGetECGDataResponseDate:date
                                                       heartRate:heartRate];
        }
    });
    
    return YES;
}

#define CMD_RESET_TO_FACTORY_SETTINGS 0x12

- (void)resetToFactorySettings
{
    [self log:@"resetToFactorySettings" data:@""];
    Byte bytes[16] = { CMD_RESET_TO_FACTORY_SETTINGS, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    self.cmds = [[NSMutableArray alloc] init];
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)resetToFactorySettingsResponse:(NSData *)data
{
    [self log:@"resetToFactorySettingsResponse" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerResetToFactorySettingsResponse)]) {
            [self.delegate activityTrackerResetToFactorySettingsResponse];
        }
    });
    
    return YES;
}

#define CMD_RESET_MCU 0x2E

- (void)resetMCU
{
    [self log:@"resetMCU" data:@""];
    Byte bytes[16] = { CMD_RESET_MCU, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)resetMCUResponse:(NSData *)data
{
    [self log:@"resetMCU" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerResetMCUResponse)]) {
            [self.delegate activityTrackerResetMCUResponse];
        }
    });
    
    return YES;
}

#define CMD_GET_FIRMWARE_VERSION 0x27

- (void)getFirmwareVersion
{
    [self log:@"getFirmwareVersion" data:@""];
    Byte bytes[16] = { CMD_GET_FIRMWARE_VERSION, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    self.cmds = [[NSMutableArray alloc] init];
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)getFirmwareVersionResponse:(NSData *)data
{
    [self log:@"getFirmwareVersionResponse" data:@"%@", data];
    
    const Byte *bytes = (const Byte *)data.bytes;
    
    NSString *version = [NSString stringWithFormat:@"Version %i %i %i %i", byteFromBCD(bytes[AA]), byteFromBCD(bytes[BB]), byteFromBCD(bytes[CC]), byteFromBCD(bytes[DD])];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerGetFirmwareVersionResponse:)]) {
            [self.delegate activityTrackerGetFirmwareVersionResponse:version];
        }
    });
    
    return YES;
}

#define CMD_FIRMWARE_UPDATE 0x47

- (void)firmwareUpdate
{
    [self log:@"firmwareUpdate" data:@""];
    Byte bytes[16] = { CMD_FIRMWARE_UPDATE, 0x55, 0xAA, 0x5A, 0xA5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    self.cmds = [[NSMutableArray alloc] init];
    [self sendCmdDataWithCRC:bytes];
}

- (BOOL)firmwareUpdateResponse:(NSData *)data
{
    [self log:@"firmwareUpdateResponse" data:@"%@", data];
    
    //const Byte *bytes = (const Byte *)data.bytes;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(activityTrackerFirmwareUpdateResponse)]) {
            [self.delegate activityTrackerFirmwareUpdateResponse];
        }
    });
    
    return YES;
}


#pragma mark Communication

- (void)enqueueCmdData:(NSData *)data
{
    //[self log:@"enqueueCmdData" data:@"%@", data];
    [self.cmds addObject:data];
}

- (void)sendCmdDataWithCRC:(Byte[16])bytes
{
    bytes[CRC] = [self calculateCRC:bytes];
    NSData *data = [[NSData alloc] initWithBytes:bytes length:16];
    if (self.cmds.count == 0) {
        [self enqueueCmdData:data];
        [self sendNextCmd];
    } else {
        [self enqueueCmdData:data];
    }
}

- (void)sendNextCmd
{
    NSData *data = [self.cmds firstObject];
    //[self log:@"sendNextCmd" data:@"%@", data];
    if (data) {
        [self.peripheral writeValue:data
                  forCharacteristic:self.activityTrackerTX
                               type:CBCharacteristicWriteWithResponse];
    }
}

- (Byte)calculateCRC:(Byte[])bytes
{
    Byte crc = 0;
    for(int i = 0; i != 15; ++i) {
        crc += bytes[i];
    }
    //[self log:@"crc = %x", crc];
    return crc;
}

Byte byteFromBCD(Byte bcd)
{
    Byte high = (bcd & 0xf0) >> 4;
    Byte low = bcd & 0x0f;
    Byte byte = (10 * high) + low;
    
    //[self log:@"%02x = %02x %02x => %d", bcd, high, low, byte);
    
    return byte;
}

Byte BCDFromByte(Byte byte)
{
    Byte high = byte / 10;
    Byte low = byte % 10;
    Byte bcd = (high << 4) + low;
    
    //[self log:@"%02x = %02x %02x => %d", bcd, high, low, byte);
    
    return bcd;
}

int intFrom4Bytes(Byte a, Byte b, Byte c, Byte d)
{
    return 256 * 256 * 256 * a + 256 * 256 * b + 256 * c + d;
}

int intFrom3Bytes(Byte a, Byte b, Byte c)
{
    return 256 * 256 * a + 256 * b + c;
}

int intFrom2Bytes(Byte a, Byte b)
{
    return 256 * a + b;
}

- (NSDate *)gmtDateFromDay:(int)day month:(int)month year:(int)year hour:(int)hour minute:(int)minute second:(int)second
{
    NSDateFormatter *gmt = [[NSDateFormatter alloc] init];
    gmt.dateFormat = @"dd/MM/yyyy HH:mm:ss";
    [gmt setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    
    NSDate *date = [gmt dateFromString:[NSString stringWithFormat:@"%02d/%02d/20%02d %02d:%02d:%02d", day, month, year, hour, minute, second]];
    return date;
}

- (NSDate *)dateFromDay:(int)day month:(int)month year:(int)year hour:(int)hour minute:(int)minute second:(int)second
{
    NSDateFormatter *ddMMyyyyHHmmss = [[NSDateFormatter alloc] init];
    ddMMyyyyHHmmss.dateFormat = @"dd/MM/yyyy HH:mm:ss";
    
    NSDate *date = [ddMMyyyyHHmmss dateFromString:[NSString stringWithFormat:@"%02d/%02d/20%02d %02d:%02d:%02d", day, month, year, hour, minute, second]];
    return date;
}

- (void)BCDBytesFromDate:(NSDate *)date day:(Byte *)day month:(Byte *)month year:(Byte *)year hour:(Byte *)hour minute:(Byte *)minute second:(Byte *)second
{
    NSDateComponents *components = [[NSCalendar currentCalendar] components:NSCalendarUnitDay|NSCalendarUnitMonth|NSCalendarUnitYear|NSCalendarUnitHour|NSCalendarUnitMinute|NSCalendarUnitSecond fromDate:date];
    
    *day = BCDFromByte([components day]);
    *month = BCDFromByte([components month]);
    *year = BCDFromByte([components year] - 2000);
    *hour = BCDFromByte([components hour]);
    *minute = BCDFromByte([components minute]);
    *second = BCDFromByte([components second]);
}

- (NSString *)characteristicName:(CBCharacteristic *)characteristic
{
    if (characteristic == self.activityTrackerTX) return @"activityTrackerTX";
    if (characteristic == self.activityTrackerRX) return @"activityTrackerRX";
    return @"?";
}

#pragma mark parseResponse

- (void)parseResponse:(NSData *)data
{
    BOOL done = NO;
    
    const Byte cmd = *(const Byte *)data.bytes;
    
    // ECG CMDs
    switch (cmd) {
        case CMD_GET_ECG_DATA:
        case CMD_GET_ECG_DATA_ERROR:
            done = [self getECGDataResponse:data];
            break;
        case CMD_DELETE_ECG_DATA:
        case CMD_DELETE_ECG_DATA_ERROR:
            done = [self deleteECGDataResponse:data];
            break;
        case CMD_STOP_ECG_MODE:
        case CMD_STOP_ECG_MODE_OK:
        case CMD_STOP_ECG_MODE_ERROR:
            done = [self stopECGModeResponse:data];
            break;
        case CMD_START_ECG_MODE: // ¿Llega alguna vez? Creo que no
        case CMD_ECG_MODE_UPDATES:
            done = [self ECGModeResponse:data];
            // Eliminar el primero de la cola solo si es el start, mejor que este return
            if (self.cmds.count > 0) {
                NSData *data = [self.cmds firstObject];
                const Byte *bytes = (const Byte *)data.bytes;
                if (bytes[0] == CMD_START_ECG_MODE) {
                    [self.cmds removeObjectAtIndex:0];
                }
            }
            return;
            break;
        case CMD_ECG_MODE_RATE_UPDATES:
            done = [self ECGModeRateResponse:data];
            // Eliminar el primero de la cola solo si es el start, mejor que este return
            return;
            break;
    }
    
    // Other CMDs
    if(!done) switch (cmd & CMD_ERROR_MASK) {
        case CMD_SET_DEVICE_ID:
            done = [self setDeviceIdResponse:data];
            break;
        case CMD_SAFE_BONDING_SAVE_PASSWORD:
            done = [self safeBondingSavePasswordResponse:data];
            break;
        case CMD_SAFE_BONDING_SEND_PASSWORD:
            done = [self safeBondingSendPasswordResponse:data];
            break;
        case CMD_SAFE_BONDING_STATUS:
            done = [self safeBondingStatusResponse:data];
            break;
        case CMD_SET_TIME:
            done = [self setTimeResponse:data];
            break;
        case CMD_GET_TIME:
            done = [self getTimeResponse:data];
            break;
        case CMD_SET_PERSONAL_INFORMATION:
            done = [self setPersonalInformationResponse:data];
            break;
        case CMD_GET_PERSONAL_INFORMATION:
            done = [self getPersonalInformationResponse:data];
            break;
        case CMD_GET_TOTAL_ACTIVITY_DATA:
            done = [self getTotalActivityDataResponse:data];
            break;
        case CMD_GET_DETAIL_ACTIVITY_DATA:
            done = [self getDetailActivityDataResponse:data];
            break;
        case CMD_DELETE_ACTIVITY_DATA:
            done = [self deleteActivityDataResponse:data];
            break;
        case CMD_START_REAL_TIME_METER_MODE_AND_UPDATES:
            done = [self realTimeMeterModeResponse:data];
            break;
        case CMD_STOP_REAL_TIME_METER_MODE:
            done = [self stopRealTimeMeterModeResponse:data];
            break;
        case CMD_GET_SLEEP_MONITOR_MODE:
            done = [self getSleepMonitorModeResponse:data];
            break;
        case CMD_SWITCH_SLEEP_MONITOR_MODE:
            done = [self switchSleepMonitorModeResponse:data];
            break;
        case CMD_GET_CURRENT_ACTIVITY_INFORMATION:
            done = [self getCurrentActivityInformationResponse:data];
            break;
        case CMD_QUERY_DATA_STORAGE:
            done = [self queryDataStorageResponse:data];
            break;
        case CMD_SET_TARGET_STEPS:
            done = [self setTargetStepsResponse:data];
            break;
        case CMD_GET_TARGET_STEPS:
            done = [self getTargetStepsResponse:data];
            break;
        case CMD_GET_ACTIVITY_GOAL_ACHIEVED_RATE:
            done = [self getActivityGoalAchievedRateResponse:data];
            break;
        case CMD_RESET_TO_FACTORY_SETTINGS:
            done = [self resetToFactorySettingsResponse:data];
            break;
        case CMD_RESET_MCU:
            done = [self resetMCUResponse:data];
            break;
        case CMD_GET_FIRMWARE_VERSION:
            done = [self getFirmwareVersionResponse:data];
            break;
        case CMD_FIRMWARE_UPDATE:
            done = [self firmwareUpdateResponse:data];
            break;
    }
    if (done) {
        //NSLog(@"----8<----8<----8<---- %lu", (unsigned long)self.cmds.count);
        if (self.cmds.count > 0) [self.cmds removeObjectAtIndex:0];
        if (self.cmds.count > 0) [self sendNextCmd];
    }
}

#pragma mark CBPeripheralDelegate

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    NSLog(@"peripheral: %@ %@ didDiscoverServices", peripheral.name, peripheral.identifier.UUIDString);
    
    for (CBService *service in peripheral.services) {
        NSLog(@"service: %@ (%@)", service.UUID, service.UUID.UUIDString);
        
        if ([service.UUID.UUIDString isEqualToString:@"FFF0"]) {
            self.activityTrackerService = service;
            [peripheral discoverCharacteristics:@[[CBUUID UUIDWithString:@"FFF6"], [CBUUID UUIDWithString:@"FFF7"]] forService:service];
        }
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service
             error:(NSError *)error
{
    NSLog(@"peripheral: %@ %@ didDiscoverCharacteristicsForService: %@ (%@)",
          peripheral.name, peripheral.identifier.UUIDString,
          service.UUID, service.UUID.UUIDString);
    
    for (CBCharacteristic *characteristic in service.characteristics) {
        NSLog(@"characteristic: %@ (%@) =>  isNotifying: %@", characteristic.UUID, characteristic.UUID.UUIDString, characteristic.isNotifying ? @"YES" : @"NO");
        
        if ([characteristic.UUID.UUIDString isEqualToString:@"FFF6"]) {
            self.activityTrackerTX = characteristic;
        }
        if ([characteristic.UUID.UUIDString isEqualToString:@"FFF7"]) {
            self.activityTrackerRX = characteristic;
            [self.peripheral setNotifyValue:YES forCharacteristic:self.activityTrackerRX];
        }
    }
    if (!self.isReady && self.activityTrackerTX && self.activityTrackerRX) {
        self.isReady = YES;
        
        if ([self.delegate respondsToSelector:@selector(activityTrackerReady:)]) {
            [self.delegate activityTrackerReady:self];
        }
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic
             error:(NSError *)error
{
    NSData *data = [characteristic.value copy];
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
        [self parseResponse:data];
    });
}

@end
