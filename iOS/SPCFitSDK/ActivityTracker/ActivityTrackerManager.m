#import "ActivityTrackerManager.h"

@implementation ActivityTrackerManager

+ (ActivityTrackerManager *)sharedInstance
{
    static ActivityTrackerManager *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[ActivityTrackerManager alloc] init];
    });
    return sharedInstance;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        _centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
        _peripherals = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (void)findPeripherals:(NSTimeInterval)timeInterval
{
    NSLog(@"findPeripherals");
    [self logCentralManagerState];

    if (self.centralManager.state == CBCentralManagerStatePoweredOn) {
        [NSTimer scheduledTimerWithTimeInterval:timeInterval
                                         target:self
                                       selector:@selector(findPeripheralsTimeout)
                                       userInfo:nil
                                        repeats:NO];

        [self.centralManager scanForPeripheralsWithServices:nil options:nil];
    }
}

- (void)findPeripheralsTimeout {
    NSLog(@"findPeripheralsTimeout");

    [self.centralManager stopScan];
}

- (CBPeripheral *)peripheralAtIndex:(NSUInteger)index
{
    NSUUID *uuid = [[self.peripherals allKeys] objectAtIndex:index];
    return self.peripherals[uuid];
}

- (void)connectTo:(CBPeripheral *)peripheral
{
    [self.centralManager connectPeripheral:peripheral options:nil];
}

- (void)logCentralManagerState
{
    NSLog(@"CBCentralManagerState = %@", [self centralManagerStateName]);
}

- (NSString *)centralManagerStateName
{
    switch(self.centralManager.state) {
        case CBCentralManagerStatePoweredOn:
            return @"CBCentralManagerStatePoweredOn";
        case CBCentralManagerStatePoweredOff:
            return @"CBCentralManagerStatePoweredOff";
        case CBCentralManagerStateResetting:
            return @"CBCentralManagerStateResetting";
        case CBCentralManagerStateUnauthorized:
            return @"CBCentralManagerStateUnauthorized";
        case CBCentralManagerStateUnsupported:
            return @"CBCentralManagerStateUnsupported";
        case CBCentralManagerStateUnknown:
        default:
            return @"CBCentralManagerStateUnknown";
    }
}

#pragma mark Notificaciones

- (void)postUpdateNotification:(CBPeripheral *)peripheral
{
    NSLog(@"postUpdateNotification");

    NSDictionary *postInfo = @{ @"peripheral": peripheral };
    [[NSNotificationCenter defaultCenter] postNotificationName:@"ActivityTrackerManagerUpdateNotification" object:self userInfo:postInfo];
}

- (void)postConnectedNotification:(CBPeripheral *)peripheral
{
    NSLog(@"postConnectNotification");

    NSDictionary *postInfo = @{ @"peripheral": peripheral };
    [[NSNotificationCenter defaultCenter] postNotificationName:@"ActivityTrackerManagerConnectedNotification" object:self userInfo:postInfo];
}

#pragma mark CBCentralManagerDelegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central
{
    NSLog(@"centralManagerDidUpdateState");
    [self logCentralManagerState];

    if (central.state == CBCentralManagerStatePoweredOn) {
        [self findPeripherals:5.0];
    }
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral
     advertisementData:(NSDictionary *)advertisementData
                  RSSI:(NSNumber *)RSSI
{
    NSLog(@"centralManagerDidDiscoverPeripheral %@ %@", peripheral.name, peripheral.identifier.UUIDString);

    if ([peripheral.name isEqualToString:@"Activity Tracker"]) {
        self.peripherals[peripheral.identifier] = peripheral;
    }

    [self postUpdateNotification:peripheral];
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    NSLog(@"centralManagerDidConnectPeripheral %@ %@", peripheral.name, peripheral.identifier.UUIDString);

    self.activePeripheral = peripheral;

    [self postConnectedNotification:peripheral];
}

@end
