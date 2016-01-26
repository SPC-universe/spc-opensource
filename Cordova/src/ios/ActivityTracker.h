#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "ActivityTrackerDelegate.h"
#import "SleepQualityDetailData.h"

@interface ActivityTracker : NSObject

@property (weak,nonatomic) id<ActivityTrackerDelegate> delegate;
@property (strong, nonatomic) CBPeripheral *peripheral;
@property (strong, nonatomic) NSMutableArray *logs;

- (instancetype)initWithPeripheral:(CBPeripheral *)peripheral
                          delegate:(id<ActivityTrackerDelegate>) delegate;
- (void)discoverServices;
- (BOOL)isReady;
- (void)sendNextCmd;

- (void)setDeviceId:(NSString *)deviceId;

- (void)safeBondingSavePassword:(NSString *)password;
- (void)safeBondingSendPassword:(NSString *)password;
- (void)safeBondingStatus;

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
- (void)getActivityGoalAchievedRate:(Byte)day;

- (void)resetToFactorySettings;
- (void)resetMCU;

- (void)getFirmwareVersion;
- (void)firmwareUpdate;

- (void)getSleepMonitorMode;
- (void)switchSleepMonitorMode;

- (void)startECGMode;
- (void)stopECGMode;
- (void)deleteECGData;
- (void)getECGData:(Byte)index;

@end
