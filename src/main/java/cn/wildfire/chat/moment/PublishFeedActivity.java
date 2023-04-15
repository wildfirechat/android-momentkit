package cn.wildfire.chat.moment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.contact.pick.PickContactActivity;
import cn.wildfire.chat.kit.widget.OptionItemView;
import cn.wildfire.chat.moment.third.widgets.NineGridView;
import cn.wildfire.chat.moment.thirdbar.BaseTitleBarActivity;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.moment.FeedContentType;
import cn.wildfirechat.moment.MomentClient;
import cn.wildfirechat.moment.model.Feed;
import cn.wildfirechat.moment.model.FeedEntry;
import cn.wildfirechat.remote.ChatManager;

public class PublishFeedActivity extends BaseTitleBarActivity implements NineGridView.OnImageClickListener {
    EditText editText;
    NineGridView nineGridView;
    OptionItemView mentionOptionItemView;
    OptionItemView visibleScopeOptionItemView;

    private FeedMediaContentAdapter nineGridAdapter;

    public static final String VIDEO_URL = "video_url";
    public static final String IMAGE_URLS = "image_urls";
    private String videoPath = null;

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int REQUEST_CODE_PICK_TO_MENTION = 101;
    private static final int REQUEST_CODE_VISIBLE_SCOPE = 102;

    private ArrayList<String> mentionUids = new ArrayList<>();
    private ArrayList<String> blockUids = new ArrayList<>();
    private ArrayList<String> toUids = new ArrayList<>();
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        bindViews();
        init();
    }

    private void bindViews() {
        editText = findViewById(R.id.publish_input);
        nineGridView = findViewById(R.id.preview_image_content);
        mentionOptionItemView = findViewById(R.id.mentionOptionItemView);
        visibleScopeOptionItemView = findViewById(R.id.visibleScopeOptionItemView);

        mentionOptionItemView.setOnClickListener(v -> mention());
        visibleScopeOptionItemView.setOnClickListener(v -> visibleScope());
    }


    private void init() {
        videoPath = getIntent().getStringExtra(VIDEO_URL);
        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra(IMAGE_URLS);
        if (!TextUtils.isEmpty(videoPath) || (imageUrls != null && !imageUrls.isEmpty())) {
            nineGridAdapter = new FeedMediaContentAdapter(this, new RequestOptions().centerCrop(), DrawableTransitionOptions.withCrossFade());
            if (videoPath != null) {
                nineGridAdapter.setMediaUrls(Collections.singletonList(videoPath), true);
            } else {
                nineGridAdapter.setMediaUrls(imageUrls, false);
            }

            nineGridView.setAdapter(nineGridAdapter);
            nineGridView.setOnImageClickListener(this);
        } else {
            setTitleLeftText("发表文字");
            nineGridView.setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean isTranslucentStatus() {
        return true;
    }

    @Override
    protected boolean isFitsSystemWindows() {
        return false;
    }

    void mention() {
        Intent intent = PickContactActivity.buildPickIntent(this, 0, mentionUids, blockUids);
        startActivityForResult(intent, REQUEST_CODE_PICK_TO_MENTION);
    }

    void visibleScope() {
        Intent intent = new Intent(this, FeedVisibleScopeActivity.class);
        intent.putExtra(FeedVisibleScopeActivity.PARAM_VISIBLE_MODE, mode);
        if (mode == FeedVisibleScopeActivity.VISIBLE_MODE_PART) {
            intent.putExtra(FeedVisibleScopeActivity.PARAM_UIDS, toUids);
        } else if (mode == FeedVisibleScopeActivity.VISIBLE_MODE_BLOCK) {
            intent.putExtra(FeedVisibleScopeActivity.PARAM_UIDS, blockUids);
        }
        startActivityForResult(intent, REQUEST_CODE_VISIBLE_SCOPE);
    }

    @Override
    public void onTitleRightClick() {
        publishFeed();
    }

    private void mentionUsers(List<UserInfo> userInfos) {
        mentionUids.clear();

        for (UserInfo userInfo : userInfos) {
            mentionUids.add(userInfo.uid);
        }
        if (userInfos.isEmpty()) {
            mentionOptionItemView.setDesc("");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 3 && i < userInfos.size(); i++) {
                sb.append(userInfos.get(i).displayName);
                sb.append("、");
            }
            sb.deleteCharAt(sb.length() - 1);
            if (userInfos.size() > 3) {
                sb.append("等");
            }
            mentionOptionItemView.setDesc(sb.toString());
        }
    }

    private void setFeedVisibleScope(int mode, List<UserInfo> userInfos) {
        this.mode = mode;
        blockUids.clear();
        toUids.clear();
        List<String> uids = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            uids.add(userInfo.uid);
        }
        visibleScopeOptionItemView.setTitle("谁可以看");
        switch (mode) {
            case FeedVisibleScopeActivity.VISIBLE_MODE_ALL:
                visibleScopeOptionItemView.setDesc("公开");
                break;
            case FeedVisibleScopeActivity.VISIBLE_MODE_PART:
                toUids.addAll(uids);
                visibleScopeOptionItemView.setDesc(buildUserNames(userInfos));
                break;
            case FeedVisibleScopeActivity.VISIBLE_MODE_BLOCK:
                blockUids.addAll(uids);
                visibleScopeOptionItemView.setTitle("谁不可看");
                visibleScopeOptionItemView.setDesc(buildUserNames(userInfos));
                break;
            case FeedVisibleScopeActivity.VISIBLE_MODE_PRIVATE:
                visibleScopeOptionItemView.setDesc("私密");
                toUids.add(ChatManager.Instance().getUserId());
                break;
            default:
                break;
        }
    }

    private String buildUserNames(List<UserInfo> userInfos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3 && i < userInfos.size(); i++) {
            sb.append(userInfos.get(i).displayName);
            sb.append("、");
        }
        sb.deleteCharAt(sb.length() - 1);
        if (userInfos.size() > 3) {
            sb.append("等");
        }
        return sb.toString();
    }

    @Override
    public void onImageClick(int position, View view) {
        if (position < nineGridAdapter.getImageUrls().size()) {
            // preview image
        } else {
            // add more image
            int count = 9 - nineGridAdapter.getImageUrls().size();
            Intent intent = ImagePicker.picker().showCamera(true).enableMultiMode(count).buildPickIntent(this);
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (data != null) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                List<String> imageUrls = nineGridAdapter.getImageUrls();
                for (ImageItem imageItem : images) {
                    imageUrls.add(imageItem.path);
                }

                nineGridAdapter.setMediaUrls(imageUrls, false);
                nineGridView.setAdapter(nineGridAdapter);
            }
        } else if (requestCode == REQUEST_CODE_PICK_TO_MENTION) {
            List<UserInfo> pickedUserInfos = data.getParcelableArrayListExtra(PickContactActivity.RESULT_PICKED_USERS);
            mentionUsers(pickedUserInfos);
        } else if (requestCode == REQUEST_CODE_VISIBLE_SCOPE) {
            int mode = data.getIntExtra(FeedVisibleScopeActivity.RESULT_VISIBLE_MODE, 0);
            List<UserInfo> userInfos = data.getParcelableArrayListExtra(FeedVisibleScopeActivity.RESULT_USERS);
            setFeedVisibleScope(mode, userInfos);
        }
    }

    // TODO 是否发送原图
    private void publishFeed() {

        if (TextUtils.isEmpty(editText.getText().toString()) && (nineGridAdapter == null || (videoPath == null && nineGridAdapter.getImageUrls().isEmpty()))) {
            Toast.makeText(this, "请输入想发表的内容~", Toast.LENGTH_SHORT).show();
            return;
        }
        Feed feed = new Feed();
        MaterialDialog dialog = new MaterialDialog.Builder(this)
            .progress(true, 100)
            .cancelable(false)
            .build();
        dialog.show();
        ChatManager.Instance().getWorkHandler().post(() -> {
            List<FeedEntry> feedEntries = new ArrayList<>();
            feed.medias = feedEntries;
            if (nineGridAdapter != null) {
                if (!TextUtils.isEmpty(videoPath) && !videoPath.endsWith(".png")) {
                    feed.type = FeedContentType.Content_Video_Type;
                    FeedEntry feedEntry = new FeedEntry();
                    String videoUrl = MomentClient.uploadMediaSync(videoPath);
                    if (videoUrl == null) {
                        toast("上传视频失败");
                        dialog.dismiss();
                        return;
                    }

                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MICRO_KIND);
                    try {
                        String thumbnailFilePath = Config.VIDEO_SAVE_DIR + File.pathSeparator + System.currentTimeMillis() + ".jpg";
                        FileOutputStream outputStream = new FileOutputStream(thumbnailFilePath);
                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                        outputStream.close();
                        String thumbnailUrl = MomentClient.uploadMediaSync(thumbnailFilePath);
                        if (thumbnailUrl != null) {
                            feedEntry.mediaUrl = videoUrl;
                            feedEntry.thumbUrl = thumbnailUrl;

                            feedEntry.mediaWidth = thumbnail.getWidth();
                            feedEntry.mediaHeight = thumbnail.getHeight();
                            feedEntries.add(feedEntry);
                        }
                    } catch (Exception e) {
                        toast("上传失败 " + e.getMessage());
                        dialog.dismiss();
                        return;
                    }
                } else if (nineGridAdapter.getImageUrls().size() > 0) {
                    feed.type = FeedContentType.Content_Image_Type;
                    for (String imagePath : nineGridAdapter.getImageUrls()) {
                        String imageUrl = MomentClient.uploadMediaSync(imagePath);
                        if (imageUrl != null) {
                            FeedEntry feedEntry = new FeedEntry();
                            feedEntry.mediaUrl = imageUrl;

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(videoPath, options);
                            int outHeight = options.outHeight;
                            feedEntry.mediaWidth = options.outWidth;
                            feedEntry.mediaHeight = outHeight;

                            feedEntries.add(feedEntry);
                        } else {
                            toast("上传图片失败");
                            dialog.dismiss();
                            return;
                        }
                    }
                }
            } else {
                feed.type = FeedContentType.Content_Text_Type;
            }

            feed.toUsers = toUids; // 谁可以看
            feed.excludeUsers = blockUids; // 谁不可以看
            feed.mentionedUser = mentionUids; // 提醒谁看
            feed.extra = null;
            feed.text = editText.getText().toString();

            MomentClient.getInstance().postFeed(feed, new MomentClient.PostCallback() {
                @Override
                public void onSuccess(long feedUid, long timestamp) {
                    if (PublishFeedActivity.this.isFinishing()) {
                        return;
                    }
                    dialog.dismiss();
                    feed.feedId = feedUid;
                    setResult(Activity.RESULT_OK);
                    finish();
                }

                @Override
                public void onFailure(int errorCode) {
                    if (PublishFeedActivity.this.isFinishing()) {
                        return;
                    }
                    Toast.makeText(PublishFeedActivity.this, "post error " + errorCode, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });
    }

    private void toast(String msg) {
        ChatManager.Instance().getMainHandler().post(() -> Toast.makeText(PublishFeedActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}
