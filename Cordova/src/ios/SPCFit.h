#import <Cordova/CDV.h>

@interface SPCFit : CDVPlugin

- (void)findDevices:(CDVInvokedUrlCommand *)command;
- (void)setPassword:(CDVInvokedUrlCommand *)command;
- (void)connect:(CDVInvokedUrlCommand *)command;
- (void)setTime:(CDVInvokedUrlCommand *)command;
- (void)getTime:(CDVInvokedUrlCommand *)command;
- (void)setPersonalInformation:(CDVInvokedUrlCommand *)command;
- (void)getPersonalInformation:(CDVInvokedUrlCommand *)command;
- (void)setTargetSteps:(CDVInvokedUrlCommand *)command;
- (void)getTargetSteps:(CDVInvokedUrlCommand *)command;
- (void)getCurrentActivityInformation:(CDVInvokedUrlCommand *)command;
- (void)getTotalActivityData:(CDVInvokedUrlCommand *)command;
- (void)getDetailActivityData:(CDVInvokedUrlCommand *)command;
- (void)startRealTimeMeterMode:(CDVInvokedUrlCommand *)command;
- (void)stopRealTimeMeterMode:(CDVInvokedUrlCommand *)command;

- (void)getSleepMonitorMode:(CDVInvokedUrlCommand *)command;
- (void)switchSleepMonitorMode:(CDVInvokedUrlCommand *)command;
- (void)startECGMode:(CDVInvokedUrlCommand *)command;
- (void)stopECGMode:(CDVInvokedUrlCommand *)command;
- (void)getECGData:(CDVInvokedUrlCommand *)command;

@end
