#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "ActivityTracker.h"

@interface ActivityTrackerManager : NSObject <CBCentralManagerDelegate>

@property (strong, nonatomic) CBCentralManager *centralManager;
@property (strong, nonatomic) NSMutableDictionary *peripherals;
@property (strong, nonatomic) NSMutableDictionary *peripheralsUUID;

@property (strong, nonatomic) NSMutableDictionary *devices;
@property (strong, nonatomic) NSMutableDictionary *activityTrackers;
@property (strong, nonatomic) ActivityTracker *activityTracker;

+ (ActivityTrackerManager *)sharedInstance;
- (void)reinit;

- (void)findPeripherals:(NSTimeInterval)timeInterval;
- (CBPeripheral *)peripheralAtIndex:(NSUInteger)index;
- (NSString *)deviceIdAtIndex:(NSUInteger)index;

- (void)connectTo:(NSString *)deviceId delegate:(id<ActivityTrackerDelegate>)delegate;
- (void)disconnectFrom:(NSString *)deviceId;

@end
