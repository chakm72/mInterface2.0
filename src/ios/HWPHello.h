#import <Cordova/CDV.h>

#import <Foundation/Foundation.h>
@import UIKit;
#import <MapKit/MapKit.h>
#import <CoreLocation/CoreLocation.h>
#import <Cordova/CDVPlugin.h>
#import <UIKit/UIKit.h>

@interface HWPHello : CDVPlugin <CLLocationManagerDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIAlertViewDelegate>
{
    UIImagePickerController *ipc;
    UIPopoverController *popover;
}
- (void)StartService:(CDVInvokedUrlCommand*)command;
- (void)SendLocation;
- (void)GetLocation:(CDVInvokedUrlCommand*)command;
- (void)timeReader;
- (void)CheckSumIndicatorResult;
- (void)CheckLocation:(CDVInvokedUrlCommand*)command;
- (void)DespatchQueueNew;
- (void)CopyFile:(CDVInvokedUrlCommand*)command;
- (void)FileChooser:(CDVInvokedUrlCommand*)command;
- (NSString *)contentTypeForImageData:(NSData *)data;
- (void)UpdateChoice:(CDVInvokedUrlCommand*)command;
- (void)UpdateConfirm:(CDVInvokedUrlCommand*)command;
- (void)RefreshTimeProfile:(CDVInvokedUrlCommand*)command;
- (void)GetNewDate:(CDVInvokedUrlCommand*)command;
-(void)timeValues:(NSString *)date hour:(NSString *)hour minute:(NSString *)minute;
- (void)KillTimers;
- (void)AuthenticationRefresh;
-(void) triggerAuthenticationServices;
- (void)GetSyncIndicator:(CDVInvokedUrlCommand*)command;
@property (strong, nonatomic) CLLocationManager * locationManager;
@property (strong, nonatomic) NSMutableArray * coordinatesArray;
@property (weak, nonatomic) IBOutlet UIButton *btnGallery;
@property (strong, nonatomic) UIViewController * viewcont;
@property (nonatomic, strong) NSString *callbackIdForImagePicker;
@property (nonatomic, strong) NSString *callbackIdForAppUpdate;
@property NSTimer *QueueTimer;
@property NSTimer *LocationTimer;
@property NSTimer *timeReaderTimer;
@property NSTimer *CheckSumTimer;
@property NSTimer *authTimer;
@end
