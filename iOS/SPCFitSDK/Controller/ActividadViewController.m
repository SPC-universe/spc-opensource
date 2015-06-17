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
    self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activePeripheral delegate:self];
}

#pragma mark ActivityTrackerDelegate

- (void)activityTrackerReady
{
    NSLog(@"activityTrackerReady");

    [self.activityTracker getTotalActivityData:0];
    //[self.activityTracker getCurrentActivityInformation];
}

- (void)activityTrackerGetTotalActivityDataResponseDay:(Byte)day
                                                  date:(NSDate *)date
                                                 steps:(int)steps
                                          aerobicSteps:(int)aerobicSteps
                                                   cal:(int)cal
{
    NSLog(@"activityTrackerGetTotalActivityDataResponse 1");

    self.stepsField.text = [NSString stringWithFormat:@"%i", steps];
    self.caloriesField.text = [NSString stringWithFormat:@"%0.2f", cal / 100.0];
}

- (void)activityTrackerGetTotalActivityDataResponseDay:(Byte)day
                                                  date:(NSDate *)date
                                                    km:(int)km
                                          activityTime:(int)activityTime
{
    NSLog(@"activityTrackerGetTotalActivityDataResponse 2");

    self.kmField.text = [NSString stringWithFormat:@"%0.2f", km / 100.0];
}

- (void)activityTrackerRealTimeModeResponseSteps:(int)steps
                                    aerobicSteps:(int)aerobicSteps
                                             cal:(int)cal
                                              km:(int)km
                                    activityTime:(int)activityTime
{
    NSLog(@"activityTrackerRealTimeModeResponse");

    self.stepsField.text = [NSString stringWithFormat:@"%i", steps];
    self.caloriesField.text = [NSString stringWithFormat:@"%0.2f", cal / 100.0];
    self.kmField.text = [NSString stringWithFormat:@"%0.2f", km / 100.0];
}

- (void)activityTrackerGetCurrentActivityInformationResponse
{
    NSLog(@"activityTrackerGetCurrentActivityInformationResponse");
}

@end
