package cn.wildfire.chat.moment;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.wildfire.chat.moment.third.interfaces.OnFeedUserClickListener;
import cn.wildfire.chat.moment.thirdbar.BaseTitleBarActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.moment.MomentClient;
import cn.wildfirechat.moment.model.Feed;
import cn.wildfirechat.remote.ChatManager;

public class FeedMessageActivity extends BaseTitleBarActivity implements OnFeedUserClickListener, FeedMessagAdapter.OnFeedMessageLongClickListener, FeedMessagAdapter.OnFeedMessageClickListener, FeedMessagAdapter.OnLoadMoreMessageClickListener {

    private RecyclerView recyclerView;
    private FeedMessagAdapter feedMessagAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        init();
    }

    @Override
    protected boolean isTranslucentStatus() {
        return true;
    }

    @Override
    protected boolean isFitsSystemWindows() {
        return false;
    }


    private void init() {
        setTitleLeftText("朋友圈消息");
        recyclerView = findViewById(R.id.recyclerView);
        feedMessagAdapter = new FeedMessagAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(feedMessagAdapter);
        feedMessagAdapter.setOnFeedMessageClickListener(this);
        feedMessagAdapter.setOnFeedMessageLongClickListener(this);
        feedMessagAdapter.setOnFeedUserClickListener(this);
        feedMessagAdapter.setOnLoadMoreMessageClickListener(this);

        List<Message> messages = MomentClient.getInstance().getFeedMessages(0, true);
        feedMessagAdapter.setFeedMessages(messages);
        feedMessagAdapter.notifyDataSetChanged();
        MomentClient.getInstance().clearUnreadStatus();
    }

    @Override
    public void onTitleRightClick() {
        // TODO
    }

    @Override
    public void onFeedMessageClick(long feedId) {
//        FeedDetailActivity.feed = mFriendCircleAdapter.getmFriendCircleBeans().get(feedPosition);
//        Intent intent = new Intent(this, FeedDetailActivity.class);
//        startActivity(intent);
        MomentClient.getInstance().getFeed(feedId, new MomentClient.GetFeedCallback() {
            @Override
            public void onSuccess(Feed feed) {

            }

            @Override
            public void onFailure(int errorCode) {

            }
        });
    }

    @Override
    public void onFeedCommentMessageClick(long feedId, long commentId) {
        // TODO show feed detail
        MomentClient.getInstance().getFeed(feedId, new MomentClient.GetFeedCallback() {
            @Override
            public void onSuccess(Feed feed) {

            }

            @Override
            public void onFailure(int errorCode) {

            }
        });
    }

    @Override
    public void onFeedMessageLongClick(long feedId) {
        // TODO show feed detail
    }

    @Override
    public void onFeedCommentMessageLongClick(long feedId, long commentId) {

    }

    @Override
    public void onFeedUserClick(String userId) {
        Intent intent = new Intent(this, FeedListActivity.class);
        intent.putExtra("userInfo", ChatManager.Instance().getUserInfo(userId, false));
        startActivity(intent);
    }

    @Override
    public void onLoadMoreFeedMessageClick() {
        List<Message> feedMessages = feedMessagAdapter.getFeedMessages();
        long msgId = 0;
        int insertPosition = 0;
        if (feedMessages != null && !feedMessages.isEmpty()) {
            msgId = feedMessages.get(feedMessages.size() - 1).messageId;
            insertPosition = feedMessages.size();
        }
        feedMessages = MomentClient.getInstance().getFeedMessages(msgId, false);
        feedMessagAdapter.addFeedMessages(feedMessages);
        feedMessagAdapter.notifyItemRangeInserted(insertPosition, feedMessages.size());
    }
}
