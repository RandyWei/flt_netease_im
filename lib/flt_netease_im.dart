import 'dart:async';

import 'package:flutter/services.dart';

class FltNeteaseIm {
  static const MethodChannel _channel =
      const MethodChannel('flt_netease_im');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
