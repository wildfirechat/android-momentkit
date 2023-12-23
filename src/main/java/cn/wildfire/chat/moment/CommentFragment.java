package cn.wildfire.chat.moment;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cn.wildfire.chat.kit.widget.InputAwareLayout;
import cn.wildfire.chat.kit.widget.KeyboardAwareLinearLayout;
import cn.wildfire.chat.kit.R;

public class CommentFragment extends Fragment implements KeyboardAwareLinearLayout.OnKeyboardHiddenListener,
        KeyboardAwareLinearLayout.OnKeyboardShownListener {
    private InputAwareLayout inputAwareLayout;
    private FeedCommentPanel commentInputPanel;
    private EditText editText;

    private long feedId;
    private long commentId;

    private int commentPanelTop = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            feedId = bundle.getLong("feedId", 0);
            commentId = bundle.getLong("commentId", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.moment_comment_fragment, container, false);
        inputAwareLayout = view.findViewById(R.id.rootInputAwareLayout);
        inputAwareLayout.addOnKeyboardHiddenListener(this);
        inputAwareLayout.addOnKeyboardShownListener(this);
        commentInputPanel = view.findViewById(R.id.commentPanel);

        commentInputPanel.init(this, inputAwareLayout);
        editText = commentInputPanel.editText;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inputAwareLayout.showSoftkey(editText);
        commentPanelTop = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("commentPanelTop", 0);
    }

    @Override
    public void onKeyboardShown() {
        commentInputPanel.onKeyboardShown();
        if (commentPanelTop > 0) {
            return;
        }

        commentInputPanel.post(() -> {
            int[] location = new int[2];
            commentInputPanel.getLocationInWindow(location);
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putInt("commentPanelTop", location[1])
                    .apply();
        });
    }

    public void comment(String comment) {
        BaseFeedActivity activity = (BaseFeedActivity) getActivity();
        if (activity == null) {
            return;
        }
        if (feedId > 0 && commentId == 0) {
            activity.commentFeed(feedId, comment);
            return;
        }
        if (feedId > 0 && commentId > 0) {
            activity.replyComment(feedId, commentId, comment);
        }

    }

    public void hideKeyboard() {
        inputAwareLayout.hideSoftkey(editText, null);
    }

    @Override
    public void onKeyboardHidden() {
        commentInputPanel.onKeyboardHidden();
    }
}
