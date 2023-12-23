package cn.wildfire.chat.moment;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Collections;

import cn.wildfire.chat.moment.third.beans.FriendCircleBean;
import cn.wildfire.chat.moment.third.widgets.TitleBar;
import cn.wildfire.chat.kit.R;
import cn.wildfirechat.moment.MomentClient;
import cn.wildfirechat.moment.model.Feed;

public class FeedDetailActivity extends BaseFeedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    protected int contentLayoutResId() {
        return R.layout.moment_activity_detail;
    }

    @Override
    protected void initView() {
        super.initView();
        long feedId = getIntent().getLongExtra("feedId", 0);
        if (feedId == 0) {
            finish();
        }

        // TODO show loading
        MomentClient.getInstance().getFeed(feedId, new MomentClient.GetFeedCallback() {
            @Override
            public void onSuccess(Feed feed) {
                if (isFinishing()) {
                    return;
                }
                // TODO dismiss loading

                mFriendCircleAdapter.showBottomPadding();
                mFriendCircleAdapter.setFriendCircleBeans(Collections.singletonList(FriendCircleBean.fromFeed(FeedDetailActivity.this, feed)));
            }

            @Override
            public void onFailure(int errorCode) {
                if (isFinishing()) {
                    return;
                }
                // TODO dismiss loading
                Toast.makeText(FeedDetailActivity.this, "" + errorCode, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void initTitleBar() {
        getTitleBar().setLeftText("详情");
        setLeftTextColor(Color.parseColor("#040404"));
        setTitleMode(TitleBar.TitleBarMode.MODE_LEFT);
        setTitleLeftIcon(R.drawable.back_left_black);
    }

    // TODO load only the current feed
    protected void loadFeeds() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
