#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "ActivityTracker.h"

@interface ActivityTrackerManager : NSObject <CBCentralManagerDelegate>

@property (strong, nonatomic) CBCentralManager *centralManager;
@property (strong, nonatomic) NSMutableDictionary *peripherals;
@property (strong, nonatomic) CBPeripheral *activePeripheral;

+ (ActivityTrackerManager *)sharedInstance;

- (void)findPeripherals:(NSTimeInterval)timeInterval;
- (CBPeripheral *)peripheralAtIndex:(NSUInteger)index;
- (void)connectTo:(CBPeripheral *)peripheral;

@end
