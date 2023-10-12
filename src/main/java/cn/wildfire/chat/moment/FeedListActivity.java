package cn.wildfire.chat.moment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.mm.TakePhotoActivity;
import cn.wildfire.chat.moment.third.adapters.HostViewHolder;
import cn.wildfire.chat.moment.third.beans.FriendCircleBean;
import cn.wildfire.chat.moment.third.utils.Utils;
import cn.wildfire.chat.moment.third.widgets.TitleBar;
import cn.wildfire.chat.moment.thirdbar.TitleBarAlphaChangeHelper;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.moment.MomentClient;
import cn.wildfirechat.moment.OnReceiveFeedMessageListener;
import cn.wildfirechat.moment.message.FeedCommentMessageContent;
import cn.wildfirechat.moment.model.Feed;
import cn.wildfirechat.moment.model.Profile;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.OnUserInfoUpdateListener;
import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollState;
import me.everything.android.ui.overscroll.IOverScrollStateListener;
import me.everything.android.ui.overscroll.IOverScrollUpdateListener;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class FeedListActivity extends BaseFeedActivity implements OnReceiveFeedMessageListener, OnUserInfoUpdateListener {

    private ImageView refreshImageView;
    private TextView unreadFeedMessageCountTextView;
    private RelativeLayout unreadFeedMessageCountRelativeLayout;

    private static final int REFRESH_STATE_IDLE = -1;
    private static final int REFRESH_STATE_REFRESHING = 0;
    private static final int REFRESH_STATE_RESETTING = 1;
    private int refreshState = -1;
    private static float REFRESH_IMAGE_VIEW_MAX_OFFSET = 280;

    private static final int REQUEST_CODE_RECORDER_VIDEO = 100;
    private static final int REQUEST_CODE_PICK_PHOTO = 101;
    private static final int REQUEST_CODE_PUBLISH_FEED = 102;
    private static final int REQUEST_CODE_PICK_PROFILE_BACKGROUND_PHOTO = 103;

    private boolean isLoadingOldFeed;
    private List<Feed> feeds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MomentClient.getInstance().setMomentMessageReceiveListener(this);
        ChatManager.Instance().addUserInfoUpdateListener(this);
    }

    @Override
    protected int contentLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        super.initView();
        View view = getLayoutInflater().inflate(R.layout.circle_host_header, recyclerView, false);
        view.setOnClickListener(v -> onUnreadFeedMessageCountClick());
        HostViewHolder headerViewHolder = new HostViewHolder(view);
        unreadFeedMessageCountRelativeLayout = view.findViewById(R.id.unreadFeedMessageCountRelativeLayout);
        unreadFeedMessageCountTextView = view.findViewById(R.id.unreadFeedMessageCountTextView);
        mFriendCircleAdapter.setHeaderViewHolder(headerViewHolder);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (!recyclerView.canScrollVertically(1)) {
                    int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
                    if (!isLoadingOldFeed && lastVisibleItem > mFriendCircleAdapter.getItemCount() - 3) {
                        loadOldFeeds();
                    }
                }
            }
        });
        refreshImageView = findViewById(R.id.refreshImageView);

        TitleBarAlphaChangeHelper.handle(getTitleBar(),
            recyclerView,
            headerViewHolder.friend_avatar,
            new TitleBarAlphaChangeHelper.OnTitleBarAlphaColorChangeListener() {
                @Override
                public void onChange(float alpha, int color) {
                    setStatusBarDark(alpha > 1);
                    setStatusBarHolderBackgroundColor(color);
                }
            });

        IOverScrollDecor decor = OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        decor.setOverScrollStateListener(new IOverScrollStateListener() {
            @Override
            public void onOverScrollStateChange(IOverScrollDecor decor, int oldState, int newState) {
                if (oldState == IOverScrollState.STATE_IDLE && newState == IOverScrollState.STATE_DRAG_START_SIDE) {
                    REFRESH_IMAGE_VIEW_MAX_OFFSET = Utils.calcStatusBarHeight(FeedListActivity.this) + titleBar.getHeight() + 80;
                }
            }
        });
        decor.setOverScrollUpdateListener(new IOverScrollUpdateListener() {
            @Override
            public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
                if (state == IOverScrollState.STATE_DRAG_START_SIDE) {
                    refreshImageView.setRotation(-offset * 2);
                    refreshImageView.setTranslationY(offset * 2 > REFRESH_IMAGE_VIEW_MAX_OFFSET ? REFRESH_IMAGE_VIEW_MAX_OFFSET : offset * 2);
                    return;
                }

                if (state == IOverScrollState.STATE_BOUNCE_BACK && offset > REFRESH_IMAGE_VIEW_MAX_OFFSET && refreshState == REFRESH_STATE_IDLE) {
                    loadFeeds();
                    return;
                }

                if (state == IOverScrollState.STATE_BOUNCE_BACK) {
                    if (refreshState == REFRESH_STATE_IDLE) {
                        refreshImageView.setRotation(-offset * 2);
                        refreshImageView.setTranslationY(offset * 2 > REFRESH_IMAGE_VIEW_MAX_OFFSET ? REFRESH_IMAGE_VIEW_MAX_OFFSET : offset * 2);
                    }
                }

            }
        });

        headerViewHolder.friend_wall_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateProfileBackground();
            }
        });

        mFriendCircleAdapter.setUserInfo(user);

        loadProfile();
        loadFeeds();
        updateUnreadFeedMessageCount();
    }

    private void showUpdateProfileBackground() {
        new MaterialDialog.Builder(this)
            .items(Arrays.asList("更新朋友圈背景"))
            .itemsCallback((dialog, itemView, position, text) -> {
                if (position == 0) {
                    pickProfileBackgroundImage();
                }
            })
            .build()
            .show();
    }

    private void loadProfile() {
        MomentClient.getInstance().getUserProfile(user == null ? ChatManager.Instance().getUserId() : user.uid, new MomentClient.UserProfileCallback() {
            @Override
            public void onSuccess(Profile profile) {
                if (isFinishing()) {
                    return;
                }
                mFriendCircleAdapter.setProfile(profile);
            }

            @Override
            public void onFailure(int errorCode) {

            }
        });

    }

    @Override
    protected void initTitleBar() {
        getTitleBar().getLeftTextView().setAlpha(0f);
        getTitleBar().setLeftText("朋友圈");
        setLeftTextColor(Color.parseColor("#040404"));
        if (user == null || user.uid.equals(ChatManager.Instance().getUserId())) {
            setTitleMode(TitleBar.TitleBarMode.MODE_BOTH);
            setTitleRightIcon(R.drawable.ic_camera);
        } else {
            setTitleMode(TitleBar.TitleBarMode.MODE_LEFT);
        }
        setTitleLeftIcon(R.drawable.back_left);
    }

    @Override
    public void onTitleDoubleClick() {
        recyclerView.smoothScrollToPosition(0);
        recyclerView.postDelayed(() -> loadFeeds(), 500);
    }

    private float refreshOffset;
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (refreshState == REFRESH_STATE_REFRESHING) {
                refreshOffset += 10;
                refreshImageView.setRotation(refreshOffset);
                refreshImageView.postDelayed(this, 100);
            }
        }
    };

    private void showRefreshing() {
        refreshImageView.setVisibility(View.VISIBLE);
        refreshImageView.setTranslationY(REFRESH_IMAGE_VIEW_MAX_OFFSET);
        refreshState = REFRESH_STATE_REFRESHING;
        refreshOffset = REFRESH_IMAGE_VIEW_MAX_OFFSET;
        refreshImageView.post(refreshRunnable);
    }

    private void resetRefreshing() {
        refreshState = REFRESH_STATE_RESETTING;
        refreshImageView.animate().translationY(0).withEndAction(() -> refreshState = REFRESH_STATE_IDLE).setDuration(1000).start();
    }

    @Override
    public void onFeedItemLongClick(View feedItemView, int feedPosition) {
        super.onFeedItemLongClick(feedItemView, feedPosition);
    }

    protected void loadFeeds() {
        showRefreshing();
        feeds = MomentClient.getInstance().restoreCache(user == null ? null : user.uid);
        if (!feeds.isEmpty()) {
            mFriendCircleAdapter.setFriendCircleBeans(feedsToFriendCircleBeans(feeds));
        }
        MomentClient.getInstance().getFeeds(0, 20, user == null ? null : user.uid, new MomentClient.GetFeedsCallback() {
            @Override
            public void onSuccess(List<Feed> feeds) {
                resetRefreshing();
                if (feeds == null || feeds.isEmpty()) {
                    mFriendCircleAdapter.showVisibleScopeItem();
                    return;
                }

                if (feeds.size() < 20) {
                    mFriendCircleAdapter.showVisibleScopeItem();
                }
                mFriendCircleAdapter.setFriendCircleBeans(feedsToFriendCircleBeans(feeds));
                mFriendCircleAdapter.notifyDataSetChanged();
                MomentClient.getInstance().storeCache(feeds, user == null ? null : user.uid);
                FeedListActivity.this.feeds = feeds;
            }

            @Override
            public void onFailure(int errorCode) {
                resetRefreshing();
            }
        });
    }

    private void loadOldFeeds() {
        List<FriendCircleBean> friendCircleBeans = mFriendCircleAdapter.getFriendCircleBeans();
        if (friendCircleBeans == null || friendCircleBeans.isEmpty()) {
            return;
        }
        isLoadingOldFeed = true;
        mFriendCircleAdapter.showLoadingOldFeedItem();
        long fromIndex = friendCircleBeans.get(friendCircleBeans.size() - 1).getId();

        MomentClient.getInstance().getFeeds(fromIndex, 20, user == null ? null : user.uid, new MomentClient.GetFeedsCallback() {
            @Override
            public void onSuccess(List<Feed> feeds) {
                if (feeds == null || feeds.isEmpty()) {
                    mFriendCircleAdapter.showVisibleScopeItem();
                    return;
                }
                mFriendCircleAdapter.hideLoadingOldFeedItem();
                mFriendCircleAdapter.addFriendCircleBeans(feedsToFriendCircleBeans(feeds));
                if (feeds.size() < 20) {
                    mFriendCircleAdapter.showVisibleScopeItem();
                }
                isLoadingOldFeed = false;
                FeedListActivity.this.feeds.addAll(feeds);
            }

            @Override
            public void onFailure(int errorCode) {
                mFriendCircleAdapter.hideLoadingOldFeedItem();
                isLoadingOldFeed = false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MomentClient.getInstance().setMomentMessageReceiveListener(null);
        ChatManager.Instance().removeUserInfoUpdateListener(this);
    }

    @Override
    public void onTitleRightClick() {
        new MaterialDialog.Builder(this)
            .items(Arrays.asList("拍摄", "从相册选取"))
            .itemsCallback((dialog, itemView, position, text) -> {
                if (position == 0) {
                    takeShortVideo();
                } else {
                    pickImageToPublish();
                }
            })
            .build()
            .show();
    }

    @Override
    public boolean onTitleRightLongClick() {
        Intent intent = new Intent(this, PublishFeedActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PUBLISH_FEED);
        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_RECORDER_VIDEO) {
            String path = data.getStringExtra("path");
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(this, "拍照错误, 请向我们反馈", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, PublishFeedActivity.class);
            intent.putExtra(PublishFeedActivity.VIDEO_URL, path);
            startActivityForResult(intent, REQUEST_CODE_PUBLISH_FEED);
        } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            if (images == null || images.isEmpty()) {
                Toast.makeText(this, "no image picked", Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<String> imageUrls = new ArrayList<>();
            for (ImageItem imageItem : images) {
                imageUrls.add(imageItem.path);
            }

            Intent intent = new Intent(this, PublishFeedActivity.class);
            intent.putExtra(PublishFeedActivity.IMAGE_URLS, imageUrls);
            startActivityForResult(intent, REQUEST_CODE_PUBLISH_FEED);
        } else if (requestCode == REQUEST_CODE_PICK_PROFILE_BACKGROUND_PHOTO) {
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
            if (images != null && !images.isEmpty()) {
                updateProfileBackground(images.get(0).path);
            }
        } else if (requestCode == REQUEST_CODE_PUBLISH_FEED) {
            loadFeeds();
        }
    }

    private void updateProfileBackground(String imagePath) {
        ChatManager.Instance().getWorkHandler().post(() -> {
            String imgUrl = MomentClient.uploadMediaSync(imagePath);
            MomentClient.getInstance().updateUserProfile(Profile.UpdateUserProfileType.UpdateUserProfileType_BackgroundUrl, imgUrl, 0, new MomentClient.GeneralCallback() {
                @Override
                public void onSuccess() {
                    if (isFinishing()) {
                        return;
                    }
                    loadProfile();
                }

                @Override
                public void onFailure(int errorCode) {
                    if (isFinishing()) {
                        return;
                    }
                    Toast.makeText(FeedListActivity.this, "上传文件失败 " + errorCode, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void pickImageToPublish() {
        Intent intent = ImagePicker.picker().showCamera(true).enableMultiMode(9).buildPickIntent(this);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
    }

    private void pickProfileBackgroundImage() {
        Intent intent = ImagePicker.picker().showCamera(true).enableMultiMode(1).buildPickIntent(this);
        startActivityForResult(intent, REQUEST_CODE_PICK_PROFILE_BACKGROUND_PHOTO);
    }

    private void takeShortVideo() {
        Intent intent = new Intent(this, TakePhotoActivity.class);
        startActivityForResult(intent, REQUEST_CODE_RECORDER_VIDEO);
    }

    @Override
    public void onReceiveFeedCommentMessage(Message feedCommentMessage) {
        updateUnreadFeedMessageCount();

        long feedId = ((FeedCommentMessageContent) feedCommentMessage.content).getFeedId();
        updateFeed(feedId);
    }

    @Override
    public void onReceiveFeedMessage(Message feedMessage) {
        updateUnreadFeedMessageCount();
    }


    private void updateFeed(long feedId) {
        if (this.feeds == null || this.feeds.isEmpty()) {
            // do nothing
        } else {
            int feedIndex = -1;
            for (int i = 0; i < feeds.size(); i++) {
                if (feeds.get(i).feedId == feedId) {
                    feedIndex = i;
                }
            }
            if (feedIndex > -1) {
                int finalFeedIndex = feedIndex;
                MomentClient.getInstance().getFeed(feedId, new MomentClient.GetFeedCallback() {
                    @Override
                    public void onSuccess(Feed feed) {
                        if (isFinishing()) {
                            return;
                        }
                        FriendCircleBean friendCircleBean = feedToFriendCircleBean(feed);
                        mFriendCircleAdapter.updateFriendCircleBean(finalFeedIndex, friendCircleBean);
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        if (isFinishing()) {
                            return;
                        }
                    }
                });
            }

        }
    }

    private void onUnreadFeedMessageCountClick() {
        Intent intent = new Intent(this, FeedMessageActivity.class);
        startActivity(intent);
        ChatManager.Instance().getMainHandler().post(() -> {
            unreadFeedMessageCountRelativeLayout.setVisibility(View.GONE);
            unreadFeedMessageCountRelativeLayout.setClickable(false);
        });
    }

    private void updateUnreadFeedMessageCount() {
        List<Message> messages = MomentClient.getInstance().getFeedMessages(0, true);
        if (messages.isEmpty()) {
            unreadFeedMessageCountRelativeLayout.setVisibility(View.GONE);
            unreadFeedMessageCountRelativeLayout.setClickable(false);
        } else {
            unreadFeedMessageCountRelativeLayout.setVisibility(View.VISIBLE);
            unreadFeedMessageCountTextView.setText("您有" + messages.size() + "条未读消息");
        }
    }

    @Override
    public void onUserInfoUpdate(List<UserInfo> userInfos) {
        mFriendCircleAdapter.updateUserInfo(userInfos);
    }
}
