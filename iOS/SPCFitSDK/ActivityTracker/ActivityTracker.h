#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "ActivityTrackerDelegate.h"

@interface ActivityTracker : NSObject

@property (weak,nonatomic) id<ActivityTrackerDelegate> delegate;
@property (strong, nonatomic) CBPeripheral *peripheral;
@property (strong, nonatomic) NSMutableArray *logs;

- (instancetype)initWithPeripheral:(CBPeripheral *)peripheral
                          delegate:(id<ActivityTrackerDelegate>) delegate;
- (void)discoverServices;
- (BOOL)isReady;
- (void)sendNextCmd;

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
- (void)switchSleepMonitorMode;
- (void)queryDataStorage;
- (void)setTargetSteps:(int)steps;
- (void)getTargetSteps;
- (void)getActivityGoalAchievedRate:(Byte)day;

- (void)safeBondingSavePassword:(NSString *)password;
- (void)safeBondingSendPassword:(NSString *)password;
- (void)safeBondingStatus;

- (void)startECGMode;
- (void)stopECGMode;
- (void)deleteECGData;
- (void)getECGData:(Byte)index;

@end
