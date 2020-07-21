package cn.wildfire.chat.moment;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.third.utils.TimeUtils;
import cn.wildfire.chat.moment.third.interfaces.OnFeedUserClickListener;
import cn.wildfire.chat.moment.third.span.VerticalImageSpan;
import cn.wildfire.chat.kit.R;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.moment.FeedCommentType;
import cn.wildfirechat.moment.message.FeedCommentMessageContent;
import cn.wildfirechat.moment.message.FeedMessageContent;
import cn.wildfirechat.remote.ChatManager;

public class FeedMessageViewHolder extends RecyclerView.ViewHolder {
    private ImageView portraitImageView;
    private TextView nameTextViw;
    private TextView contentTextView;
    private TextView timeTextView;
    private ImageView descImageView;
    private TextView descTextView;

    private OnFeedUserClickListener onFeedUserClickListener;

    String userId;

    public FeedMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        portraitImageView = itemView.findViewById(R.id.messageSenderPortraitImageView);
        nameTextViw = itemView.findViewById(R.id.messageSenderNameTextView);
        contentTextView = itemView.findViewById(R.id.messageContentTextView);
        timeTextView = itemView.findViewById(R.id.messageTimeTextView);

        descImageView = itemView.findViewById(R.id.feedDescImageView);
        descTextView = itemView.findViewById(R.id.feedDescTextView);

        portraitImageView.setOnClickListener(v -> {
            if (onFeedUserClickListener != null) {
                onFeedUserClickListener.onFeedUserClick("");
            }
        });
    }

    public void setOnFeedUserClickListener(OnFeedUserClickListener onFeedUserClickListener) {
        this.onFeedUserClickListener = onFeedUserClickListener;
    }

    public void onBind(Message message) {
        MessageContent messageContent = message.content;
        String sender = "";
        if (messageContent instanceof FeedMessageContent) {
            FeedMessageContent feedMessageContent = (FeedMessageContent) messageContent;
            sender = feedMessageContent.getSender();
            contentTextView.setText("提到了我");
        } else if (messageContent instanceof FeedCommentMessageContent) {
            FeedCommentMessageContent feedCommentMessageContent = (FeedCommentMessageContent) messageContent;
            sender = feedCommentMessageContent.getSender();
            if (feedCommentMessageContent.getCommentType() == FeedCommentType.Comment_Thumbup_Type) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append("赞");
                builder.setSpan(new VerticalImageSpan(ContextCompat.getDrawable(itemView.getContext(), R.drawable.heart_drawable_blue)),
                        0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                contentTextView.setText(builder);
            } else {
                contentTextView.setText(feedCommentMessageContent.getText());
            }
        }
        UserInfo userInfo = ChatManager.Instance().getUserInfo(sender, false);
        userId = userInfo.uid;

        nameTextViw.setText(userInfo.displayName);
        GlideApp.with(itemView).load(userInfo.portrait).placeholder(R.mipmap.avatar_def).into(portraitImageView);
        timeTextView.setText(TimeUtils.getMsgFormatTime(message.serverTime));
    }

    // TODO
    private void bindFeed() {

    }
}
