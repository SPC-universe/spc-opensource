#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

@class ActivityTracker;

@protocol ActivityTrackerDelegate <NSObject>

@optional
- (void)activityTrackerReady:(ActivityTracker *)activityTracker;

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

- (void)activityTrackerGetDetailActivityDataResponseIndex:(int)index
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

@end

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

@end
