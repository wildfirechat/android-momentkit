## 说明

本仓库是野火IM Android端朋友圈UI相关代码，依赖于```moment client```库，```moment client```库是闭源的，需要购买才能使用。

## 编译
0. ***请严格按照以下步骤进行，否则会编译不过，或者运行时出现错误。***

1. 联系官方，购买```moment client aar```相关授权。

2. 将***本仓库***下载到和```android-chat```的同级目录，并确保***本仓库***下载之后的目录名字是```android-momentkit```

3. ```android-chat```项目中新建```aar module```，将```moment client aar```引入，具体如下：
   1. Android Studio -> File -> New -> New Module -> Import .JAR/.AAR Package 
   2. Import Module From Library 界面 File name：选择步骤1中购买的aar文件；Subproject name 填写```momentclient```
   3. Finish，完成之后，```android-chat```项目根目录下的```settting.gradle```会新增一行```include ':momentclient'```，并且```android-chat```项目根目录下会新增一个```momentclient```目录。

4. 修改```android-chat/uikit/build.gradle```，将以下部分取消注释：

      ```
      //            java.srcDirs += ['../../android-momentkit/src/main/java']
      //            res.srcDirs += ['../../android-momentkit/src/main/res-moment']
      
      //    implementation project(':momentclient') //和步骤3中填写的Subproject name一致
      //    api 'me.everything:overscroll-decor-android:1.0.4'
      ```

5. 修改```uikit/src/main/AndroidManifest.xml```，将以下取消注释：

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
