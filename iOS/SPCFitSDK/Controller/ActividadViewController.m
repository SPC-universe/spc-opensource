#import "ActividadViewController.h"
#import "ActivityTrackerManager.h"

@interface ActividadViewController () <ActivityTrackerDelegate>

@property (strong, nonatomic) ActivityTrackerManager *activityTrackerManager;
@property (strong, nonatomic) ActivityTracker *activityTracker;

@property (weak, nonatomic) IBOutlet UITextField *stepsField;
@property (weak, nonatomic) IBOutlet UITextField *caloriesField;
@property (weak, nonatomic) IBOutlet UITextField *kmField;

@end

@implementation ActividadViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    self.activityTrackerManager = [ActivityTrackerManager sharedInstance];
    self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activityTracker.peripheral delegate:self];
    [self.activityTracker discoverServices];
}

#pragma mark ActivityTrackerDelegate

- (void)activityTrackerReady:(ActivityTracker *)activityTracker
{
    NSLog(@"activityTrackerReady: %@", activityTracker);

    [self.activityTracker getTotalActivityData:0];
    //[self.activityTracker getCurrentActivityInformation];
}

-(void)activityTrackerGetTotalActivityDataResponseDay:(int)day
                                                 date:(NSDate *)date
                                                steps:(int)steps
                                         aerobicSteps:(int)aerobicSteps
                                             calories:(int)calories
{
    NSLog(@"activityTrackerGetTotalActivityDataResponse 1");

    self.stepsField.text = [NSString stringWithFormat:@"%i", steps];
    self.caloriesField.text = [NSString stringWithFormat:@"%0.2f", calories / 100.0];
}

- (void)activityTrackerGetTotalActivityDataResponseDay:(int)day
                                                  date:(NSDate *)date
                                              distance:(int)distance
                                          activityTime:(int)activityTime
{
    NSLog(@"activityTrackerGetTotalActivityDataResponse 2");

    self.kmField.text = [NSString stringWithFormat:@"%0.2f", distance / 100.0];
}

- (void)activityTrackerRealTimeModeResponseSteps:(int)steps
                                    aerobicSteps:(int)aerobicSteps
                                        calories:(int)calories
                                        distance:(int)distance
                                    activityTime:(int)activityTime
{
    NSLog(@"activityTrackerRealTimeModeResponse");

    self.stepsField.text = [NSString stringWithFormat:@"%i", steps];
    self.caloriesField.text = [NSString stringWithFormat:@"%0.2f", calories / 100.0];
    self.kmField.text = [NSString stringWithFormat:@"%0.2f", distance / 100.0];
}

- (void)activityTrackerGetCurrentActivityInformationResponse
{
    NSLog(@"activityTrackerGetCurrentActivityInformationResponse");
}

@end
