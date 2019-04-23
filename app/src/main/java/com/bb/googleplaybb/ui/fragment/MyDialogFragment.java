package com.bb.googleplaybb.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bb.googleplaybb.R;
import com.bb.googleplaybb.ui.activity.MainActivity;
import com.bb.googleplaybb.utils.UIUtils;

/**
 * Created by Boby on 2019/1/8.
 */

public class MyDialogFragment extends DialogFragment implements View.OnClickListener{
    View mView;
    private Button mBtn_cancel;
    private View mBtn_modify;
    private EditText mEt_ip;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = UIUtils.inflate(R.layout.dialog_modify_ip);
        mBtn_cancel = mView.findViewById(R.id.btn_cancel);
        mBtn_modify = mView.findViewById(R.id.btn_modify);
        mEt_ip = mView.findViewById(R.id.et_ip);

        mBtn_cancel.setOnClickListener(this);
        mBtn_modify.setOnClickListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(UIUtils.getContext());
        dialog.setTitle("修改IP");
        dialog.setContentView(mView);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                Toast.makeText(UIUtils.getContext(),"点击了取消按钮",Toast.LENGTH_SHORT).show();
        }
    }
}
