package cn.wildfire.chat.moment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.wildfire.chat.moment.third.Constants;
import cn.wildfire.chat.moment.third.adapters.FriendCircleAdapter;
import cn.wildfire.chat.moment.third.beans.CommentBean;
import cn.wildfire.chat.moment.third.beans.FriendCircleBean;
import cn.wildfire.chat.moment.third.beans.PraiseBean;
import cn.wildfire.chat.moment.third.interfaces.OnCommentItemClickListener;
import cn.wildfire.chat.moment.third.interfaces.OnCommentItemLongClickListener;
import cn.wildfire.chat.moment.third.interfaces.OnCommentUserClickListener;
import cn.wildfire.chat.moment.third.interfaces.OnFeedItemLongClickListener;
import cn.wildfire.chat.moment.third.interfaces.OnFeedUserClickListener;
import cn.wildfire.chat.moment.third.interfaces.OnPraiseOrCommentClickListener;
import cn.wildfire.chat.moment.third.others.FriendsCircleAdapterDivideLine;
import cn.wildfire.chat.moment.third.utils.Utils;
import cn.wildfire.chat.moment.thirdbar.BaseTitleBarActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.moment.FeedCommentType;
import cn.wildfirechat.moment.MomentClient;
import cn.wildfirechat.moment.model.Feed;
import cn.wildfirechat.remote.ChatManager;

public abstract class BaseFeedActivity extends BaseTitleBarActivity implements
        OnPraiseOrCommentClickListener,
        OnCommentItemClickListener,
        OnCommentUserClickListener,
        OnFeedUserClickListener,
        OnCommentItemLongClickListener,
        OnFeedItemLongClickListener {

    protected FriendCircleAdapter mFriendCircleAdapter;
    protected RecyclerView recyclerView;
    protected LinearLayoutManager layoutManager;

    protected CommentFragment commentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(contentLayoutResId());

        initView();
    }

    protected abstract int contentLayoutResId();


    protected void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                hideCommentFragment();
            }

        });

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new FriendsCircleAdapterDivideLine());

        mFriendCircleAdapter = new FriendCircleAdapter(this, recyclerView);
        mFriendCircleAdapter.setOnCommentItemClickListener(this);
        mFriendCircleAdapter.setOnCommentItemLongClickListener(this);
        mFriendCircleAdapter.setOnFeedItemLongClickListener(this);
        mFriendCircleAdapter.setOnCommentUserClickListener(this);
        mFriendCircleAdapter.setOnFeedUserClickListener(this);
        recyclerView.setAdapter(mFriendCircleAdapter);

        initTitleBar();
    }

    protected void initTitleBar() {

    }

    protected List<FriendCircleBean> feedsToFriendCircleBeans(List<Feed> feeds) {
        List<FriendCircleBean> friendCircleBeans = new ArrayList<>();
        if (feeds != null) {
            for (Feed feed : feeds) {
                FriendCircleBean friendCircleBean = FriendCircleBean.fromFeed(BaseFeedActivity.this, feed);
                friendCircleBeans.add(friendCircleBean);
            }
        }
        return friendCircleBeans;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTitleRightClick() {
    }

    @Override
    public void onTitleLeftClick() {
        super.onTitleLeftClick();
    }

    @Override
    protected boolean isTranslucentStatus() {
        return true;
    }

    @Override
    protected boolean isFitsSystemWindows() {
        return false;
    }

    @Override
    public void onPraiseClick(int position) {
        List<FriendCircleBean> friendCircleBeans = mFriendCircleAdapter.getmFriendCircleBeans();
        FriendCircleBean friendCircleBean = friendCircleBeans.get(position);
        List<PraiseBean> praiseBeans = friendCircleBean.getPraiseBeans();
        PraiseBean praiseBean = new PraiseBean();
        UserInfo userInfo = ChatManager.Instance().getUserInfo(ChatManager.Instance().getUserId(), false);
        praiseBean.setPraiseUserId(userInfo.uid);
        praiseBean.setPraiseUserName(userInfo.displayName);
        if (praiseBeans == null) {
            List<PraiseBean> beans = new ArrayList<>();
            MomentClient.getInstance().postComment(FeedCommentType.Comment_Thumbup_Type, friendCircleBean.getId(), null, null, null, new MomentClient.PostCallback() {
                @Override
                public void onSuccess(long id, long timestamp) {
                    praiseBean.setId(id);
                    beans.add(praiseBean);
                    friendCircleBean.setPraiseBeans(beans);
                    mFriendCircleAdapter.notifyItemChanged(position + mFriendCircleAdapter.headerCount(), Collections.singleton("star"));
                }

                @Override
                public void onFailure(int errorCode) {
                    // do nothing
                }
            });
        } else {
            if (praiseBeans.contains(praiseBean)) {
                int i = praiseBeans.indexOf(praiseBean);
                MomentClient.getInstance().deleteComment(praiseBeans.get(i).getId(), friendCircleBean.getId(), new MomentClient.GeneralCallback() {
                    @Override
                    public void onSuccess() {
                        praiseBeans.remove(praiseBean);
                        friendCircleBean.setPraiseBeans(praiseBeans);
                        mFriendCircleAdapter.notifyItemChanged(position + mFriendCircleAdapter.headerCount(), Collections.singleton("star"));
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        // do nothing
                    }
                });
            } else {
                MomentClient.getInstance().postComment(FeedCommentType.Comment_Thumbup_Type, friendCircleBean.getId(), null, null, null, new MomentClient.PostCallback() {
                    @Override
                    public void onSuccess(long id, long timestamp) {
                        praiseBean.setId(id);
                        praiseBeans.add(praiseBean);
                        friendCircleBean.setPraiseBeans(praiseBeans);
                        mFriendCircleAdapter.notifyItemChanged(position + mFriendCircleAdapter.headerCount(), Collections.singleton("star"));
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        // do nothing
                    }
                });
            }
        }
    }

    @Override
    public void onCommentClick(View view, int position) {
        View itemView = layoutManager.findViewByPosition(position + mFriendCircleAdapter.headerCount());

        int scrollBy = getCommentPanelTop() - itemView.getBottom();
        recyclerView.scrollBy(0, -scrollBy);

        List<FriendCircleBean> friendCircleBeans = mFriendCircleAdapter.getmFriendCircleBeans();
        FriendCircleBean friendCircleBean = friendCircleBeans.get(position);

        showCommentFragment(friendCircleBean.getId(), 0);
    }

    @Override
    public void onCommentItemClick(View commentItemView, int feedPosition, int commentPosition) {
        if (commentFragment != null) {
            hideCommentFragment();
            return;
        }

        List<FriendCircleBean> friendCircleBeans = mFriendCircleAdapter.getmFriendCircleBeans();
        FriendCircleBean friendCircleBean = friendCircleBeans.get(feedPosition);
        List<CommentBean> commentBeans = friendCircleBean.getCommentBeans();
        CommentBean commentBean = commentBeans.get(commentPosition);

        if (commentBean.getChildUserId().equals(ChatManager.Instance().getUserId())) {
            PopupMenu popup = new PopupMenu(this, commentItemView);
            popup.getMenuInflater().inflate(R.menu.comment_item_delete_popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.delete:
                        deleteComment(feedPosition, commentPosition);
                        break;
                    default:
                        break;
                }

                return true;
            });
            popup.show();
        } else {
            int[] location = new int[2];
            commentItemView.getLocationInWindow(location);
            int bottom = location[1] + commentItemView.getHeight();

            int scrollBy = getCommentPanelTop() - bottom;
            recyclerView.scrollBy(0, -scrollBy);
            showCommentFragment(friendCircleBean.getId(), commentBean.getId());
        }
    }

    @Override
    public void onCommentItemLongClick(View commentItemView, int feedPosition, int commentPosition) {
        int menuId = R.menu.comment_item_popup_menu;

        CommentBean commentBean = mFriendCircleAdapter.getmFriendCircleBeans().get(feedPosition).getCommentBeans().get(commentPosition);
        if (commentBean.getChildUserId().equals(ChatManager.Instance().getUserId())) {
            //me
            menuId = R.menu.comment_item_delete_popup_menu;
        }
        PopupMenu popup = new PopupMenu(this, commentItemView);
        popup.getMenuInflater().inflate(menuId, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.copy:
                    Toast.makeText(this, "copy", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.collection:
                    break;
                case R.id.delete:
                    deleteComment(feedPosition, commentPosition);
                    break;
                default:
                    break;
            }
            return true;
        });
        popup.show(); //showing popup menu
    }

    @Override
    public void onFeedItemLongClick(View feedItemView, int feedPosition) {
        FriendCircleBean friendCircleBean = mFriendCircleAdapter.getmFriendCircleBeans().get(feedPosition);
        int menuId;
        if (friendCircleBean.getUserBean().getUserId().equals(ChatManager.Instance().getUserId())) {
            menuId = R.menu.feed_item_delete_popup_menu;
        } else {
            menuId = R.menu.feed_item_popup_menu;
        }
        PopupMenu popup = new PopupMenu(this, feedItemView);
        popup.getMenuInflater().inflate(menuId, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.copy:
                    Toast.makeText(this, "copy", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.collection:
                    break;
                case R.id.delete:
                    deleteFeed(feedPosition);
                    break;
                default:
                    break;
            }
            return true;
        });
        popup.show(); //showing popup menu
    }

    private void showCommentFragment(long feedId, long commentId) {
        if (commentFragment != null) {
            return;
        }
        commentFragment = new CommentFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("feedId", feedId);
        bundle.putLong("commentId", commentId);
        commentFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().
                replace(R.id.commentLayoutContainer, commentFragment, "comment")
                .show(commentFragment)
                .commit();
    }

    private void hideCommentFragment() {
        if (commentFragment == null) {
            return;
        }

        commentFragment.hideKeyboard();
        getSupportFragmentManager().beginTransaction().remove(commentFragment).commit();
        commentFragment = null;
    }

    @Override
    public void onBackPressed() {
        if (commentFragment != null) {
            hideCommentFragment();
            return;
        }

        super.onBackPressed();
    }

    public void commentFeed(long feedId, String comment) {
        int position = mFriendCircleAdapter.getFeedPosition(feedId);
        MomentClient.getInstance().postComment(FeedCommentType.Comment_Comment_Type, feedId, comment, null, null, new MomentClient.PostCallback() {
            @Override
            public void onSuccess(long id, long timestamp) {
                FriendCircleBean friendCircleBean = mFriendCircleAdapter.getFriendCircleBean(feedId);
                CommentBean commentBean = new CommentBean();
                commentBean.setCommentContent(comment);
                UserInfo userInfo = ChatManager.Instance().getUserInfo(ChatManager.Instance().getUserId(), false);
                commentBean.setChildUserId(userInfo.uid);
                commentBean.setChildUserName(userInfo.displayName);
                commentBean.setCommentType(Constants.CommentType.COMMENT_TYPE_SINGLE);
                List<CommentBean> commentBeans = friendCircleBean.getCommentBeans();
                if (commentBeans == null) {
                    commentBeans = new ArrayList<>();
                }
                commentBean.setId(id);
                commentBeans.add(commentBean);
                friendCircleBean.setCommentBeans(commentBeans);
                mFriendCircleAdapter.notifyItemChanged(position + mFriendCircleAdapter.headerCount(), Collections.singleton("comment"));
            }

            @Override
            public void onFailure(int errorCode) {
                // do nothing
            }
        });
        hideCommentFragment();
    }

    public void replyComment(long feedId, long commentId, String comment) {
        int position = mFriendCircleAdapter.getFeedPosition(feedId);
        CommentBean parentCommentBean = mFriendCircleAdapter.getCommentBean(feedId, commentId);
        MomentClient.getInstance().postComment(FeedCommentType.Comment_Comment_Type, feedId, comment, parentCommentBean.getChildUserId(), null, new MomentClient.PostCallback() {
            @Override
            public void onSuccess(long id, long timestamp) {
                FriendCircleBean friendCircleBean = mFriendCircleAdapter.getFriendCircleBean(feedId);
                CommentBean commentBean = new CommentBean();
                commentBean.setCommentContent(comment);
                UserInfo userInfo = ChatManager.Instance().getUserInfo(ChatManager.Instance().getUserId(), false);
                commentBean.setChildUserId(userInfo.uid);
                commentBean.setChildUserName(userInfo.displayName);
                commentBean.setParentUserId(parentCommentBean.getChildUserId());
                commentBean.setParentUserName(parentCommentBean.getChildUserName());
                commentBean.setCommentType(Constants.CommentType.COMMENT_TYPE_REPLY);
                List<CommentBean> commentBeans = friendCircleBean.getCommentBeans();
                if (commentBeans == null) {
                    commentBeans = new ArrayList<>();
                }
                commentBean.setId(id);
                commentBeans.add(commentBean);
                friendCircleBean.setCommentBeans(commentBeans);
                mFriendCircleAdapter.notifyItemChanged(position + mFriendCircleAdapter.headerCount(), Collections.singleton("comment"));
            }

            @Override
            public void onFailure(int errorCode) {
                // do nothing
            }
        });
        hideCommentFragment();
    }

    private int getKeyboardPortraitHeight() {
        int defaultCustomKeyboardSize = getResources().getDimensionPixelSize(R.dimen.default_custom_keyboard_size);
        int minCustomKeyboardSize = getResources().getDimensionPixelSize(R.dimen.min_custom_keyboard_size);
        int keyboardHeight = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("keyboard_height_portrait", defaultCustomKeyboardSize);
        return Math.max(keyboardHeight, minCustomKeyboardSize);
    }

    private int getCommentPanelTop() {
        int commentPanelTop = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("commentPanelTop", 0);
        if (commentPanelTop > 0) {
            return commentPanelTop;
        }

        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int height = outMetrics.heightPixels;
        return height - (getKeyboardPortraitHeight() + Utils.dp2px(50));
    }

    protected void deleteFeed(int feedPosition) {
        List<FriendCircleBean> friendCircleBeans = mFriendCircleAdapter.getmFriendCircleBeans();
        FriendCircleBean friendCircleBean = friendCircleBeans.get(feedPosition);
        List<CommentBean> commentBeans = friendCircleBean.getCommentBeans();
        MomentClient.getInstance().deleteFeed(friendCircleBean.getId(), new MomentClient.GeneralCallback() {
            @Override
            public void onSuccess() {
                friendCircleBeans.remove(feedPosition);
                mFriendCircleAdapter.notifyItemRemoved(feedPosition + mFriendCircleAdapter.headerCount());
            }

            @Override
            public void onFailure(int errorCode) {
                // do nothing
            }
        });
    }

    protected void deleteComment(int feedPosition, int commentPosition) {
        List<FriendCircleBean> friendCircleBeans = mFriendCircleAdapter.getmFriendCircleBeans();
        FriendCircleBean friendCircleBean = friendCircleBeans.get(feedPosition);
        List<CommentBean> commentBeans = friendCircleBean.getCommentBeans();
        CommentBean commentBean = commentBeans.get(commentPosition);
        MomentClient.getInstance().deleteComment(commentBean.getId(), friendCircleBean.getId(), new MomentClient.GeneralCallback() {
            @Override
            public void onSuccess() {
                commentBeans.remove(commentBean);
                friendCircleBean.setCommentBeans(commentBeans);
                mFriendCircleAdapter.notifyItemChanged(feedPosition + mFriendCircleAdapter.headerCount(), Collections.singleton("comment"));
            }

            @Override
            public void onFailure(int errorCode) {
                // do nothing
            }
        });
    }

    @Override
    public void onCommentUserClick(String userId) {
//        Intent intent = new Intent(this, UserInfoActivity.class);
//        intent.putExtra("userInfo", ChatManager.Instance().getUserInfo(userId, false));
//        startActivity(intent);
        Intent intent = new Intent(this, FeedListActivity.class);
        intent.putExtra("userInfo", ChatManager.Instance().getUserInfo(userId, false));
        startActivity(intent);
    }

    @Override
    public void onFeedUserClick(String userId) {
        onCommentUserClick(userId);
    }

}
