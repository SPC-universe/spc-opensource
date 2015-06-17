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
    self.activityTracker = [[ActivityTracker alloc] initWithPeripheral:self.activityTrackerManager.activePeripheral delegate:self];

    self.activityData = [[NSMutableArray alloc] init];
    self.sleepData = [[NSMutableArray alloc] init];
}

#pragma mark ActivityTrackerDelegate

- (void)activityTrackerReady
{
    NSLog(@"activityTrackerReady");

    [self.activityTracker getDetailActivityData:0];
}

- (void)activityTrackerGetDetailActivityDataDayResponseIndex:(int)index
                                                        date:(NSDate *)date
                                                       steps:(int)steps
                                                aerobicSteps:(int)aerobicSteps
                                                         cal:(int)cal
                                                          km:(int)km
{
    [self.activityData addObject:@{
        @"date": date,
        @"steps": @(steps),
        @"cal": @(cal / 100.0),
        @"km": @(km / 100.0)
    }];

    [self.tableView reloadData];
}

-(void)activityTrackerGetDetailActivityDataSleepResponseIndex:(int)index
                                                         date:(NSDate *)date
                                                 sleepQuality:(int)sleepQuality
{
    [self.sleepData addObject:@{
        @"date": date,
        @"quality": @(sleepQuality)
    }];

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
