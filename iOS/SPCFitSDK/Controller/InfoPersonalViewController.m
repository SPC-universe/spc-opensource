#import "InfoPersonalViewController.h"
#import "ActivityTrackerManager.h"

@interface InfoPersonalViewController () <ActivityTrackerDelegate, UITextFieldDelegate>

@property (strong, nonatomic) ActivityTrackerManager *activityTrackerManager;
@property (strong, nonatomic) ActivityTracker *activityTracker;

@property (weak, nonatomic) IBOutlet UISegmentedControl *genderControl;
@property (weak, nonatomic) IBOutlet UITextField *ageField;
@property (weak, nonatomic) IBOutlet UITextField *heightField;
@property (weak, nonatomic) IBOutlet UITextField *weightField;
@property (weak, nonatomic) IBOutlet UITextField *stepLengthField;
@property (weak, nonatomic) IBOutlet UITextField *goalField;

@end

@implementation InfoPersonalViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    self.activityTrackerManager = [ActivityTrackerManager sharedInstance];
    self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activityTracker.peripheral delegate:self];
    [self.activityTracker discoverServices];
}

#pragma mark Actions

- (IBAction)getPersonalInformation:(UIButton *)sender {
    [self.activityTracker getPersonalInformation];
}

- (IBAction)setPersonalInformation:(UIButton *)sender {
    [self.activityTracker setPersonalInformationMale:self.genderControl.selectedSegmentIndex
                                                 age:[self.ageField.text intValue]
                                              height:[self.heightField.text intValue]
                                              weight:[self.weightField.text intValue]
                                              stride:[self.stepLengthField.text intValue]];
}

#pragma mark ActivityTrackerDelegate

- (void)activityTrackerReady:(ActivityTracker *)activityTracker
{
    NSLog(@"activityTrackerReady: %@", activityTracker);

    [self.activityTracker getPersonalInformation];
}

- (void)activityTrackerGetPersonalInformationResponseMan:(BOOL)man
                                                     age:(Byte)age
                                                  height:(Byte)height
                                                  weight:(Byte)weight
                                              stepLength:(Byte)stepLength
                                                deviceId:(NSString *)deviceId
{
    NSLog(@"getPersonalInformationResponse");

    self.genderControl.selectedSegmentIndex = man;
    self.ageField.text = [NSString stringWithFormat:@"%i", age];
    self.heightField.text = [NSString stringWithFormat:@"%i", height];
    self.weightField.text = [NSString stringWithFormat:@"%i", weight];
    self.stepLengthField.text = [NSString stringWithFormat:@"%i", stepLength];

    [self.activityTracker getTargetSteps];
}

- (void)activityTrackerGetTargetStepsResponse:(int)goal
{
    NSLog(@"activityTrackerGetTargetStepsResponse");

    self.goalField.text = [NSString stringWithFormat:@"%i", goal];
}

- (void)activityTrackerSetPersonalInformationResponse
{
    NSLog(@"setPersonalInformationResponse");

    [self.activityTracker setTargetSteps:[self.goalField.text intValue]];
}

#pragma mark UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];

    return YES;
}

@end
