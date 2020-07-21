package cn.wildfire.chat.moment.third.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cn.wildfire.chat.kit.R;

public class VisibleScopeViewHolder extends RecyclerView.ViewHolder {
    public TextView visibleScopeTextView;

    public VisibleScopeViewHolder(@NonNull View itemView) {
        super(itemView);
        visibleScopeTextView = itemView.findViewById(R.id.visibleScopeTextView);
    }
}
