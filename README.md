## 野火IM解决方案

野火IM是专业级即时通讯和实时音视频整体解决方案，由北京野火无限网络科技有限公司维护和支持。

主要特性有：私有部署安全可靠，性能强大，功能齐全，全平台支持，开源率高，部署运维简单，二次开发友好，方便与第三方系统对接或者嵌入现有系统中。详细情况请参考[在线文档](https://docs.wildfirechat.cn)。

主要包括一下项目：

| [GitHub仓库地址(主站)](https://github.com/wildfirechat)      | [码云仓库地址(镜像)](https://gitee.com/wfchat)        | 说明                                                                                      | 备注                                           |
| ------------------------------------------------------------ | ----------------------------------------------------- | ----------------------------------------------------------------------------------------- | ---------------------------------------------- |
| [android-chat](https://github.com/wildfirechat/android-chat) | [android-chat](https://gitee.com/wfchat/android-chat) | 野火IM Android SDK源码和App源码                                                           | 可以很方便地进行二次开发，或集成到现有应用当中 |
| [ios-chat](https://github.com/wildfirechat/ios-chat)         | [ios-chat](https://gitee.com/wfchat/ios-chat)         | 野火IM iOS SDK源码和App源码                                                               | 可以很方便地进行二次开发，或集成到现有应用当中 |
| [pc-chat](https://github.com/wildfirechat/pc-chat)           | [pc-chat](https://gitee.com/wfchat/pc-chat)           | 基于[Electron](https://electronjs.org/)开发的PC平台应用                                   |                                                |
| [web-chat](https://github.com/wildfirechat/web-chat)         | [web-chat](https://gitee.com/wfchat/web-chat)         | Web平台的Demo, [体验地址](http://web.wildfirechat.cn)                                     |                                                |
| [wx-chat](https://github.com/wildfirechat/wx-chat)           | [wx-chat](https://gitee.com/wfchat/wx-chat)           | 微信小程序平台的Demo                                                                      |                                                |
| [server](https://github.com/wildfirechat/server)             | [server](https://gitee.com/wfchat/server)             | IM server                                                                                 |                                                |
| [app server](https://github.com/wildfirechat/app_server)     | [app server](https://gitee.com/wfchat/app_server)     | 应用服务端                                                                                |                                                |
| [robot_server](https://github.com/wildfirechat/robot_server) | [robot_server](https://gitee.com/wfchat/robot_server) | 机器人服务端                                                                              |                                                |
| [push_server](https://github.com/wildfirechat/push_server)   | [push_server](https://gitee.com/wfchat/push_server)   | 推送服务器                                                                                |                                                |
| [docs](https://github.com/wildfirechat/docs)                 | [docs](https://gitee.com/wfchat/docs)                 | 野火IM相关文档，包含设计、概念、开发、使用说明，[在线查看](https://docs.wildfirechat.cn/) |                                                |  |



本仓库是野火IM Android端朋友圈UI相关代码，依赖于```moment client```库，```moment client```库是闭源的，需要购买才能使用。

## 编译
0. ***请严格按照以下步骤进行，否则会编译不过，或者运行时出现错误。***

1. 联系官方，购买```moment client aar```相关授权。

2. 将***本仓库***下载到和```android-chat```的同级目录，并确保***本仓库***下载之后的目录名字是```android-momentkit```，***比如```android-chat```和```android-momentkit```都在```workspace```目录之下，不是将```android-momentkit```放到```android-chat```目录之下***

3. 将第一步中获取到的```momentclient-release.aar```放到```android-chat/uikit/libs```目录下
4. 修改```android-chat/uikit/build.gradle```，将以下部分取消注释：

      ```
      //            java.srcDirs += ['../../android-momentkit/src/main/java']
      //            res.srcDirs += ['../../android-momentkit/src/main/res-moment']

      //    api 'me.everything:overscroll-decor-android:1.0.4'
      ```

5. 修改```android-chat/uikit/src/main/AndroidManifest.xml```，将以下取消注释：

      ```
      <!--        <activity android:name="cn.wildfire.chat.moment.PublishFeedActivity" />-->
      <!--        <activity android:name="cn.wildfire.chat.moment.FeedMessageActivity" />-->
      <!--        <activity android:name="cn.wildfire.chat.moment.FeedDetailActivity" />-->
      <!--        <activity android:name="cn.wildfire.chat.moment.FeedListActivity">-->
      <!--            <intent-filter>-->
      <!--                <action android:name="${applicationId}.moment" />-->
      <!--                <category android:name="android.intent.category.DEFAULT" />-->
      <!--            </intent-filter>-->
      <!--        </activity>-->
      <!--        <activity android:name="cn.wildfire.chat.moment.FeedVisibleScopeActivity" />-->

      ```

## 感谢

本项目基于[HighPerformanceFriendsCircle](https://github.com/Micrason/HighPerformanceFriendsCircle)和[MultiWeChat](https://github.com/MsPenghao/MultiWeChat)开发，在此，对他们的无私奉献表示感谢。
