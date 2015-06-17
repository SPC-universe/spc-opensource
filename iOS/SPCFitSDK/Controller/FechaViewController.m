#import "FechaViewController.h"
#import "ActivityTrackerManager.h"

@interface FechaViewController () <ActivityTrackerDelegate>

@property (strong, nonatomic) ActivityTrackerManager *activityTrackerManager;
@property (strong, nonatomic) ActivityTracker *activityTracker;

@property (weak, nonatomic) IBOutlet UITextField *timeField;

@end

@implementation FechaViewController

#pragma mark Init

- (void)viewDidLoad {
    [super viewDidLoad];

    self.activityTrackerManager = [ActivityTrackerManager sharedInstance];
    self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activePeripheral delegate:self];
}

#pragma mark Actions

- (IBAction)getTime:(UIButton *)sender {
    [self.activityTracker getTime];
}

- (IBAction)setTime:(UIButton *)sender {
    [self.activityTracker setTime:[NSDate dateWithTimeIntervalSinceNow:0.0]];
}

#pragma mark ActivityTrackerDelegate

- (void)activityTrackerReady
{
    NSLog(@"activityTrackerReady");

    [self.activityTracker getTime];
}

- (void)activityTrackerGetTimeResponse:(NSDate *)date
{
    NSLog(@"getTimeResponse: %@", date);

    self.timeField.text = [NSString stringWithFormat:@"%@", date];
}

- (void)activityTrackerSetTimeResponse:(BOOL)error
{
    NSLog(@"setTimeResponse: %@", error ? @"YES" : @"NO");
}

@end
