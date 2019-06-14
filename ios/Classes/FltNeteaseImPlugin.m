#import "FltNeteaseImPlugin.h"
#import <NIMSDK/NIMSDK.h>

@implementation FltNeteaseImPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"bughub.dev/flt_netease_im"
                                     binaryMessenger:[registrar messenger]];
    FltNeteaseImPlugin* instance = [[FltNeteaseImPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
}
    
- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"initSDK" isEqualToString:call.method]) {
        
        NSDictionary *dict = call.arguments;
        
        NSDictionary *optionDict = dict[@"options"];
        NSString *appKey = optionDict[@"appKey"];
        if (appKey==nil||[appKey isEqual:[NSNull null]]||[appKey isEqualToString:@""]) {
            result([FlutterError errorWithCode:@"ERROR"
                                       message:@"appKey is null"
                                       details:nil]);
            return;
        }
        NIMSDKOption *option = [NIMSDKOption optionWithAppKey:appKey];
        
        id pushConfigDict = optionDict[@"mixPushConfig"];
        //CerName 为开发者为推送证书在云信管理后台定义的名字，在使用中，云信服务器会寻找同名推送证书发起苹果推送服务。
        //目前 CerName 可传 APNs 证书 和 Voip 证书两种，分别对应了参数中 apnsCername 和 pkCername 两个字段。
        if (pushConfigDict!=nil&&[pushConfigDict isEqual:[NSNull null]]&&[pushConfigDict isKindOfClass:[NSDictionary class]]) {
            option.apnsCername = pushConfigDict[@"apnsCername"];
            option.pkCername = pushConfigDict[@"pkCername"];
        }
        
        [[NIMSDK sharedSDK] registerWithOption: option];
        
        //为了更好的应用体验，SDK 需要对应用数据做一些本地持久，比如消息，用户信息等等。在默认情况下，所有数据将放置于 $Document/NIMSDK 目录下。
        //设置该值后 SDK 产生的数据(包括聊天记录，但不包括临时文件)都将放置在这个目录下
        
        [self getValue:optionDict key:@"sdkStorageRootPath" :^(id result) {
            [[NIMSDKConfig sharedConfig] setupSDKDir:result];
        }];
        
        //是否在收到消息后自动下载附件
        BOOL preloadAttach = [optionDict[@"preloadAttach"] boolValue];
        [NIMSDKConfig sharedConfig].fetchAttachmentAutomaticallyAfterReceiving = preloadAttach;
        [NIMSDKConfig sharedConfig].fetchAttachmentAutomaticallyAfterReceivingInChatroom = preloadAttach;
        
        //是否需要将被撤回的消息计入未读计算考虑
        [self getValue:optionDict key:@"shouldConsiderRevokedMessageUnreadCount" :^(id result) {
            [NIMSDKConfig sharedConfig].shouldConsiderRevokedMessageUnreadCount = [result boolValue];
        }];
        
        //是否需要多端同步未读数
        BOOL shouldSyncUnreadCount = [optionDict[@"sessionReadAck"] boolValue];
        [NIMSDKConfig sharedConfig].shouldSyncUnreadCount = shouldSyncUnreadCount;
        
        //是否将群通知计入未读
        BOOL shouldCountTeamNotification = [optionDict[@"teamNotificationMessageMarkUnread"] boolValue];
        [NIMSDKConfig sharedConfig].shouldCountTeamNotification = shouldCountTeamNotification;
        
        //是否支持动图缩略
        BOOL animatedImageThumbnailEnabled = [optionDict[@"animatedImageThumbnailEnabled"] boolValue];
        [NIMSDKConfig sharedConfig].animatedImageThumbnailEnabled = animatedImageThumbnailEnabled;
        
        //客户端自定义信息，用于多端登录时同步该信息
        [self getValue:optionDict key:@"customTag" :^(id result) {
            [NIMSDKConfig sharedConfig].customTag = result;
        }];
        
        result(nil);
    }else if([@"login" isEqualToString: call.method]){
        NSDictionary *args = call.arguments;
        NSLog(@"%@",args);
        NSString *account = args[@"account"];
        NSString *token = args[@"token"];
        [[[NIMSDK sharedSDK] loginManager]login:account token:token completion:^(NSError * _Nullable error) {
            NSLog(@"%@",error);
        }];

        
    } else {
        result(FlutterMethodNotImplemented);
    }
}
    
-(void)getValue:(NSDictionary*)dict key:(NSString*) key :(void(^)(id result))block{
    id value = dict[key];
    if (value==nil||[value isEqual:[NSNull null]]||[value isEqualToString:@""]) {
        return;
    }
    block(value);
}
    
    
    
    @end
