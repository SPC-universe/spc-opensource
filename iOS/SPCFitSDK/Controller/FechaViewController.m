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
    self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activityTracker.peripheral delegate:self];
    [self.activityTracker discoverServices];
}

#pragma mark Actions

- (IBAction)getTime:(UIButton *)sender {
    [self.activityTracker getTime];
}

- (IBAction)setTime:(UIButton *)sender {
    [self.activityTracker setTime:[NSDate dateWithTimeIntervalSinceNow:0.0]];
}

#pragma mark ActivityTrackerDelegate

- (void)activityTrackerReady:(ActivityTracker *)activityTracker
{
    NSLog(@"activityTrackerReady");
    
    self.activityTracker = activityTracker;
    
    // Antes de trabajar con la pulsera hay que enviarle una clave de 6 caracteres
    // La pulsera nos responde con el método activityTrackerSafeBondingSendPasswordResponse
    [self.activityTracker safeBondingSendPassword:@"123456"];
}

- (void)activityTrackerSafeBondingSavePasswordResponse
{
    NSLog(@"safeBondingSavePasswordResponse");
    
    // Se ha guardado la clave en el dispositivo
    //[self.activityTracker safeBondingStatus];
}

- (void)activityTrackerSafeBondingStatusResponse:(BOOL)error
{
    NSLog(@"safeBondingStatusResponse error? %@", error ? @YES : @NO);
    
    if (!error) {
        // Si no hay error podemos continuar con las llamadas
        [self.activityTracker getTime];
    } else {
        // Si hay error hay que guardar una clave nueva
        [self.activityTracker safeBondingSendPassword:@"123456"];
    }
}

- (void)activityTrackerSafeBondingSendPasswordResponse:(BOOL)error
{
    NSLog(@"safeBondingSendPasswordResponse error? %@", error ? @YES : @NO);
    
    if (!error) {
        // Si no hay error podemos continuar con las llamadas
        [self.activityTracker getTime];
    } else {
        // Si hay error hay que guardar una clave nueva
        [self.activityTracker safeBondingSavePassword:@"123456"];
    }
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
