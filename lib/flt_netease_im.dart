import 'dart:async';

import 'package:flutter/services.dart';
import 'src/config.dart';
export 'src/config.dart';

class FltNeteaseIm {
  static StreamSubscription<dynamic> _loginStreamSubscription;

  static const MethodChannel _channel = const MethodChannel('bughub.dev/flt_netease_im');

  static Future<void> initSDK(SDKOptions options) async {
    return await _channel.invokeMethod("initSDK", {"options": options.toJson(), "loginInfo": null});
  }

  static void login(LoginInfo loginInfo, {Function onSuccess, Function onError}) {
    _loginStreamSubscription = EventChannel("bughub.dev/flt_netease_im/events[login]")
        .receiveBroadcastStream()
        .listen((data) {
      print(data);
      if (data["event"] == "LOGIN_SUCCESS") {
        print("登录成功");
        if (onSuccess != null) onSuccess();
      } else {
        print("未知错误");
      }
    }, onError: (detail) {
      print("detail:$detail");
      //if (onError != null) onError(code, message, detail);
    }, onDone: () {
      _loginStreamSubscription.cancel();
    });

    _channel.invokeMethod("login", loginInfo.toJson());
  }
}
