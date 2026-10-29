#import "InstantpayRdService.h"
#import <UIKit/UIKit.h>
#import <objc/runtime.h>


static NSString * const SUCCESS = @"SUCCESS";
static NSString * const FAILED = @"FAILED";

@implementation InstantpayRdService {
    RCTPromiseResolveBlock _responsePromise;
    RCTPromiseRejectBlock _responseReject;
}

//RCT_EXPORT_MODULE();

// 2. Global Persistent Singleton to retain resolution references across app switches
+ (instancetype)sharedInstance {
    static InstantpayRdService *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

// 3. Automatically run when the library binary loads into memory
+ (void)load {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        [self autoSwizzleAppScenes];
    });
}

#pragma mark - Zero-Config Runtime Method Swizzling

+ (void)autoSwizzleAppScenes {
    // Dynamically look up the target project's SceneDelegate class blueprint runtime mapping
    Class sceneDelegateClass = NSClassFromString(@"SceneDelegate");
    if (!sceneDelegateClass) {
        // Fallback for namespaced bundle schemas
        NSString *appName = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleName"];
        NSString *swiftSceneClass = [NSString stringWithFormat:@"%@.SceneDelegate", appName];
        sceneDelegateClass = NSClassFromString(swiftSceneClass);
    }
    
    if (!sceneDelegateClass) return; // Exit if the layout context doesn't exist

    SEL originalSelector = @selector(scene:openURLContexts:);
    SEL swizzledSelector = @selector(swizzled_scene:openURLContexts:);

    Method originalMethod = class_getInstanceMethod(sceneDelegateClass, originalSelector);
    Method swizzledMethod = class_getInstanceMethod([self class], swizzledSelector);

    if (!swizzledMethod) return;

    // Dynamically patch our custom interceptor straight into the host app's SceneDelegate map
    BOOL didAddMethod = class_addMethod(sceneDelegateClass,
                                        originalSelector,
                                        method_getImplementation(swizzledMethod),
                                        method_getTypeEncoding(swizzledMethod));

    if (!didAddMethod && originalMethod) {
        method_exchangeImplementations(originalMethod, swizzledMethod);
    }
}

// Injected method context executing inside the active application SceneDelegate channel
- (void)swizzled_scene:(UIScene *)scene openURLContexts:(NSSet<UIOpenURLContext *> *)URLContexts {
    UIOpenURLContext *context = [URLContexts anyObject];
    if (context && context.URL) {
        // Call the centralized extractor loop attached to our active TurboModule singleton
        [[InstantpayRdService sharedInstance] extractPayloadAndResolvePromise:context.URL];
    }
    
    // Forward execution back down to the user's original scene tracking routines (keeps standard linking alive)
    if ([self respondsToSelector:@selector(swizzled_scene:openURLContexts:)]) {
        [self swizzled_scene:scene openURLContexts:URLContexts];
    }
}

#pragma mark - Data Extraction Loop

- (void)extractPayloadAndResolvePromise:(NSURL *)url {
    //NSLog(@"[RdService TurboModule] Intercepted deep link successfully: %@", url.absoluteString);
    
    // Narrow safety checking: Ensure this link belongs to our specific transaction stream
    if ([url.scheme isEqualToString:@"ipayrdservice"] && [url.host isEqualToString:@"in.gov.uidai.pehchaan"]) {
        
        NSURLComponents *components = [NSURLComponents componentsWithURL:url resolvingAgainstBaseURL:NO];
        //NSString *statusValue = nil;
        NSString *responseToken = nil;
        
        for (NSURLQueryItem *item in components.queryItems) {
            if ([item.name isEqualToString:@"status"]) {
                //statusValue = item.value;
            } else if ([item.name isEqualToString:@"response"]) {
                responseToken = item.value;
            }
        }
        
        if ([InstantpayRdService sharedInstance]->_responsePromise == nil) {
            [[InstantpayRdService sharedInstance] resolveResultWithErrorMessage:@"Something Went Wrong, Please try again #IRSPRP1"];
            return; // Exit early since no JavaScript context is waiting for this response
        }
        
        responseToken = (responseToken != nil) ? responseToken : @"";
        
        // Retrieve and execute the captured promise hooks bound onto our persistent singleton anchor
        RCTPromiseResolveBlock activeResolve = [InstantpayRdService sharedInstance]->_responsePromise;
        if (activeResolve) {
            
            NSMutableDictionary *resultData = [NSMutableDictionary dictionaryWithDictionary:@{
                @"status": @"SUCCESS",
                @"response": responseToken,
                @"timestamp": @"",
            }];
            
            // Convert JSON to String
            NSData *jsonData = [NSJSONSerialization dataWithJSONObject:resultData options:0 error:nil];
            NSString *resultString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
            
            [[InstantpayRdService sharedInstance] resolveResult:@"Successfully Captured the data" status:SUCCESS data:resultString actCode:@"" exceptionMessage:@""];
            
            // Instantly purge references to guarantee memory release safety
            [InstantpayRdService sharedInstance]->_responsePromise = nil;
            [InstantpayRdService sharedInstance]->_responseReject = nil;
        }
    }
}


- (void)getFingerPrint:(nonnull NSString *)deviceName pidOption:(nonnull NSString *)pidOption resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject { 
    _responsePromise = resolve;
    [self resolveResultWithErrorMessage:@"Not Supported in iOS"];
}

- (void)openEyeAuth:(nonnull NSString *)deviceName pidOption:(nonnull NSString *)pidOption resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject { 
    _responsePromise = resolve;
    [self resolveResultWithErrorMessage:@"Not Supported in iOS"];
}

- (void)openFaceAuth:(nonnull NSString *)pidOption resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject { 
    _responsePromise = resolve;
    [self resolveResultWithErrorMessage:@"Not Supported in iOS"];
}

- (void)openAadhaarAuth:(nonnull NSString *)token resolve:(nonnull RCTPromiseResolveBlock)resolve reject:(nonnull RCTPromiseRejectBlock)reject {
    _responsePromise = resolve;
    [InstantpayRdService sharedInstance]->_responsePromise = resolve;
    [InstantpayRdService sharedInstance]->_responseReject = reject;
    
    //[self logPrint:[NSString stringWithFormat:@"Token : %@", token]];
    
    // 1. Construct the dynamic URL string using formatting
    NSString *urlString = [NSString stringWithFormat:@"pehchaan://in.gov.uidai.pehchaan?request=%@&callback=ipayrdservice", token];

    // 2. Safely create the NSURL object
    NSURL *appURL = [NSURL URLWithString:urlString];

    // 3. Perform the asynchronous redirection on the main thread
    /*dispatch_async(dispatch_get_main_queue(), ^{
        [[UIApplication sharedApplication] openURL:appURL
                                           options:@{}
                                 completionHandler:^(BOOL success) {
            if (success) {
                NSLog(@"Successfully redirected to Pehchan app");
            } else {
                [self resolveResultWithErrorMessage:@"Aadhaar Auth services not available, Please install Aadhaar application from App Store."];
            }
        }];
    });*/
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([[UIApplication sharedApplication] canOpenURL:appURL]) {
            [[UIApplication sharedApplication] openURL:appURL options:@{} completionHandler:nil];
        } else {
            // Safe clean rejection if the app is missing
            [self resolveResultWithErrorMessage:@"Aadhaar Auth services not available, Please install Aadhaar application from App Store."];
        }
    });
}



- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeInstantpayRdServiceSpecJSI>(params);
}

+ (NSString *)moduleName
{
  return @"InstantpayRdService";
}

- (void)resolveResultWithErrorMessage:(NSString *)message {
    // Forwards the call to the main method using nil for optional fields
    [self resolveResult:message status:nil data:nil actCode:nil exceptionMessage:nil];
}

- (void)resolveResult:(nonnull NSString *)message status:(nullable NSString *)status data:(nullable NSString *)data actCode:(nullable NSString *)actCode exceptionMessage:(nullable NSString *)exceptionMessage {
    status = (status != nil) ? status : FAILED;
    data = (data != nil) ? data : @"";
    actCode = (actCode != nil) ? actCode : @"";
    exceptionMessage = (exceptionMessage != nil) ? exceptionMessage : @"";
    
    if(_responsePromise == nil){
        return;
    }
    
    NSMutableDictionary *result = [NSMutableDictionary dictionaryWithDictionary:@{
        @"status": status,
        @"message": message,
        @"data": data,
        @"actCode": actCode
    }];
    
    if (exceptionMessage != nil && exceptionMessage.length > 0) {
        result[@"exceptionMessage"] = exceptionMessage;
    }
    
    _responsePromise(result);
    _responsePromise = nil;
}

- (void)logPrint:(nonnull NSString *)value {
    NSLog(@"InstantpayRdService* %@", value);
}

@end



