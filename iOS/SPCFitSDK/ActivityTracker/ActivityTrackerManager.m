#import "ActivityTrackerManager.h"

@interface ActivityTrackerManager ()

@property (strong, nonatomic) NSTimer *findPeripheralsTimeoutTimer;
@property (strong, nonatomic) NSTimer *findPeripheralsRetryTimer;

@end


@implementation ActivityTrackerManager

#pragma mark Lifecycle

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
        _activityTrackers= [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (void)reinit
{
    [self.findPeripheralsTimeoutTimer invalidate];
    self.findPeripheralsTimeoutTimer = nil;

    [self.findPeripheralsRetryTimer invalidate];
    self.findPeripheralsRetryTimer = nil;

    [self.centralManager stopScan];
    self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];

    for (NSString *deviceId in self.peripherals) {
        CBPeripheral *peripheral = self.peripherals[deviceId];
        if (peripheral.state != CBPeripheralStateDisconnected) {
            [self.centralManager cancelPeripheralConnection:peripheral];
        }
    }
    self.peripherals = [[NSMutableDictionary alloc] init];

    for (NSString *deviceId in self.activityTrackers) {
        CBPeripheral *activityTracker = self.peripherals[deviceId];
        activityTracker.delegate = nil;
    }
    self.activityTracker = nil;
    self.activityTrackers= [[NSMutableDictionary alloc] init];
}

#pragma mark Notificaciones

- (void)postActivityTrackerManagerStateUpdatedNotification:(NSString *)state
{
    NSLog(@"postActivityTrackerManagerStateUpdatedNotification: %@", state);
    
    NSDictionary *userInfo = @{ @"state": state };
    [[NSNotificationCenter defaultCenter] postNotificationName:@"ActivityTrackerManagerStateUpdatedNotification" object:self userInfo:userInfo];
}

- (void)postActivityTrackerFoundNotification:(NSString *)deviceId
{
    NSLog(@"postActivityTrackerFoundNotification: %@", deviceId);
    
    NSDictionary *postInfo = @{ @"deviceId": deviceId };
    [[NSNotificationCenter defaultCenter] postNotificationName:@"ActivityTrackerFoundNotification" object:self userInfo:postInfo];
}

- (void)postActivityTrackerConnectedNotification:(NSString *)deviceId
{
    NSLog(@"postActivityTrackerConnectedNotification: %@", deviceId);
    
    if (deviceId) {
        NSDictionary *postInfo = @{ @"deviceId": deviceId };
        [[NSNotificationCenter defaultCenter] postNotificationName:@"ActivityTrackerConnectedNotification" object:self userInfo:postInfo];
    }
}

- (void)postActivityTrackerDisconnectedNotification:(NSString *)deviceId
{
    NSLog(@"postActivityTrackerDisconnectedNotification: %@", deviceId);
    
    if (deviceId) {
        NSDictionary *postInfo = @{ @"deviceId": deviceId };
        [[NSNotificationCenter defaultCenter] postNotificationName:@"ActivityTrackerDisconnectedNotification" object:self userInfo:postInfo];
    }
}

- (void)postActivityTrackerTimeoutNotification
{
    // ?
}

#pragma mark Peripherals

- (void)findPeripherals:(NSTimeInterval)timeInterval
{
    NSLog(@"findPeripherals");
    [self logCentralManagerState];
    
    if (self.findPeripheralsTimeoutTimer) {
        [self.findPeripheralsTimeoutTimer invalidate];
        self.findPeripheralsTimeoutTimer = nil;
        [self.centralManager stopScan];
    }

    if (self.findPeripheralsRetryTimer) {
        [self.findPeripheralsRetryTimer invalidate];
        self.findPeripheralsRetryTimer = nil;
    }

    if (self.centralManager.state == CBCentralManagerStatePoweredOn) {
        [self.centralManager retrieveConnectedPeripheralsWithServices:@[[CBUUID UUIDWithString:@"FFF0"]]];

        self.findPeripheralsTimeoutTimer =
        [NSTimer scheduledTimerWithTimeInterval:timeInterval
                                         target:self
                                       selector:@selector(findPeripheralsTimeout)
                                       userInfo:nil
                                        repeats:NO];
        
        [self.centralManager scanForPeripheralsWithServices:@[[CBUUID UUIDWithString:@"FFF0"]] options:nil];
    }
}

- (void)findPeripheralsTimeout {
    NSLog(@"findPeripheralsTimeout");
    
    [self.findPeripheralsTimeoutTimer invalidate];
    self.findPeripheralsTimeoutTimer = nil;

    [self.centralManager stopScan];

    self.findPeripheralsRetryTimer =
    [NSTimer scheduledTimerWithTimeInterval:60.0
                                     target:self
                                   selector:@selector(findPeripheralsRetry)
                                   userInfo:nil
                                    repeats:NO];
}

- (void)findPeripheralsRetry
{
    [self findPeripherals:10.0];
}

- (NSString *)deviceIdAtIndex:(NSUInteger)index;
{
    NSString *deviceId = [[self.peripherals allKeys] objectAtIndex:index];
    return deviceId;
}

- (CBPeripheral *)peripheralAtIndex:(NSUInteger)index
{
    NSString *deviceId = [[self.peripherals allKeys] objectAtIndex:index];
    return self.peripherals[deviceId];
}

- (void)connectTo:(NSString *)deviceId delegate:(id<ActivityTrackerDelegate>)delegate
{
    NSLog(@"connectTo: %@ delegate: %@", deviceId, delegate);
    for (NSString *peripheralDeviceId in self.peripherals) {
        CBPeripheral *peripheral = self.peripherals[peripheralDeviceId];
        NSLog(@"*** peripheral %@ %@ %@", peripheral.name, peripheralDeviceId, deviceId);
        if ([peripheralDeviceId isEqualToString:deviceId]) {
            ActivityTracker *activityTracker = [[ActivityTracker alloc] initWithPeripheral:peripheral delegate:delegate];
            self.activityTrackers[deviceId] = activityTracker;
            [self.centralManager connectPeripheral:peripheral options:nil];
        }
    }
}

- (void)disconnectFrom:(NSString *)deviceId
{
    NSLog(@"disconnectFrom: %@", deviceId);
    
    CBPeripheral *peripheral = self.peripherals[deviceId];
    if (peripheral) {
        [self.centralManager cancelPeripheralConnection:peripheral];
    }
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

#pragma mark CBCentralManagerDelegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central
{
    [self logCentralManagerState];
    
    [self postActivityTrackerManagerStateUpdatedNotification:[self centralManagerStateName]];
    
    /*if (central.state == CBCentralManagerStatePoweredOn) {
     [self findPeripherals:10.0];
     }*/
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral
     advertisementData:(NSDictionary *)advertisementData
                  RSSI:(NSNumber *)RSSI
{
    NSLog(@"centralManagerDidDiscoverPeripheral %@ %@ %@", peripheral.name, advertisementData, peripheral.identifier.UUIDString);
    
    NSString *deviceId = advertisementData[@"kCBAdvDataLocalName"];
    
    // Tmp fix
    // || [deviceId isEqualToString:@""]
    if ([deviceId isEqualToString:@"SPC FIT PULSE"]) {
        deviceId = peripheral.identifier.UUIDString;
    }

    if (deviceId) {
        self.peripherals[deviceId] = peripheral;
        [self postActivityTrackerFoundNotification:deviceId];
    }
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    NSLog(@"centralManager:didConnectPeripheral: %@ %@", peripheral.name, peripheral.identifier.UUIDString);
    
    NSString *deviceId = [[self.peripherals allKeysForObject:peripheral] firstObject];
    if (deviceId) {
        self.activityTracker = self.activityTrackers[deviceId];
        
        [self postActivityTrackerConnectedNotification:deviceId];
    }
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error
{
    NSLog(@"centralManager:didDisconnectPeripheral: %@ %@", peripheral.name, peripheral.identifier.UUIDString);
    
    NSString *deviceId = [[self.peripherals allKeysForObject:peripheral] firstObject];
    self.activityTracker = nil;
    
    if (deviceId) {
        [self postActivityTrackerDisconnectedNotification:deviceId];
    }
}

- (void)centralManager:(CBCentralManager *)central didRetrievePeripherals:(NSArray *)peripherals
{
    for (CBPeripheral *peripheral in peripherals) {
        NSLog(@"centralManager:didRetrievePeripherals: %@ %@", peripheral.name, peripheral.identifier.UUIDString);
    }
}

- (void)centralManager:(CBCentralManager *)central didRetrieveConnectedPeripherals:(NSArray *)peripherals
{
    for (CBPeripheral *peripheral in peripherals) {
        NSLog(@"centralManager:didRetrieveConnectedPeripherals: %@ %@", peripheral.name, peripheral.identifier.UUIDString);
        
        NSString *deviceId = peripheral.identifier.UUIDString;
        if (deviceId) {
            self.peripherals[deviceId] = peripheral;
            [self postActivityTrackerFoundNotification:deviceId];
        }
    }
}

@end
