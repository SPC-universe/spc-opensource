#import "ActivityTracker.h"

@interface ActivityTracker () <CBPeripheralDelegate>

@property (strong, nonatomic) CBPeripheral *peripheral;
@property (strong, nonatomic) CBService *activityTrackerService;
@property (strong, nonatomic) CBCharacteristic *activityTrackerTX;
@property (strong, nonatomic) CBCharacteristic *activityTrackerRX;

@end

@implementation ActivityTracker

- (instancetype)initWithPeripheral:(CBPeripheral *)peripheral
                          delegate:(id<ActivityTrackerDelegate>)delegate
{
    self = [super init];
    if (self) {
        _peripheral = peripheral;
        _delegate = delegate;

        self.peripheral.delegate = self;
        [self.peripheral discoverServices:@[[CBUUID UUIDWithString:@"FFF0"]]];
    }
    return self;
}

-(BOOL)isReady
{
    return self.peripheral &&
        self.peripheral.state == CBPeripheralStateConnected &&
        self.activityTrackerService &&
        self.activityTrackerTX &&
        self.activityTrackerRX;
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

#define CMD_SET_TIME 0x01

- (void)setTime:(NSDate *)date
{
    NSLog(@"setTime: %@", date);

    Byte year, month, day, hour, minute, second;

    [self BCDBytesFromDate:date day:&day month:&month year:&year hour:&hour minute:&minute second:&second];

    NSLog(@"%02x/%02x/%02x %02x:%02x:%02x", day, month, year, hour, minute, second);

    Byte bytes[16] = { CMD_SET_TIME, year, month, day, hour, minute, second, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)setTimeResponse:(NSData *)data
{
    NSLog(@"setTimeResponse: %@", data);

    //const Byte *bytes = (const Byte *)data.bytes;

    if ([self.delegate respondsToSelector:@selector(activityTrackerSetTimeResponse)]) {
        [self.delegate activityTrackerSetTimeResponse];
    }
}

#define CMD_GET_TIME 0x41

- (void)getTime
{
    NSLog(@"getTime");
    Byte bytes[16] = { CMD_GET_TIME, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)getTimeResponse:(NSData *)data
{
    NSLog(@"getTimeResponse: %@", data);

    const Byte *bytes = (const Byte *)data.bytes;

    Byte year = byteFromBCD(bytes[AA]);
    Byte month = byteFromBCD(bytes[BB]);
    Byte day = byteFromBCD(bytes[CC]);
    Byte hour = byteFromBCD(bytes[DD]);
    Byte minute = byteFromBCD(bytes[EE]);
    Byte second = byteFromBCD(bytes[FF]);

    NSDate *date = [self dateFromDay:day month:month year:year hour:hour minute:minute second:second];

    if ([self.delegate respondsToSelector:@selector(activityTrackerGetTimeResponse:)]) {
        [self.delegate activityTrackerGetTimeResponse:date];
    }
}

#define CMD_SET_PERSONAL_INFORMATION 0x02

- (void)setPersonalInformationMale:(BOOL)male
                               age:(Byte)age
                            height:(Byte)height
                            weight:(Byte)weight
                            stride:(Byte)stride
{
    NSLog(@"setPersonalInformation: %d %d %d %d %d", male, age, height, weight, stride);

    Byte bytes[16] = { CMD_SET_PERSONAL_INFORMATION, male, age, height, weight, stride, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)setPersonalInformationResponse:(NSData *)data
{
    NSLog(@"setPersonalInformation");

    if ([self.delegate respondsToSelector:@selector(activityTrackerSetPersonalInformationResponse)]) {
        [self.delegate activityTrackerSetPersonalInformationResponse];
    }
}

#define CMD_GET_PERSONAL_INFORMATION 0x42

- (void)getPersonalInformation
{
    NSLog(@"getPersonalInformation");

    Byte bytes[16] = { CMD_GET_PERSONAL_INFORMATION, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)getPersonalInformationResponse:(NSData *)data
{
    NSLog(@"getPersonalInformationResponse: %@", data);

    const Byte *bytes = (const Byte *)data.bytes;

    BOOL male = bytes[AA];
    int age = bytes[BB];
    int height = bytes[CC];
    int weight = bytes[DD];
    int stride = bytes[EE];
    NSString *deviceId = [NSString stringWithFormat:@"(%02x %02x %02x %02x %02x %02x)",
                          bytes[FF],
                          bytes[GG],
                          bytes[HH],
                          bytes[II],
                          bytes[JJ],
                          bytes[KK]];

    if ([self.delegate respondsToSelector:@selector(activityTrackerGetPersonalInformationResponseMale:age:height:weight:stride:deviceId:)]) {
        [self.delegate activityTrackerGetPersonalInformationResponseMale:male
                                                                     age:age
                                                                  height:height
                                                                  weight:weight
                                                                  stride:stride
                                                                deviceId:deviceId];
    }
}

#define CMD_GET_TOTAL_ACTIVITY_DATA 0x07

- (void)getTotalActivityData:(Byte)day
{
    NSLog(@"getTotalActivityData: %d", day);
    Byte bytes[16] = { CMD_GET_TOTAL_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)getTotalActivityDataResponse:(NSData *)data
{
    NSLog(@"getTotalActivityDataResponse: %@", data);

    const Byte *bytes = (const Byte *)data.bytes;

    Byte dayIndex = bytes[BB];

    Byte year = byteFromBCD(bytes[CC]);
    Byte month = byteFromBCD(bytes[DD]);
    Byte day = byteFromBCD(bytes[EE]);

    NSDate *date = [self dateFromDay:day month:month year:year hour:0 minute:0 second:0];

    if (bytes[AA] == 0) {
        int steps = intFrom3Bytes(bytes[FF], bytes[GG], bytes[HH]);
        int aerobicSteps = intFrom3Bytes(bytes[II], bytes[JJ], bytes[KK]);
        int cal = intFrom3Bytes(bytes[LL], bytes[MM], bytes[NN]);

        if ([self.delegate respondsToSelector:@selector(activityTrackerGetTotalActivityDataResponseDay:date:steps:aerobicSteps:cal:)]) {
            [self.delegate activityTrackerGetTotalActivityDataResponseDay:dayIndex
                                                                     date:date
                                                                    steps:steps
                                                             aerobicSteps:aerobicSteps
                                                                      cal:cal];
        }
    }
    if (bytes[AA] == 1) {
        int km = intFrom3Bytes(bytes[FF], bytes[GG], bytes[HH]);
        int activityTime = intFrom2Bytes(bytes[II], bytes[JJ]);

        if ([self.delegate respondsToSelector:@selector(activityTrackerGetTotalActivityDataResponseDay:date:km:activityTime:)]) {
            [self.delegate activityTrackerGetTotalActivityDataResponseDay:dayIndex
                                                                     date:date
                                                                       km:km
                                                             activityTime:activityTime];
        }
    }
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
    NSLog(@"getDetailActivityData: %d", day);

    NSLog(@"getTotalActivityData: %d", day);
    Byte bytes[16] = { CMD_GET_DETAIL_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)getDetailActivityDataResponse:(NSData *)data
{
    NSLog(@"getDetailActivityDataResponse: %@", data);

    const Byte *bytes = (const Byte *)data.bytes;

    if (bytes[D_TYPE_INDEX] == D_WITHOUT_DATA) {
        if ([self.delegate respondsToSelector:@selector(activityTrackerGetDetailActivityDataResponseWithoutData)]) {
            [self.delegate activityTrackerGetDetailActivityDataResponseWithoutData];
        }
    }
    if (bytes[D_TYPE_INDEX] == D_WITH_DATA) {
        Byte year = byteFromBCD(bytes[D_AA]);
        Byte month = byteFromBCD(bytes[D_BB]);
        Byte day = byteFromBCD(bytes[D_CC]);

        Byte index = bytes[D_DD];
        Byte hour = index / 4;
        Byte minute = 15 * (index % 4);

        NSLog(@"%d => %02d:%02d", index, hour, minute);

        if (bytes[D_EE] == D_ACTIVITY_DATA) {
            NSDate *date = [self dateFromDay:day month:month year:year hour:hour minute:minute second:0];
            
            int cal = intFrom2Bytes(bytes[D_GG], bytes[D_FF]);
            int steps = intFrom2Bytes(bytes[D_II], bytes[D_HH]);
            int km = intFrom2Bytes(bytes[D_KK], bytes[D_JJ]);
            int aerobicSteps = intFrom2Bytes(bytes[D_MM], bytes[D_LL]);

            if ([self.delegate respondsToSelector:@selector(activityTrackerGetDetailActivityDataDayResponseIndex:date:steps:aerobicSteps:cal:km:)]) {
                [self.delegate activityTrackerGetDetailActivityDataDayResponseIndex:index
                                                                               date:date
                                                                              steps:steps
                                                                       aerobicSteps:aerobicSteps
                                                                                cal:cal
                                                                                 km:km];
            }
        }
        if (bytes[D_EE] == D_SLEEP_QUALITY_DATA) {
            for (int i = 0; i != 8; ++i) {
                int sleepQuality = bytes[D_FF + i];

                NSLog(@"%d => %02d:%02d", index, hour, minute);

                NSDate *date = [self dateFromDay:day month:month year:year hour:hour minute:(minute + 2*i) second:0];

                if ([self.delegate respondsToSelector:@selector(activityTrackerGetDetailActivityDataSleepResponseIndex:date:sleepQuality:)]) {
                    [self.delegate activityTrackerGetDetailActivityDataSleepResponseIndex:index
                                                                                     date:date
                                                                             sleepQuality:sleepQuality];
                }
            }
        }
    }
}

#define CMD_DELETE_ACTIVITY_DATA 0x04

- (void)deleteActivityData:(Byte)day
{
    NSLog(@"deleteActivityData: %d", day);
    Byte bytes[16] = { CMD_DELETE_ACTIVITY_DATA, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)deleteActivityDataResponse:(NSData *)data
{
    NSLog(@"deleteActivityDataResponse: %@", data);

    //const Byte *bytes = (const Byte *)data.bytes;

    if ([self.delegate respondsToSelector:@selector(activityTrackerDeleteActivityDataResponse)]) {
        [self.delegate activityTrackerDeleteActivityDataResponse];
    }
}

#define CMD_START_REAL_TIME_METER_MODE_AND_UPDATES 0x09

- (void)startRealTimeMeterMode
{
    NSLog(@"startRealTimeMeterMode");
    Byte bytes[16] = { CMD_START_REAL_TIME_METER_MODE_AND_UPDATES, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)realTimeMeterModeResponse:(NSData *)data
{
    NSLog(@"realTimeMeterModeResponse: %@", data);

    const Byte *bytes = (const Byte *)data.bytes;

    int steps = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);
    int aerobicSteps = intFrom3Bytes(bytes[DD], bytes[EE], bytes[FF]);
    int cal = intFrom3Bytes(bytes[GG], bytes[HH], bytes[II]);
    int km = intFrom3Bytes(bytes[JJ], bytes[KK], bytes[LL]);
    int activityTime = intFrom2Bytes(bytes[MM], bytes[NN]);

    if ([self.delegate respondsToSelector:@selector(activityTrackerRealTimeModeResponseSteps:aerobicSteps:cal:km:activityTime:)]) {
        [self.delegate activityTrackerRealTimeModeResponseSteps:steps
                                                   aerobicSteps:aerobicSteps
                                                            cal:cal
                                                             km:km
                                                   activityTime:activityTime];
    }
}

#define CMD_STOP_REAL_TIME_METER_MODE 0x0A

- (void)stopRealTimeMeterMode
{
    NSLog(@"stopRealTimeMeterMode");
    Byte bytes[16] = { CMD_STOP_REAL_TIME_METER_MODE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)stopRealTimeMeterModeResponse:(NSData *)data
{
    NSLog(@"stopRealTimeMeterModeResponse: %@", data);

    //const Byte *bytes = (const Byte *)data.bytes;

    if ([self.delegate respondsToSelector:@selector(activityTrackerStopRealTimeMeterModeResponse)]) {
        [self.delegate activityTrackerStopRealTimeMeterModeResponse];
    }
}

#define CMD_GET_CURRENT_ACTIVITY_INFORMATION 0x48

- (void)getCurrentActivityInformation
{
    NSLog(@"getCurrentActivityInformation");
    Byte bytes[16] = { CMD_GET_CURRENT_ACTIVITY_INFORMATION, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)getCurrentActivityInformationResponse:(NSData *)data
{
    NSLog(@"getCurrentActivityInformationResponse: %@", data);

    //const Byte *bytes = (const Byte *)data.bytes;

    if ([self.delegate respondsToSelector:@selector(activityTrackerGetCurrentActivityInformationResponse)]) {
        [self.delegate activityTrackerGetCurrentActivityInformationResponse];
    }
}

#define CMD_QUERY_DATA_STORAGE 0x46

- (void)queryDataStorage
{
    NSLog(@"queryDataStorage");
    Byte bytes[16] = { CMD_QUERY_DATA_STORAGE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)queryDataStorageResponse:(NSData *)data
{
    NSLog(@"queryDataStorageResponse: %@", data);

    //const Byte *bytes = (const Byte *)data.bytes;

    if ([self.delegate respondsToSelector:@selector(activityTrackerQueryDataStorageResponse)]) {
        [self.delegate activityTrackerQueryDataStorageResponse];
    }
}

#define CMD_SET_TARGET_STEPS 0x0B

- (void)setTargetSteps:(int)steps
{
    NSLog(@"setTargetSteps: %d", steps);

    Byte steps1 = 0xff & (steps >> 16);
    Byte steps2 = 0xff & (steps >> 8);
    Byte steps3 = 0xff & steps;

    NSLog(@"%x %x %x %x", steps, steps1, steps2, steps3);

    Byte bytes[16] = { CMD_SET_TARGET_STEPS, steps1, steps2, steps3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)setTargetStepsResponse:(NSData *)data
{
    NSLog(@"setTargetStepsResponse: %@", data);

    //const Byte *bytes = (const Byte *)data.bytes;

    if ([self.delegate respondsToSelector:@selector(activityTrackerSetTargetStepsResponse)]) {
        [self.delegate activityTrackerSetTargetStepsResponse];
    }
}

#define CMD_GET_TARGET_STEPS 0x4B

- (void)getTargetSteps
{
    NSLog(@"getTargetSteps");
    Byte bytes[16] = { CMD_GET_TARGET_STEPS, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)getTargetStepsResponse:(NSData *)data
{
    NSLog(@"getTargetStepsResponse: %@", data);

    const Byte *bytes = (const Byte *)data.bytes;

    int steps = intFrom3Bytes(bytes[AA], bytes[BB], bytes[CC]);

    if ([self.delegate respondsToSelector:@selector(activityTrackerGetTargetStepsResponse:)]) {
        [self.delegate activityTrackerGetTargetStepsResponse:steps];
    }
}

#define CMD_GET_ACTIVITY_GOAL_ACHIEVED_RATE 0x08

- (void)getActivityGoalAchievedRate:(Byte)day
{
    NSLog(@"getActivityGoalAchievedRateDay: %d", day);
    Byte bytes[16] = { CMD_GET_ACTIVITY_GOAL_ACHIEVED_RATE, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    [self sendCmdDataWithCRC:bytes];
}

- (void)getActivityGoalAchievedRateResponse:(NSData *)data
{
    NSLog(@"getActivityGoalAchievedRateDayResponse: %@", data);

    const Byte *bytes = (const Byte *)data.bytes;

    Byte dayIndex = bytes[AA];

    Byte year = byteFromBCD(bytes[BB]);
    Byte month = byteFromBCD(bytes[CC]);
    Byte day = byteFromBCD(bytes[DD]);

    if (year && month && day) {
        NSDate *date = [self dateFromDay:day month:month year:year hour:0 minute:0 second:0];

        Byte goalAchievedRate = bytes[EE];

        int activitySpeed = intFrom2Bytes(bytes[FF], bytes[GG]);
        int ex = intFrom3Bytes(bytes[HH], bytes[II], bytes[JJ]);
        int goalFinishedPercent = intFrom2Bytes(bytes[KK], bytes[LL]);

        if ([self.delegate respondsToSelector:@selector(activityTrackerGetActivityGoalAchievedRateResponseDay:date:goalAchievedRate:activitySpeed:ex:goalFinishedPercent:)]) {
            [self.delegate activityTrackerGetActivityGoalAchievedRateResponseDay:dayIndex
                                                                            date:date
                                                                goalAchievedRate:goalAchievedRate
                                                                   activitySpeed:activitySpeed
                                                                              ex:ex
                                                             goalFinishedPercent:goalFinishedPercent];
        }
    }
}

#pragma mark Communication

- (void)sendCmdDataWithCRC:(Byte[16])bytes
{
    bytes[CRC] = [self calculateCRC:bytes];
    NSData *data = [[NSData alloc] initWithBytes:bytes length:16];

    if (self.isReady) {
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
    NSLog(@"crc = %x", crc);
    return crc;
}

Byte byteFromBCD(Byte bcd)
{
    Byte high = (bcd & 0xf0) >> 4;
    Byte low = bcd & 0x0f;
    Byte byte = (10 * high) + low;

    NSLog(@"%02x = %02x %02x => %d", bcd, high, low, byte);

    return byte;
}

Byte BCDFromByte(Byte byte)
{
    Byte high = byte / 10;
    Byte low = byte % 10;
    Byte bcd = (high << 4) + low;

    NSLog(@"%02x = %02x %02x => %d", bcd, high, low, byte);

    return bcd;
}

int intFrom3Bytes(Byte a, Byte b, Byte c)
{
    return 256 * 256 * a + 256 * b + c;
}

int intFrom2Bytes(Byte a, Byte b)
{
    return 256 * a + b;
}

- (NSDate *)dateFromDay:(Byte)day month:(Byte)month year:(Byte)year hour:(Byte)hour minute:(Byte)minute second:(Byte)second
{
    NSDateFormatter *ddmmyyyy = [[NSDateFormatter alloc] init];
    ddmmyyyy.dateFormat = @"dd/MM/yyyy HH:mm:ss";
    NSDate *date = [ddmmyyyy dateFromString:[NSString stringWithFormat:@"%02d/%02d/20%02d %02d:%02d:%02d", day, month, year, hour, minute, second]];
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

#define CMD_ERROR_MASK 0x7f

- (void)parseResponse:(NSData *)data
{
    NSLog(@"parseResponse: %@", data);

    const Byte cmd = *(const Byte *)data.bytes;
    switch (cmd & CMD_ERROR_MASK) {
        case CMD_SET_TIME:
            [self setTimeResponse:data];
            break;
        case CMD_GET_TIME:
            [self getTimeResponse:data];
            break;
        case CMD_SET_PERSONAL_INFORMATION:
            [self setPersonalInformationResponse:data];
            break;
        case CMD_GET_PERSONAL_INFORMATION:
            [self getPersonalInformationResponse:data];
            break;
        case CMD_GET_TOTAL_ACTIVITY_DATA:
            [self getTotalActivityDataResponse:data];
            break;
        case CMD_GET_DETAIL_ACTIVITY_DATA:
            [self getDetailActivityDataResponse:data];
            break;
        case CMD_DELETE_ACTIVITY_DATA:
            [self deleteActivityDataResponse:data];
            break;
        case CMD_START_REAL_TIME_METER_MODE_AND_UPDATES:
            [self realTimeMeterModeResponse:data];
            break;
        case CMD_STOP_REAL_TIME_METER_MODE:
            [self stopRealTimeMeterModeResponse:data];
            break;
        case CMD_GET_CURRENT_ACTIVITY_INFORMATION:
            [self getCurrentActivityInformationResponse:data];
            break;
        case CMD_QUERY_DATA_STORAGE:
            [self queryDataStorageResponse:data];
            break;
        case CMD_SET_TARGET_STEPS:
            [self setTargetStepsResponse:data];
            break;
        case CMD_GET_TARGET_STEPS:
            [self getTargetStepsResponse:data];
            break;
        case CMD_GET_ACTIVITY_GOAL_ACHIEVED_RATE:
            [self getActivityGoalAchievedRateResponse:data];
            break;
    }
}

#pragma mark CBPeripheralDelegate

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    NSLog(@"peripheral: %@ didDiscoverServices", peripheral.name);

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
    NSLog(@"peripheral: %@ didDiscoverCharacteristicsForService: %@ (%@)",
          peripheral.name,
          service.UUID, service.UUID.UUIDString);

    for (CBCharacteristic *characteristic in service.characteristics) {
        NSLog(@"characteristic: %@ (%@) =>  isNotifying: %@", characteristic.UUID, characteristic.UUID.UUIDString, characteristic.isNotifying ? @"YES" : @"NO");

        //[self.peripheral discoverDescriptorsForCharacteristic:characteristic];

        if ([characteristic.UUID.UUIDString isEqualToString:@"FFF6"]) {
            self.activityTrackerTX = characteristic;
            //[self.peripheral setNotifyValue:YES forCharacteristic:self.activityTrackerTX];
            //[peripheral readValueForCharacteristic:characteristic];
        }
        if ([characteristic.UUID.UUIDString isEqualToString:@"FFF7"]) {
            self.activityTrackerRX = characteristic;
            [self.peripheral setNotifyValue:YES forCharacteristic:self.activityTrackerRX];
            //[self.peripheral discoverDescriptorsForCharacteristic:characteristic];
            //[peripheral readValueForCharacteristic:characteristic];
        }
        if (self.activityTrackerTX && self.activityTrackerRX) {
            if ([self.delegate respondsToSelector:@selector(activityTrackerReady)]) {
                [self.delegate activityTrackerReady];
            }
        }
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didWriteValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    NSLog(@"didWriteValueForCharacteristic: %@ = %@", characteristic.UUID, characteristic.value);

    if (characteristic == self.activityTrackerTX) {
        //[self.peripheral readValueForCharacteristic:self.activityTrackerTX];
    }
    if (characteristic == self.activityTrackerRX) {
        //[self.peripheral readValueForCharacteristic:self.activityTrackerRX];
    }
}

-(void)peripheral:(CBPeripheral *)peripheral didUpdateNotificationStateForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    NSLog(@"didUpdateNotificationStateForCharacteristic: %@ (%@) isNotifying: %@",
          [self characteristicName:characteristic],
          characteristic.UUID,
          characteristic.isNotifying ? @"YES" : @"NO");
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic
             error:(NSError *)error
{
    NSLog(@"didUpdateValueForCharacteristic: %@ (%@) = %@", [self characteristicName:characteristic], characteristic.UUID, characteristic.value);

    [self parseResponse:characteristic.value];

    /*if (characteristic) {
        NSDictionary *postInfo = @{ @"characteristic": characteristic,
                                    @"UUID": characteristic.UUID,
                                    @"value": characteristic.value ? characteristic.value : @"(null)" };
        [self postUpdateNotification:postInfo];
    }*/
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverDescriptorsForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    NSLog(@"didDiscoverDescriptorsForCharacteristic: %@ (%@) = %@", [self characteristicName:characteristic], characteristic.UUID, characteristic.value);

    for (CBDescriptor *descriptor in characteristic.descriptors) {
        NSLog(@"descriptor: %@ (%@) => %@ %@", descriptor.UUID, descriptor.UUID.UUIDString, descriptor.description, descriptor.value);
    }
}

@end
