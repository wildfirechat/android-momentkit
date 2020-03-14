package cn.wildfire.chat.moment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wildfire.chat.kit.contact.pick.PickContactActivity;
import cn.wildfire.chat.moment.thirdbar.BaseTitleBarActivity;
import cn.wildfirechat.chat.R;
import cn.wildfirechat.model.UserInfo;

public class FeedVisibleScopeActivity extends BaseTitleBarActivity {
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;

    private static final int REQUEST_CODE_PICK_TO_BLOCK = 100;
    private static final int REQUEST_CODE_PICK_TO_FETCH = 101;

    public static final String PARAM_VISIBLE_MODE = "mode";
    public static final String PARAM_UIDS = "users";

    public static final String RESULT_VISIBLE_MODE = "mode";
    public static final String RESULT_USERS = "users";

    public static final int VISIBLE_MODE_ALL = 0;
    public static final int VISIBLE_MODE_PRIVATE = 1;
    public static final int VISIBLE_MODE_PART = 2;
    public static final int VISIBLE_MODE_BLOCK = 3;

    private int mode = VISIBLE_MODE_ALL;
    private ArrayList<String> uids;
    private ArrayList<UserInfo> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_visible_scope_activity);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setTitleLeftText("谁可以看");
        Intent intent = getIntent();
        uids = intent.getStringArrayListExtra(PARAM_UIDS);
        if (uids == null) {
            uids = new ArrayList<>();
        }
        mode = intent.getIntExtra(PARAM_VISIBLE_MODE, 0);
        switch (mode) {
            case VISIBLE_MODE_ALL:
                radioGroup.check(R.id.allRadioButton);
                break;
            case VISIBLE_MODE_PRIVATE:
                radioGroup.check(R.id.privateRadioButton);
                break;
            case VISIBLE_MODE_PART:
                radioGroup.check(R.id.partRadioButton);
                break;
            case VISIBLE_MODE_BLOCK:
                radioGroup.check(R.id.blockRadioButton);
                break;
            default:
                break;

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

    @Override
    public void onTitleRightClick() {
        super.onTitleRightClick();
        Intent intent = new Intent();
        intent.putExtra(RESULT_VISIBLE_MODE, mode);
        intent.putExtra(RESULT_USERS, users);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.privateLinearLayout)
    void privateScope() {
        radioGroup.check(R.id.privateRadioButton);
        mode = VISIBLE_MODE_PRIVATE;
    }

    @OnClick(R.id.allLinearLayout)
    void allScope() {
        radioGroup.check(R.id.allRadioButton);
        mode = VISIBLE_MODE_ALL;
    }

    @OnClick(R.id.partLinearLayout)
    void partScope() {
        radioGroup.check(R.id.partRadioButton);
        Intent intent;
        if (mode == VISIBLE_MODE_PART) {
            intent = PickContactActivity.buildPickIntent(this, 0, uids, null);
        } else {
            uids.clear();
            intent = PickContactActivity.buildPickIntent(this, 0, null, null);
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_TO_FETCH);
        mode = VISIBLE_MODE_PART;
    }

    @OnClick(R.id.blockLinearLayout)
    void blockScope() {
        radioGroup.check(R.id.blockRadioButton);
        Intent intent;
        if (mode == VISIBLE_MODE_BLOCK) {
            intent = PickContactActivity.buildPickIntent(this, 0, uids, null);
        } else {
            uids.clear();
            intent = PickContactActivity.buildPickIntent(this, 0, null, null);
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_TO_BLOCK);
        mode = VISIBLE_MODE_BLOCK;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_PICK_TO_FETCH) {
            users = data.getParcelableArrayListExtra(PickContactActivity.RESULT_PICKED_USERS);
        } else if (requestCode == REQUEST_CODE_PICK_TO_BLOCK) {
            users = data.getParcelableArrayListExtra(PickContactActivity.RESULT_PICKED_USERS);
        }
        for (UserInfo info : users) {
            uids.add(info.uid);
        }
    }
}
