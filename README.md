# flt_netease_im

基于网易云信的IM插件

## Android

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
       package="xxx">

    <!-- 权限声明 -->
    <!-- 访问网络状态-->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <!-- 控制呼吸灯，振动器等，用于新消息提醒 -->
    <uses-permission android:name="android.permission.FLASHLIGHT" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <!-- 外置存储存取权限 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

     <!-- 8.0 系统需要-->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <!-- 多媒体相关 -->
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>

    <!-- 如果需要实时音视频通话模块，下面的权限也是必须的。否则，可以不加 -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
    <uses-permission android:name="android.permission.BROADCAST_STICKY"/>
     <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    <uses-feature android:glEsVersion="0x00020000" android:required="true" />

    <!-- SDK 权限申明, 第三方 APP 接入时，请将 com.netease.nim.demo 替换为自己的包名 -->
    <!-- 和下面的 uses-permission 一起加入到你的 AndroidManifest 文件中。 -->
    <permission
        android:name="com.netease.nim.demo.permission.RECEIVE_MSG"
        android:protectionLevel="signature"/>
    <!-- 接收 SDK 消息广播权限， 第三方 APP 接入时，请将 com.netease.nim.demo 替换为自己的包名 -->
     <uses-permission android:name="com.netease.nim.demo.permission.RECEIVE_MSG"/>

    <application
            ...>


            <!-- 云信后台服务，请使用独立进程。 -->
            <service
                android:name="com.netease.nimlib.service.NimService"
                android:process=":core"/>

           <!-- 云信后台辅助服务 -->
            <service
                android:name="com.netease.nimlib.service.NimService$Aux"
                android:process=":core"/>

            <!-- 云信后台辅助服务 -->
            <service
                android:name="com.netease.nimlib.job.NIMJobService"
                android:exported="true"
                android:permission="android.permission.BIND_JOB_SERVICE"
                android:process=":core"/>

            <!-- 云信监视系统启动和网络变化的广播接收器，保持和 NimService 同一进程 -->
            <receiver android:name="com.netease.nimlib.service.NimReceiver"
                android:process=":core"
                android:exported="false">
                <intent-filter>
                    <action android:name="android.intent.action.BOOT_COMPLETED"/>
                    <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
                </intent-filter>
            </receiver>

            <!-- 云信进程间通信 Receiver -->
            <receiver android:name="com.netease.nimlib.service.ResponseReceiver"/>

            <!-- 云信进程间通信service -->
            <service android:name="com.netease.nimlib.service.ResponseService"/>

            <!-- 云信进程间通信provider -->
            <!-- android:authorities="{包名}.ipc.provider", 请将com.netease.nim.demo替换为自己的包名 -->
            <provider
                android:name="com.netease.nimlib.ipc.NIMContentProvider"
                android:authorities="com.netease.nim.demo.ipc.provider"
                android:exported="false"
                android:process=":core" />
        </application>
</manifest>
```

### 初始化

SDKOptions参照官方文档:[https://dev.yunxin.163.com/docs/interface/%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AFAndroid%E7%AB%AF/IM_Android/index.html](https://dev.yunxin.163.com/docs/interface/%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AFAndroid%E7%AB%AF/IM_Android/index.html)

暂时不支持以下设置：

messageNotifierCustomization

mNosTokenSceneConfig

notificationSmallIconId

notificationSound


## iOS

### 初始化

配置参照官方文档:[https://dev.yunxin.163.com/docs/product/IM%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AF/SDK%E5%BC%80%E5%8F%91%E9%9B%86%E6%88%90/iOS%E5%BC%80%E5%8F%91%E9%9B%86%E6%88%90/%E5%88%9D%E5%A7%8B%E5%8C%96](https://dev.yunxin.163.com/docs/product/IM%E5%8D%B3%E6%97%B6%E9%80%9A%E8%AE%AF/SDK%E5%BC%80%E5%8F%91%E9%9B%86%E6%88%90/iOS%E5%BC%80%E5%8F%91%E9%9B%86%E6%88%90/%E5%88%9D%E5%A7%8B%E5%8C%96)

暂不支持以下设置：

enabledHttps

enabledHttpsForInfo

enabledHttpsForMessage

shouldIgnoreNotification

fileProtectionNone

maxAutoLoginRetryTimes

maximumLogDays

reconnectInBackgroundStateDisabled

teamReceiptEnabled
