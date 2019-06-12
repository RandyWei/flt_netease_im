import 'package:flutter/services.dart';
import 'src/config.dart';
export 'src/config.dart';

class FltNeteaseIm {
  static const MethodChannel _channel = const MethodChannel('bughub.dev/flt_netease_im');

  static void initSDK(SDKOptions options) async {
    await _channel.invokeMethod("initSDK", options.toJson());
  }
}
