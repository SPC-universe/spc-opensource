#import "ActividadDetalleViewController.h"
#import "ActivityTrackerManager.h"

@interface ActividadDetalleViewController () <ActivityTrackerDelegate>

@property (strong, nonatomic) ActivityTrackerManager *activityTrackerManager;
@property (strong, nonatomic) ActivityTracker *activityTracker;

@property (strong, nonatomic) NSMutableArray *activityData;
@property (strong, nonatomic) NSMutableArray *sleepData;

@end

@implementation ActividadDetalleViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    self.activityTrackerManager = [ActivityTrackerManager sharedInstance];
    self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activityTracker.peripheral delegate:self];
    [self.activityTracker discoverServices];

    self.activityData = [[NSMutableArray alloc] init];
    self.sleepData = [[NSMutableArray alloc] init];
}

#pragma mark ActivityTrackerDelegate

- (void)activityTrackerReady:(ActivityTracker *)activityTracker
{
    NSLog(@"activityTrackerReady: %@", activityTracker);

    [self.activityTracker getDetailActivityData:0];
}

- (void)activityTrackerGetDetailActivityDataDayResponseIndex:(int)index
                                                        date:(NSDate *)date
                                                       steps:(int)steps
                                                aerobicSteps:(int)aerobicSteps
                                                    calories:(int)calories
                                                    distance:(int)distance
{
    [self.activityData addObject:@{
        @"date": date,
        @"steps": @(steps),
        @"cal": @(calories / 100.0),
        @"km": @(distance / 100.0)
    }];

    [self.tableView reloadData];
}

- (void)activityTrackerGetDetailActivityDataSleepResponseIndex:(int)index
                                                sleepQualities:(NSArray *)sleepQualities
{
    for (NSDictionary *sleepData in sleepQualities) {
        [self.sleepData addObject:@{ @"date": sleepData[@"data"],
                                     @"quality": sleepData[@"quality"] }];
    }

    [self.tableView reloadData];
}

#pragma mark - Table view data source

#define SECTION_DAY 0
#define SECTION_SLEEP 1

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    switch (section) {
        case SECTION_DAY:
            return self.activityData.count;
        case SECTION_SLEEP:
            return self.sleepData.count;
    }
    return 0;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    switch (section) {
        case SECTION_DAY:
            return @"Actividad";
        case SECTION_SLEEP:
            return @"Calidad del sueño";
    }
    return @"";
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Detalle" forIndexPath:indexPath];

    if (indexPath.section == SECTION_DAY) {
        NSDictionary *activityItem = self.activityData[indexPath.row];
        cell.textLabel.text = [NSString stringWithFormat:@"Steps: %@ Cal: %@ Km: %@",
                               activityItem[@"steps"],
                               activityItem[@"cal"],
                               activityItem[@"km"]];
        cell.detailTextLabel.text = [NSString stringWithFormat:@"%@", activityItem[@"date"]];
    }

    if (indexPath.section == SECTION_SLEEP) {
        NSDictionary *sleepItem = self.sleepData[indexPath.row];
        cell.textLabel.text = [NSString stringWithFormat:@"Quality: %@",
                               sleepItem[@"quality"]];
        cell.detailTextLabel.text = [NSString stringWithFormat:@"%@", sleepItem[@"date"]];
    }

    return cell;
}

@end
