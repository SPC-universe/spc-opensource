#import "SleepQualityDetailData.h"

@implementation SleepQualityDetailData

- (NSString *)description
{
    return [NSString stringWithFormat:@"%@ %i", self.date, self.quality];
}

@end
