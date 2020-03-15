## 说明

本仓库是野火IM Android端朋友圈UI相关代码，依赖于```moment client```库，```moment client```库是闭源的，需要购买才能使用。

## 编译

1. 联系官方，购买```moment client aar```相关授权。

2. 将本仓库下载到和```android-chat```的同级目录，并确保本仓库下载之后的目录名字是```android-momentkit```

3. ```android-chat```中新建```aar module```，将```moment client aar```引入。

4. 修改```android-chat/chat/build.gradle```，将以下部分取消注释：

      ```
      //            java.srcDirs += ['../../android-momentkit/src/main/java']
      //            res.srcDirs += ['../../android-momentkit/src/main/res-moment']
      
      //    implementation project(':momentclient') //和步骤3相关
      //    api 'me.everything:overscroll-decor-android:1.0.4'
      ```

5. 修改```chat/src/main/AndroidManifest.xml```，将以下取消注释：

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

