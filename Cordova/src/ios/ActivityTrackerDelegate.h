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

- (void)activityTrackerGetSleepMonitorModeResponse:(BOOL)sleepMode;
- (void)activityTrackerSwitchSleepMonitorModeResponse;

- (void)activityTrackerStartECGModeResponse;
- (void)activityTrackerECGModeResponseDate:(NSDate *)date
                                      data:(NSArray *)data;
- (void)activityTrackerECGModeRateResponse:(int)rate;
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
- (void)activityTrackerGetFirmwareVersionResponse:(NSString *)version;
- (void)activityTrackerFirmwareUpdateResponse;

@end
