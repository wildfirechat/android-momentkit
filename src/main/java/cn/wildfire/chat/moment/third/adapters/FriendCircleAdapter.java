package cn.wildfire.chat.moment.third.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.mm.MMPreviewActivity;
import cn.wildfire.chat.moment.third.Constants;
import cn.wildfire.chat.moment.third.beans.CommentBean;
import cn.wildfire.chat.moment.third.beans.FriendCircleBean;
import cn.wildfire.chat.moment.third.beans.OtherInfoBean;
import cn.wildfire.chat.moment.third.beans.PraiseBean;
import cn.wildfire.chat.moment.third.beans.UserBean;
import cn.wildfire.chat.moment.third.interfaces.OnCommentItemClickListener;
import cn.wildfire.chat.moment.third.interfaces.OnCommentItemLongClickListener;
import cn.wildfire.chat.moment.third.interfaces.OnCommentUserClickListener;
import cn.wildfire.chat.moment.third.interfaces.OnFeedItemLongClickListener;
import cn.wildfire.chat.moment.third.interfaces.OnFeedUserClickListener;
import cn.wildfire.chat.moment.third.interfaces.OnPraiseOrCommentClickListener;
import cn.wildfire.chat.moment.third.utils.SpanUtils;
import cn.wildfire.chat.moment.third.utils.Utils;
import cn.wildfire.chat.moment.third.widgets.CommentOrPraisePopupWindow;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.moment.model.Profile;
import cn.wildfirechat.remote.ChatManager;

/**
 * @author KCrason
 * @date 2018/4/27
 */
public class FriendCircleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private LayoutInflater mLayoutInflater;

    private List<FriendCircleBean> mFriendCircleBeans;

    private RequestOptions mRequestOptions;

    private int mAvatarSize;

    private DrawableTransitionOptions mDrawableTransitionOptions;

    private CommentOrPraisePopupWindow mCommentOrPraisePopupWindow;

    private OnPraiseOrCommentClickListener mOnPraiseOrCommentClickListener;
    private OnCommentItemClickListener onCommentItemClickListener;
    private OnCommentItemLongClickListener onCommentItemLongClickListener;
    private OnFeedItemLongClickListener onFeedItemLongClickListener;
    private OnCommentUserClickListener onCommentUserClickListener;
    private OnFeedUserClickListener onFeedUserClickListener;

    private LinearLayoutManager mLayoutManager;

    private RecyclerView mRecyclerView;

    private HostViewHolder headerViewHolder;
    private Profile profile;
    private UserInfo userInfo;

    private int lastItemType = LAST_ITEM_TYPE_NORMAL;
    private static final int LAST_ITEM_TYPE_NORMAL = 0;
    private static final int LAST_ITEM_TYPE_LOADING = 1;
    private static final int LAST_ITEM_TYPE_VISIBLE_SCOPE = 2;
    private static final int LAST_ITEM_TYPE_BOTTOM_PADDING = 3;

    public FriendCircleAdapter(Context context, RecyclerView recyclerView) {
        this.mContext = context;
        mRecyclerView = recyclerView;
        this.mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        this.mAvatarSize = Utils.dp2px(44f);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mRequestOptions = new RequestOptions().centerCrop();
        this.mDrawableTransitionOptions = DrawableTransitionOptions.withCrossFade();
        if (context instanceof OnPraiseOrCommentClickListener) {
            this.mOnPraiseOrCommentClickListener = (OnPraiseOrCommentClickListener) context;
        }
    }

    public void setHeaderViewHolder(HostViewHolder headerViewHolder) {
        this.headerViewHolder = headerViewHolder;
    }

    public void setOnCommentItemClickListener(OnCommentItemClickListener onCommentItemClickListener) {
        this.onCommentItemClickListener = onCommentItemClickListener;
    }

    public void setOnCommentItemLongClickListener(OnCommentItemLongClickListener onCommentItemLongClickListener) {
        this.onCommentItemLongClickListener = onCommentItemLongClickListener;
    }

    public void setOnFeedItemLongClickListener(OnFeedItemLongClickListener onFeedItemLongClickListener) {
        this.onFeedItemLongClickListener = onFeedItemLongClickListener;
    }

    public void setOnCommentUserClickListener(OnCommentUserClickListener onCommentUserClickListener) {
        this.onCommentUserClickListener = onCommentUserClickListener;
    }

    public void setOnFeedUserClickListener(OnFeedUserClickListener onFeedUserClickListener) {
        this.onFeedUserClickListener = onFeedUserClickListener;
    }

    public void setFriendCircleBeans(List<FriendCircleBean> friendCircleBeans) {
        this.mFriendCircleBeans = friendCircleBeans;
        notifyDataSetChanged();
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        notifyItemChanged(0);
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        notifyItemChanged(0);
    }

    public List<FriendCircleBean> getmFriendCircleBeans() {
        return mFriendCircleBeans;
    }

    public int getFeedPosition(long feedId) {
        if (mFriendCircleBeans == null) {
            return -1;
        }
        for (int i = 0; i < mFriendCircleBeans.size(); i++) {
            if (mFriendCircleBeans.get(i).getId() == feedId) {
                return i;
            }
        }
        return -1;
    }

    public FriendCircleBean getFriendCircleBean(long feedId) {
        if (mFriendCircleBeans == null) {
            return null;
        }
        for (int i = 0; i < mFriendCircleBeans.size(); i++) {
            if (mFriendCircleBeans.get(i).getId() == feedId) {
                return mFriendCircleBeans.get(i);
            }
        }
        return null;
    }

    public CommentBean getCommentBean(long feedId, long commendId) {
        FriendCircleBean friendCircleBean = getFriendCircleBean(feedId);
        if (friendCircleBean == null) {
            return null;
        }
        List<CommentBean> commentBeans = friendCircleBean.getCommentBeans();
        if (commentBeans == null) {
            return null;
        }
        for (CommentBean bean : commentBeans) {
            if (bean.getId() == commendId) {
                return bean;
            }
        }
        return null;
    }

    public void showLoadingOldFeedItem() {
        if (lastItemType != LAST_ITEM_TYPE_NORMAL) {
            return;
        }
        int count = getItemCount();
        lastItemType = LAST_ITEM_TYPE_LOADING;
        notifyItemInserted(count);
    }

    public void hideLoadingOldFeedItem() {
        if (lastItemType != LAST_ITEM_TYPE_LOADING) {
            return;
        }
        int count = getItemCount();
        lastItemType = LAST_ITEM_TYPE_NORMAL;
        notifyItemRemoved(count);
    }

    public void showVisibleScopeItem() {
        if (lastItemType == LAST_ITEM_TYPE_VISIBLE_SCOPE) {
            return;
        }
        if (lastItemType == LAST_ITEM_TYPE_LOADING) {
            lastItemType = LAST_ITEM_TYPE_VISIBLE_SCOPE;
            notifyItemChanged(getItemCount() - 1);
        } else {
            int position = getItemCount();
            lastItemType = LAST_ITEM_TYPE_VISIBLE_SCOPE;
            notifyItemInserted(position);
        }
    }

    public void showBottomPadding() {
        if (lastItemType == LAST_ITEM_TYPE_BOTTOM_PADDING) {
            return;
        }
        int position = getItemCount();
        lastItemType = LAST_ITEM_TYPE_BOTTOM_PADDING;
        notifyItemInserted(position);
    }

    public void addFriendCircleBeans(List<FriendCircleBean> friendCircleBeans) {
        if (friendCircleBeans != null) {
            if (mFriendCircleBeans == null) {
                mFriendCircleBeans = new ArrayList<>();
            }
            this.mFriendCircleBeans.addAll(friendCircleBeans);
            notifyItemRangeInserted(mFriendCircleBeans.size(), friendCircleBeans.size());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if (viewType == Constants.FriendCircleType.FRIEND_CIRCLE_TYPE_HEADER) {
            return headerViewHolder;
        } else if (viewType == Constants.FriendCircleType.FRIEND_CIRCLE_TYPE_ONLY_WORD) {
            holder = new OnlyWordViewHolder(mLayoutInflater.inflate(R.layout.item_recycler_firend_circle_only_word, parent, false));
        } else if (viewType == Constants.FriendCircleType.FRIEND_CIRCLE_TYPE_WORD_AND_URL) {
            holder = new WordAndUrlViewHolder(mLayoutInflater.inflate(R.layout.item_recycler_firend_circle_word_and_url, parent, false));
        } else if (viewType == Constants.FriendCircleType.FRIEND_CIRCLE_TYPE_WORD_AND_IMAGES) {
            holder = new WordAndImagesViewHolder(mLayoutInflater.inflate(R.layout.item_recycler_firend_circle_word_and_images, parent, false));
        } else if (viewType == R.layout.feed_item_loading) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View itemView = layoutInflater.inflate(R.layout.feed_item_loading, parent, false);
            holder = new LoadingViewHolder(itemView);
        } else if (viewType == R.layout.feed_item_visible_scope) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View itemView = layoutInflater.inflate(R.layout.feed_item_visible_scope, parent, false);
            holder = new VisibleScopeViewHolder(itemView);
        } else if (viewType == R.layout.feed_item_bottom_padding) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View itemView = layoutInflater.inflate(R.layout.feed_item_bottom_padding, parent, false);
            holder = new BottomPaddingViewHolder(itemView);

        }
        if (holder instanceof BaseFriendCircleViewHolder) {
            ((BaseFriendCircleViewHolder) holder).verticalCommentWidget.setOnCommentItemClickListener(onCommentItemClickListener);
            ((BaseFriendCircleViewHolder) holder).verticalCommentWidget.setOnCommentItemLongClickListener(onCommentItemLongClickListener);
            ((BaseFriendCircleViewHolder) holder).verticalCommentWidget.setOnCommentUserClickListener(onCommentUserClickListener);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBindViewHolder(holder, position, false);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            onBindViewHolder(holder, position, true);
        }
    }

    private void onBindViewHolder(RecyclerView.ViewHolder holder, int position, boolean onlyUpdatePraiseOrComment) {
        if (holder instanceof BaseFriendCircleViewHolder) {
            // header
            position -= headerCount();
            if (mFriendCircleBeans != null && position < mFriendCircleBeans.size()) {
                FriendCircleBean friendCircleBean = mFriendCircleBeans.get(position);
                makeUserBaseData((BaseFriendCircleViewHolder) holder, friendCircleBean, position, onlyUpdatePraiseOrComment, onlyUpdatePraiseOrComment);
                if (holder instanceof OnlyWordViewHolder) {
                    OnlyWordViewHolder onlyWordViewHolder = (OnlyWordViewHolder) holder;
                } else if (holder instanceof WordAndUrlViewHolder) {
                    WordAndUrlViewHolder wordAndUrlViewHolder = (WordAndUrlViewHolder) holder;
                    wordAndUrlViewHolder.layoutUrl.setOnClickListener(v -> Toast.makeText(mContext, "You Click Layout Url", Toast.LENGTH_SHORT).show());
                } else if (holder instanceof WordAndImagesViewHolder) {
                    WordAndImagesViewHolder wordAndImagesViewHolder = (WordAndImagesViewHolder) holder;
                    wordAndImagesViewHolder.nineGridView.setOnImageClickListener((position1, view) ->
                    MMPreviewActivity.startActivity(mContext, friendCircleBean.getMediaEntries(), position1));
                    wordAndImagesViewHolder.nineGridView.setAdapter(new NineImageAdapter(mContext, mRequestOptions,
                            mDrawableTransitionOptions, friendCircleBean.getMediaEntries()));
                }
            }
        } else if (holder instanceof HostViewHolder) {
            // header
            ((HostViewHolder) holder).bind(mContext, userInfo, profile);
        }
    }

    private void makeUserBaseData(BaseFriendCircleViewHolder holder, FriendCircleBean friendCircleBean, int position, boolean onlyPraise, boolean onlyComment) {
        if (!onlyPraise && !onlyComment) {
            holder.txtContent.setText(friendCircleBean.getContentSpan());
            setContentShowState(holder, friendCircleBean);
            holder.txtContent.setOnLongClickListener(v -> {
                if (onFeedItemLongClickListener != null) {
                    onFeedItemLongClickListener.onFeedItemLongClick(v, position);
                }
                return true;
            });

            UserBean userBean = friendCircleBean.getUserBean();
            if (userBean != null) {
                holder.txtUserName.setText(userBean.getUserName());
                holder.txtUserName.setOnClickListener(v -> {
                    if (onFeedUserClickListener != null) {
                        onFeedUserClickListener.onFeedUserClick(userBean.getUserId());
                    }
                });
                holder.imgAvatar.setOnClickListener(v -> {
                    if (onFeedUserClickListener != null) {
                        onFeedUserClickListener.onFeedUserClick(userBean.getUserId());
                    }
                });
                Glide.with(mContext).load(userBean.getUserAvatarUrl())
                        .apply(mRequestOptions.override(mAvatarSize, mAvatarSize))
                        .transition(mDrawableTransitionOptions)
                        .into(holder.imgAvatar);
            }

            OtherInfoBean otherInfoBean = friendCircleBean.getOtherInfoBean();

            if (otherInfoBean != null) {
                holder.txtSource.setText(otherInfoBean.getSource());
                holder.txtPublishTime.setText(otherInfoBean.getTime());
            }
            holder.txtLocation.setOnClickListener(v -> Toast.makeText(mContext, "You Click Location", Toast.LENGTH_SHORT).show());

            holder.imgPraiseOrComment.setOnClickListener(v -> {
                if (mContext instanceof Activity) {
                    if (mCommentOrPraisePopupWindow == null) {
                        mCommentOrPraisePopupWindow = new CommentOrPraisePopupWindow(mContext);
                    }
                    List<PraiseBean> praiseBeans = friendCircleBean.getPraiseBeans();
                    boolean like = false;
                    if (praiseBeans != null) {
                        for (PraiseBean praiseBean : praiseBeans) {
                            if (praiseBean.getPraiseUserId().equals(ChatManager.Instance().getUserId())) {
                                like = true;
                                break;
                            }
                        }
                    }
                    mCommentOrPraisePopupWindow
                            .setOnPraiseOrCommentClickListener(mOnPraiseOrCommentClickListener)
                            .setCurrentPosition(position);
                    mCommentOrPraisePopupWindow.setLiked(like);
                    if (mCommentOrPraisePopupWindow.isShowing()) {
                        mCommentOrPraisePopupWindow.dismiss();
                    } else {
                        mCommentOrPraisePopupWindow.showPopupWindow(v);
                    }
                }
            });
        }

        if (friendCircleBean.isShowPraise() || friendCircleBean.isShowComment()) {
            holder.layoutPraiseAndComment.setVisibility(View.VISIBLE);
            if (friendCircleBean.isShowComment() && friendCircleBean.isShowPraise()) {
                holder.viewLine.setVisibility(View.VISIBLE);
            } else {
                holder.viewLine.setVisibility(View.GONE);
            }
            if (friendCircleBean.isShowPraise()) {
                friendCircleBean.setPraiseSpan(SpanUtils.makePraiseSpan(mContext, friendCircleBean.getPraiseBeans(), onCommentUserClickListener));
                holder.txtPraiseContent.setVisibility(View.VISIBLE);
                holder.txtPraiseContent.setText(friendCircleBean.getPraiseSpan());
            } else {
                holder.txtPraiseContent.setVisibility(View.GONE);
            }
            if (friendCircleBean.isShowComment()) {
                holder.verticalCommentWidget.setVisibility(View.VISIBLE);
                holder.verticalCommentWidget.addComments(position, friendCircleBean.getCommentBeans(), false);
            } else {
                holder.verticalCommentWidget.setVisibility(View.GONE);
            }
        } else {
            holder.layoutPraiseAndComment.setVisibility(View.GONE);
        }

    }

    private void setContentShowState(BaseFriendCircleViewHolder holder, FriendCircleBean friendCircleBean) {
        if (friendCircleBean.isShowCheckAll()) {
            holder.txtState.setVisibility(View.VISIBLE);
            setTextState(holder, friendCircleBean.isExpanded());
            holder.txtState.setOnClickListener(v -> {
                if (friendCircleBean.isExpanded()) {
                    friendCircleBean.setExpanded(false);
                } else {
                    friendCircleBean.setExpanded(true);
                }
                setTextState(holder, friendCircleBean.isExpanded());
            });
        } else {
            holder.txtState.setVisibility(View.GONE);
            holder.txtContent.setMaxLines(Integer.MAX_VALUE);
        }
    }

    private void setTextState(BaseFriendCircleViewHolder holder, boolean isExpand) {
        if (isExpand) {
            holder.txtContent.setMaxLines(Integer.MAX_VALUE);
            holder.txtState.setText("收起");
        } else {
            holder.txtContent.setMaxLines(4);
            holder.txtState.setText("全文");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (headerCount() > 0 && position == 0) {
            return Constants.FriendCircleType.FRIEND_CIRCLE_TYPE_HEADER;
        } else if (mFriendCircleBeans != null && position < headerCount() + feedCount()) {
            return mFriendCircleBeans.get(position - headerCount()).getViewType();
        } else {
            int type = 0;
            switch (lastItemType) {
                case LAST_ITEM_TYPE_LOADING:
                    type = R.layout.feed_item_loading;
                    break;
                case LAST_ITEM_TYPE_VISIBLE_SCOPE:
                    type = R.layout.feed_item_visible_scope;
                    break;
                case LAST_ITEM_TYPE_BOTTOM_PADDING:
                    type = R.layout.feed_item_bottom_padding;
                    break;
                default:
                    break;
            }
            return type;
        }
    }

    public int headerCount() {
        return headerViewHolder == null ? 0 : 1;
    }

    private int footerCount() {
        return lastItemType != LAST_ITEM_TYPE_NORMAL ? 1 : 0;
    }

    private int feedCount() {
        return mFriendCircleBeans == null ? 0 : mFriendCircleBeans.size();
    }

    @Override
    public int getItemCount() {
        return headerCount() + feedCount() + footerCount();
    }
}
