package cn.wildfire.chat.moment.third.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.mm.MediaEntry;
import cn.wildfire.chat.kit.third.utils.ImageUtils;
import cn.wildfire.chat.moment.third.utils.Utils;
import cn.wildfire.chat.moment.third.widgets.NineGridView;

/**
 * @author KCrason
 * @date 2018/4/27
 */
public class NineImageAdapter implements NineGridView.NineGridAdapter<MediaEntry> {

    private NineGridView nineGridView;
    private List<MediaEntry> mImageBeans;

    private Context mContext;

    private RequestOptions mRequestOptions;

    private DrawableTransitionOptions mDrawableTransitionOptions;


    public NineImageAdapter(Context context, NineGridView nineGridView, RequestOptions requestOptions, DrawableTransitionOptions drawableTransitionOptions, List<MediaEntry> imageBeans) {
        this.mContext = context;
        this.nineGridView = nineGridView;
        this.mDrawableTransitionOptions = drawableTransitionOptions;
        this.mImageBeans = imageBeans;
        int itemSize = (Utils.getScreenWidth() - 2 * Utils.dp2px(4) - Utils.dp2px(54)) / 3;
        this.mRequestOptions = requestOptions.override(itemSize, itemSize);
    }

    @Override
    public int getCount() {
        return mImageBeans == null ? 0 : mImageBeans.size();
    }

    @Override
    public MediaEntry getItem(int position) {
        return mImageBeans == null ? null :
            position < mImageBeans.size() ? mImageBeans.get(position) : null;
    }

    @Override
    public View getView(int position, View itemView) {
        ImageView imageView;
        if (itemView == null) {
            imageView = new ImageView(mContext);
            imageView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.base_F2F2F2));
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER);
        } else {
            imageView = (ImageView) itemView;
        }
        RequestOptions requestOptions = mRequestOptions;
        if (mImageBeans.size() == 1) {
            MediaEntry entry = mImageBeans.get(0);
            if (entry.getWidth() > 0) {
                int[] size = ImageUtils.scaleDown(entry.getWidth(), entry.getHeight(), nineGridView.getSingleImageMaxWidth(), 720);
                requestOptions = mRequestOptions.clone().override(size[0], size[1]);
            } else {
                requestOptions = mRequestOptions.clone().override(480, 720);
            }
        }
        MediaEntry entry = mImageBeans.get(position);
        Glide.with(mContext).load(entry.getMediaUrl()).apply(requestOptions).transition(mDrawableTransitionOptions).into(imageView);
        return imageView;
    }
}
