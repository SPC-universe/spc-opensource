#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

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
- (void)activityTrackerGetActivityGoalAchievedRateResponseDay:(Byte)dayIndex
                                                         date:(NSDate *)date
                                             goalAchievedRate:(int)goalAchievedRate
                                                activitySpeed:(int)activitySpeed
                                                           ex:(int)ex
                                          goalFinishedPercent:(int)goalFinishedPercent;

@end

@interface ActivityTracker : NSObject

@property (weak,nonatomic) id<ActivityTrackerDelegate> delegate;

- (instancetype)initWithPeripheral:(CBPeripheral *)peripheral
                          delegate:(id<ActivityTrackerDelegate>) delegate;
- (BOOL)isReady;

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
