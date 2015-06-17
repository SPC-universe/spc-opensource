#import "DispositivosViewController.h"
#import "ActivityTrackerManager.h"

@interface DispositivosViewController ()

@property (strong, nonatomic) ActivityTrackerManager *activityTrackerManager;

@end

@implementation DispositivosViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    self.activityTrackerManager = [ActivityTrackerManager sharedInstance];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(activityTrackerManagerUpdateNotification:)
                                                 name:@"ActivityTrackerManagerUpdateNotification"
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(activityTrackerManagerConnectedNotification:)
                                                 name:@"ActivityTrackerManagerConnectedNotification"
                                               object:nil];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)activityTrackerManagerUpdateNotification:(NSDictionary *)postInfo
{
    [self.tableView reloadData];
}

- (void)activityTrackerManagerConnectedNotification:(NSDictionary *)postInfo
{
    [self.tableView reloadData];

    [self performSegueWithIdentifier:@"activityTrackerConnected" sender:self];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.activityTrackerManager.peripherals count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *celda = [tableView dequeueReusableCellWithIdentifier:@"Dispositivo" forIndexPath:indexPath];

    CBPeripheral *peripheral = [self.activityTrackerManager peripheralAtIndex:indexPath.row];

    celda.textLabel.text = peripheral.name;
    celda.detailTextLabel.text = peripheral.identifier.UUIDString;
    if ([peripheral.name isEqualToString:@"Activity Tracker"]) {
        celda.imageView.image = [UIImage imageNamed:@"fitpro"];
    } else {
        celda.imageView.image = [UIImage imageNamed:@"help"];
    }

    if (peripheral.state == CBPeripheralStateConnected) {
        celda.accessoryType = UITableViewCellAccessoryCheckmark;
    } else {
        celda.accessoryType = UITableViewCellAccessoryNone;
    }
    return celda;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    CBPeripheral *peripheral = [self.activityTrackerManager peripheralAtIndex:indexPath.row];
    if (peripheral.state == CBPeripheralStateConnected) {
        [self performSegueWithIdentifier:@"activityTrackerConnected" sender:self];
    } else if (peripheral.state == CBPeripheralStateDisconnected) {
        [self.activityTrackerManager connectTo:peripheral];
    }
}

@end
