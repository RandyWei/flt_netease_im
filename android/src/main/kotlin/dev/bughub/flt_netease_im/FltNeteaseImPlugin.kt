package dev.bughub.flt_netease_im

import android.util.Log
import com.chinahrt.flutter_plugin_demo.QueuingEventSink
import com.netease.nimlib.sdk.*
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.mixpush.MixPushConfig
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar


class FltNeteaseImPlugin(var registrar: Registrar) : MethodCallHandler {

    var loginQueuingEventSink = QueuingEventSink()

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val plugin = FltNeteaseImPlugin(registrar)
            val channel = MethodChannel(registrar.messenger(), "bughub.dev/flt_netease_im")
            channel.setMethodCallHandler(plugin)
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {

        val context = registrar.activeContext()

        when {
            call.method == "initSDK" -> {//初始化SDK
                val optionsArg = call.argument<Map<*, *>>("options")
                val loginInfoArg = call.argument<Map<*, *>>("loginInfo")

                NIMClient.config(context, loginInfo(loginInfoArg), options(optionsArg))
                NIMClient.initSDK()

                //注册登录监听
                val eventChannel = EventChannel(registrar.messenger(), "bughub.dev/flt_netease_im/events[login]")
                eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
                    override fun onListen(p0: Any?, sink: EventChannel.EventSink?) {
                        loginQueuingEventSink.setDelegate(sink)
                    }

                    override fun onCancel(p0: Any?) {
                        loginQueuingEventSink.setDelegate(null)
                    }
                })

                result.success(null)
            }
            call.method == "login" -> {

                val args = call.arguments as Map<*, *>
                Log.i("11111", call.arguments.toString())
                val account = args["account"] as String?
                val token = args["token"] as String?
                val appKey = args["appKey"] as String?
                val loginInfo = LoginInfo(account, token, appKey)

                NIMSDK.getAuthService().login(loginInfo).setCallback(object : RequestCallback<LoginInfo> {
                    override fun onSuccess(info: LoginInfo?) {
                        Log.i("11111", "onSuccess$info")
                        val eventResult = HashMap<String, Any>()
                        eventResult["event"] = "LOGIN_SUCCESS"
                        val loginInfoMap = HashMap<String, Any?>()
                        loginInfoMap["account"] = info?.account
                        loginInfoMap["appKey"] = info?.appKey
                        loginInfoMap["token"] = info?.token
                        eventResult["loginInfo"] = loginInfoMap
                        loginQueuingEventSink.success(eventResult)
                    }

                    override fun onFailed(code: Int) {
                        Log.i("11111", "onFailed:$code")
                        loginQueuingEventSink.error(code.toString(), "", "")
                    }

                    override fun onException(p0: Throwable?) {
                        Log.i("11111", "onException:$p0")
                        loginQueuingEventSink.error("-1", p0?.message ?: "", p0?.message ?: "")
                    }
                })

                result.success(null)

            }
            else -> result.notImplemented()
        }
    }

    // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
    private fun loginInfo(args: Map<*, *>?): LoginInfo? {
        if (args == null) return null
        val account = args["account"] as String?
        val token = args["token"] as String?
        val appKey = args["appKey"] as String?
        return LoginInfo(account, token, appKey)
    }

    // 如果返回值为 null，则全部使用默认参数。
    private fun options(args: Map<*, *>?): SDKOptions {
        val options = SDKOptions()

        if (args == null) return options

        //设置云信SDK的appKey。appKey还可以通过在AndroidManifest文件中，通过meta-data的方式设置。 如果两处都设置了，取此处的值。
        val appKey = args["appKey"]
        Log.i("initSdk", appKey.toString())
        options.appKey = appKey as String?

        //开启对动图缩略图的支持
        val animatedImageThumbnailEnabled = args["animatedImageThumbnailEnabled"]
        options.animatedImageThumbnailEnabled = animatedImageThumbnailEnabled as Boolean? ?: false

        //是否检查 Manifest 配置 最好在调试阶段打开，调试通过之后请关掉
        val checkManifestConfig = args["checkManifestConfig"] as Boolean
        options.checkManifestConfig = checkManifestConfig

        //禁止后台进程唤醒ui进程
        val disableAwake = args["disableAwake"] as Boolean
        options.disableAwake = disableAwake

        //是否使用随机退避重连策略，默认true，强烈建议打开。如需关闭，请咨询云信技术支持。
        val enableBackOffReconnectStrategy = args["enableBackOffReconnectStrategy"] as Boolean
        options.enableBackOffReconnectStrategy = enableBackOffReconnectStrategy

        //是否启用网络连接优化策略，默认开启。
        val enableLBSOptimize = args["enableLBSOptimize"] as Boolean
        options.enableLBSOptimize = enableLBSOptimize

        //是否启用群消息已读功能，默认关闭
        val enableTeamMsgAck = args["enableTeamMsgAck"] as Boolean
        options.enableTeamMsgAck = enableTeamMsgAck

        //是否提高SDK进程优先级（默认提高，可以降低SDK核心进程被系统回收的概率）；如果部分机型有意外的情况，可以根据机型决定是否开启。 4.6.0版本起，弱 IM 模式下，强制不提高SDK进程优先级
        val improveSDKProcessPriority = args["improveSDKProcessPriority"] as Boolean
        options.improveSDKProcessPriority = improveSDKProcessPriority

        //登录时的自定义字段 ， 登陆成功后会同步给其他端 ，获取可参考 AuthServiceObserver#observeOtherClients()
        val loginCustomTag = args["loginCustomTag"]
        options.loginCustomTag = loginCustomTag as String?


        //通知栏提醒文案定制
        val messageNotifierCustomizationArg = args["messageNotifierCustomization"]
        messageNotifierCustomizationArg?.let {
            //val map = it as Map<*, *>
//            val messageNotifierCustomization = MessageNotifierCustomizationCompat
            //options.messageNotifierCustomization
        }

        //第三方推送配置
        val mixPushConfigArg = args["mixPushConfig"]
        mixPushConfigArg?.let {
            val map = it as Map<*, *>
            val mixPushConfig = MixPushConfig()

            val fcmCertificateName = map["fcmCertificateName"]
            mixPushConfig.fcmCertificateName = fcmCertificateName as String?

            val hwCertificateName = map["hwCertificateName"]
            mixPushConfig.hwCertificateName = hwCertificateName as String?

            val mzAppId = map["mzAppId"]
            mixPushConfig.mzAppId = mzAppId as String?

            val mzAppKey = map["mzAppKey"]
            mixPushConfig.mzAppKey = mzAppKey as String?

            val mzCertificateName = map["mzCertificateName"]
            mixPushConfig.mzCertificateName = mzCertificateName as String?

            val vivoCertificateName = map["vivoCertificateName"]
            mixPushConfig.vivoCertificateName = vivoCertificateName as String?

            val xmAppId = map["xmAppId"]
            mixPushConfig.xmAppId = xmAppId as String?

            val xmAppKey = map["xmAppKey"]
            mixPushConfig.xmAppKey = xmAppKey as String?

            val xmCertificateName = map["xmCertificateName"]
            mixPushConfig.xmCertificateName = xmCertificateName as String?

            options.mixPushConfig = mixPushConfig
        }

        //nos token 场景配置
        val mNosTokenSceneConfigArg = args["mNosTokenSceneConfig"]
        mNosTokenSceneConfigArg?.let {
            //            options.mNosTokenSceneConfig = NosTokenSceneConfig()
        }

        //是否需要SDK自动预加载多媒体消息的附件。
        //如果打开，SDK收到多媒体消息后，图片和视频会自动下载缩略图，音频会自动下载文件。
        //如果关闭，第三方APP可以只有决定要不要下载以及何时下载附件内容，典型时机为消息列表第一次滑动到 这条消息时，才触发下载，以节省用户流量。
        //该开关默认打开。
        val preloadAttach = args["preloadAttach"] as Boolean
        options.preloadAttach = preloadAttach

        //预加载服务，默认true，不建议设置为false，预加载连接可以优化登陆流程，提升用户体验
        val preLoadServers = args["preLoadServers"] as Boolean
        options.preLoadServers = preLoadServers

        //外置存储根目录,用于存放多媒体消息文件。
        //若不设置或设置的路径不可用，将使用"external storage root/packageName/nim/"作为根目录 注意，4.4.0 之后版本，如果开发者配置在 Context#getExternalCacheDir 及 Context.getExternalFilesDir 等应用扩展存储缓存目录下，SDK 内部将不再检查写权限。但这里的文件会随着App卸载而被删除，也可以由用户手动在设置界面里面清除。
        val sdkStorageRootPath = args["sdkStorageRootPath"]
        options.sdkStorageRootPath = sdkStorageRootPath as String?


        //配置专属服务器的地址。
        val serverConfigArg = args["serverConfig"]
        serverConfigArg?.let {
            val map = it as Map<*, *>
            val serverConfig = ServerAddresses()

            //云信数据统计服务器地址
            val bdServerAddress = map["bdServerAddress"]
            serverConfig.bdServerAddress = bdServerAddress as String?

            //IM 默认的link服务器地址，当IM LBS不可用时先连接该地址 填"IP/Host:PORT"
            val defaultLink = map["defaultLink"]
            serverConfig.defaultLink = defaultLink as String?

            //IM LBS服务器地址，通过它获取IM link 地址信息 填http/https地址
            val lbs = map["lbs"]
            serverConfig.lbs = lbs as String?

            //NOS下载加速域名/地址，用于替换NOS下载url中的 nosDownload。 提供两种方式： 1) [4.4.0+开始支持]模板方式：填写云信规定的两种模板：{bucket}.nosdn.127.net/{object} 或者 nosdn.127.net/{bucket}/{object}，其中 {bucket} 和 {object} 作为标识符，必须填写。域名部分可以替换为您申请的加速域名。 2) [所有版本支持]非模板方式：填写用于加速的 http/https地址，例如：http://111.222.111.22:9090
            val nosAccess = map["nosAccess"]
            serverConfig.nosAccess = nosAccess as String?

            //NOS下载地址的host，用于拼接最终获得的文件URL地址，也支持该host替换成下载加速域名/地址 nosAccess.
            val nosDownload = map["nosDownload"]
            serverConfig.nosDownload = nosDownload as String?

            //[4.6.0版本新增] NOS下载地址拼接模板，用于拼接最终得到的下载地址。 默认是 {bucket}.nosdn.127.net/{object}，SDK 上传资源后生成的下载地址为 https://bucket.nosdn.127.net/object
            val nosDownloadUrlFormat = map["nosDownloadUrlFormat"]
            serverConfig.nosDownloadUrlFormat = nosDownloadUrlFormat as String?

            //NOS上传是否需要支持https。SDK 3.2版本后默认支持https，同时需要配置 nosUpload!
            val nosSupportHttps = map["\tnosSupportHttps"] as Boolean
            serverConfig.nosSupportHttps = nosSupportHttps

            //NOS上传服务器主机地址（仅nosSupportHttps=true时有效，用作https上传时的域名校验及http header host字段填充） 填host地址
            val nosUpload = map["nosUpload"]
            serverConfig.nosUpload = nosUpload as String?

            //NOS上传默认的link服务器地址，当NOS LBS不可用时先连接该地址 填http/https地址
            val nosUploadDefaultLink = map["nosUploadDefaultLink"]
            serverConfig.nosUploadDefaultLink = nosUploadDefaultLink as String?

            //NOS上传LBS服务器地址 填http/https地址
            val nosUploadLbs = map["nosUploadLbs"]
            serverConfig.nosUploadLbs = nosUploadLbs as String?

            //云信运行时异常统计服务器地址
            val ntServerAddress = map["ntServerAddress"]
            serverConfig.ntServerAddress = ntServerAddress as String?

            //连接云信服务器加密数据通道的公钥
            val publicKey = map["publicKey"]
            serverConfig.publicKey = publicKey as String?

            //连接云信服务器加密数据通道的公钥的版本号（默认0）
            val publicKeyVersion = map["publicKeyVersion"]
            serverConfig.publicKeyVersion = publicKeyVersion as Int

            //是否是测试服
            val test = map["test"] as Boolean
            serverConfig.test = test

            options.serverConfig = serverConfig
        }

        //是否开启会话已读多端同步，支持多端同步会话未读数
        val sessionReadAck = args["sessionReadAck"] as Boolean
        options.sessionReadAck = sessionReadAck

        //是否需要将被撤回的消息计入未读数 默认是false，即撤回消息不影响未读数，客户端通常直接写入一条Tip消息，用于提醒"对方撤回了一条消息"，该消息也不计入未读数，不影响当前会话的未读数。 如果设置为true，撤回消息后未读数将减1.
        val shouldConsiderRevokedMessageUnreadCount = args["shouldConsiderRevokedMessageUnreadCount"] as Boolean?
        options.shouldConsiderRevokedMessageUnreadCount = shouldConsiderRevokedMessageUnreadCount
                ?: false

        //状态提醒设置。
        //默认为null，SDK不提供状态栏提醒功能，由客户APP自行实现
        // 如果将新消息通知提醒托管给 SDK 完成，需要添加以下配置。否则无需设置。
        val statusBarNotificationConfigArg = args["statusBarNotificationConfig"]
        statusBarNotificationConfigArg?.let {
            val map = it as Map<*, *>

            val statusBarNotificationConfig = StatusBarNotificationConfig()

            //如果群名称为null 或者空串，则使用customTitleWhenTeamNameEmpty 作为通知栏title
            val customTitleWhenTeamNameEmpty = map["customTitleWhenTeamNameEmpty"]
            statusBarNotificationConfig.customTitleWhenTeamNameEmpty = customTitleWhenTeamNameEmpty as String?

            //免打扰的开始时间, 格式为HH:mm(24小时制)。
            val downTimeBegin = map["downTimeBegin"]
            statusBarNotificationConfig.downTimeBegin = downTimeBegin as String?

            //免打扰的结束时间, 格式为HH:mm(24小时制)。
            //如果结束时间小于开始时间，免打扰时间为开始时间-24:00-结束时间。
            val downTimeEnd = map["downTimeEnd"]
            statusBarNotificationConfig.downTimeEnd = downTimeEnd as String?

            //免打扰设置开关。默认为关闭。
            val downTimeToggle = map["downTimeToggle"] as Boolean
            statusBarNotificationConfig.downTimeToggle = downTimeToggle

            //不显示消息详情开关, 同时也不再显示消息发送者昵称
            //默认为false
            val hideContent = map["hideContent"] as Boolean
            statusBarNotificationConfig.hideContent = hideContent

            //呼吸灯的颜色 The color of the led.
            val ledARGB = map["ledARGB"]
            statusBarNotificationConfig.ledARGB = ledARGB as Int

            //呼吸灯熄灭时的持续时间（毫秒） The number of milliseconds for the LED to be off while it's flashing.
            val ledOffMs = map["ledOffMs"]
            statusBarNotificationConfig.ledOffMs = ledOffMs as Int

            //呼吸灯亮时的持续时间（毫秒） The number of milliseconds for the LED to be on while it's flashing.
            val ledOnMs = map["ledOnMs"]
            statusBarNotificationConfig.ledOnMs = ledOnMs as Int

            //消息通知栏颜色，将应用到 NotificationCompat.Builder 的 setColor 方法 对Android 5.0 以后机型会影响到smallIcon
            val notificationColor = map["notificationColor"]
            statusBarNotificationConfig.notificationColor = notificationColor as Int

            //通知栏提醒的响应intent的activity类型。
            //可以为null。如果未提供，将使用包的launcher的入口intent的activity。
            val notificationEntranceArg = map["notificationEntrance"]
            notificationEntranceArg?.let {
                //statusBarNotificationConfig.notificationEntrance = Class<android.app.Activity>.forName(notificationEntranceArg as String)::class.java
            }

            //消息通知栏展示样式是否折叠。默认是true，这样云信消息端内消息提醒最多之占一栏。 由于端外推送消息为展开模式，可以设置为false达到端内、端外表现一致。
            val notificationFolded = map["notificationFolded"]
            statusBarNotificationConfig.notificationFolded = notificationFolded as Boolean

            //状态栏提醒的小图标的资源ID。
            //如果不提供，使用app的icon TODO
//            val notificationSmallIconId = map["notificationSmallIconId"]
//            statusBarNotificationConfig.notificationSmallIconId = notificationSmallIconId as Int

            //响铃提醒的声音资源，如果不提供，使用系统默认提示音。 TODO
//            val notificationSound = map["notificationSound"]
//            statusBarNotificationConfig.notificationSound = notificationSound as String?

            //是否需要响铃提醒。
            //默认为true
            val ring = map["ring"]
            statusBarNotificationConfig.ring = ring as Boolean

            //是否APP图标显示未读数(红点) 仅针对Android 8.0+有效
            val showBadge = map["showBadge"]
            statusBarNotificationConfig.showBadge = showBadge as Boolean

            //通知栏提醒的标题是否只显示应用名。默认是 false，当有一个会话发来消息时，显示会话名；当有多个会话发来时，显示应用名。 修改为true，那么无论一个还是多个会话发来消息，标题均显示应用名。 应用名称请在AndroidManifest的application节点下设置android:label。
            val titleOnlyShowAppName = map["titleOnlyShowAppName"]
            statusBarNotificationConfig.titleOnlyShowAppName = titleOnlyShowAppName as Boolean

            //是否需要振动提醒。
            //默认为true
            val vibrate = map["vibrate"]
            statusBarNotificationConfig.vibrate = vibrate as Boolean


            options.statusBarNotificationConfig = statusBarNotificationConfig
        }

        //群通知消息是否计入未读数，默认不计入未读
        val teamNotificationMessageMarkUnread = args["teamNotificationMessageMarkUnread"] as Boolean
        options.teamNotificationMessageMarkUnread = teamNotificationMessageMarkUnread

        //消息缩略图的尺寸。
        //该值为最长边的大小。下载的缩略图最长边不会超过该值。
        val thumbnailSize = args["thumbnailSize"]
        options.thumbnailSize = thumbnailSize as Int

        //是否检查并使用Asset目录下的私有化服务器配置文件server.conf(固定命名） 默认是false 一般只有私有化项目中，在私有化测试期间需要开启此选项，并将配置文件放在Assets/server.conf 注意：如果在SDKOptions.serverConfig已经配置了，那么该本地配置文件将失效！
        val useAssetServerAddressConfig = args["useAssetServerAddressConfig"] as Boolean
        options.useAssetServerAddressConfig = useAssetServerAddressConfig

        //通知栏显示用户昵称和头像
        val userInfoProviderArg = args["userInfoProvider"]
        userInfoProviderArg?.let {
            //            val map = it as Map<*, *>
        }

        //使用性能更好的SDK日志模式。默认使用普通日志模式。
        val useXLog = args["useXLog"] as Boolean
        options.useXLog = useXLog

        return options
    }


}
