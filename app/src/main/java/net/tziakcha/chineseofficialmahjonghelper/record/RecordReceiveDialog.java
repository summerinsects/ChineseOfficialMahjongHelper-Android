package net.tziakcha.chineseofficialmahjonghelper.record;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonConfirmDialog;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class RecordReceiveDialog extends AlertDialog {

    public interface OnReceiveListener {
        void onReceive(int total, ArrayList<RecordInfo> recordList);
    }

    private EditText mIPEdit;
    private Button mConnButton;
    private ProgressBar mProgressBar;
    private TextView mHintText;
    private boolean mReceiving = false;
    private Socket mSocket = null;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final OnReceiveListener mOnReceiveListener;

    public RecordReceiveDialog(@NonNull Context context, OnReceiveListener listener) {
        super(context);
        mOnReceiveListener = listener;
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_receive_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("点对点传输・接收");

        Button button = contentView.findViewById(R.id.dfl_btn_def);
        button.setText("取消");
        button.setOnClickListener(view -> onCancelButton());

        mIPEdit = contentView.findViewById(R.id.rrl_et_ip);
        mIPEdit.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.clearFocus();
                return true;
            }
            return false;
        });

        mConnButton = contentView.findViewById(R.id.rrl_btn_conn);
        mProgressBar = contentView.findViewById(R.id.rrl_pb);
        mHintText = contentView.findViewById(R.id.rrl_txt_hint);

        mConnButton.setOnClickListener(view -> onConnButton());

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            window.setLayout(context.getResources().getDisplayMetrics().widthPixels * 8 / 10,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // 监听返回键
        setOnKeyListener((dialogInterface, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                onCancelButton();
                return true;  // 返回true表示事件已处理
            }
            return false;  // 返回false表示事件未处理，将传递到下一个监听器
        });
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                int[] location = {0, 0};
                view.getLocationOnScreen(location);
                int left = location[0];
                int top = location[1];
                int right = left + view.getWidth();
                int bottom = top + view.getHeight();
                int x = (int)event.getRawX();
                int y = (int)event.getRawY();
                if (x < left || x > right || y < top || y > bottom) {
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void onCancelButton() {
        if (mReceiving) {
            new CommonConfirmDialog(getContext(),
                    "点对点传输・接收", "确定要终止接收数据？",
                    "终止", "继续", () -> {
                try {
                    if (mSocket != null) {
                        mSocket.close();
                        mSocket = null;
                    }
                } catch (Exception e) {
                    Utils.printDebugStackTrace(e);
                }
                dismiss();
            }).show();
        } else {
            dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    private void onConnButton() {
        String text = mIPEdit.getText().toString();
        if (text.isEmpty()) {
            return;
        }

        String[] res = text.split(":");
        if (res.length != 2) {
            return;
        }

        final String ip = res[0];
        final int port = Integer.parseUnsignedInt(res[1]);
        if (port == 0) {
            return;
        }

        mReceiving = true;
        new Thread(() -> {
            try {
                mSocket = new Socket(ip, port);
                if (mSocket.isConnected()) {
                    mHandler.post(() -> {
                        mProgressBar.setProgress(0);
                        mProgressBar.setVisibility(View.VISIBLE);
                        mConnButton.setVisibility(View.GONE);
                        mHintText.setText("连接成功，请在发送方上点击「发送」");
                    });

                    BufferedInputStream is = new BufferedInputStream(mSocket.getInputStream());
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                    // 数据以\0分隔，第一行是个数，其余各行是json
                    int total = 0;
                    int b;
                    while ((b = is.read()) != -1) {
                        if (b != 0) {
                            buffer.write(b);
                        } else {
                            String str = buffer.toString("UTF-8");
                            total = Integer.parseUnsignedInt(str);
                            buffer.reset();
                            break;
                        }
                    }

                    if (total > 0) {
                        final int finalTotal = total;
                        mHandler.post(() -> {
                            mProgressBar.setProgress(0);
                            mProgressBar.setMax(finalTotal);
                            mHintText.setText("正在接收数据，请勿退出程序。0/" + finalTotal);
                        });

                        ArrayList<RecordInfo> recordList = new ArrayList<>();
                        while ((b = is.read()) != -1) {
                            if (b == 0) {
                                String str = buffer.toString("UTF-8");
                                parseLine(str, recordList, total);
                                buffer.reset();
                            } else {
                                buffer.write(b);
                            }
                        }

                        if (buffer.size() > 0) {
                            String str = buffer.toString("UTF-8");
                            parseLine(str, recordList, total);
                        }

                        mHandler.post(() -> finishReceive(finalTotal, recordList));
                    }

                    is.close();
                } else {
                    mHandler.post(() -> mHintText.setText("连接失败"));
                }

                mSocket.close();
                mSocket = null;
            } catch (Exception e) {
                mHandler.post(this::reportError);
                Utils.printDebugStackTrace(e);
            }
        }).start();

        mHintText.setText("正在连接，请勿退出应用");
        mConnButton.setEnabled(false);
    }

    @SuppressLint("SetTextI18n")
    private void parseLine(String str, ArrayList<RecordInfo> recordList, int total) {
        boolean added = false;
        RecordInfo record = new RecordInfo();
        if (str.contains("\"version\":")) {
            if (RecordInfo.parseRecord(str, record)) {
                recordList.add(record);
                added = true;
            }
        } else {
            if (RecordInfo.parseRecordV0(str, record)) {
                recordList.add(record);
                added = true;
            }
        }

        if (added) {
            final int cnt = recordList.size();
            mHandler.post(() -> {
                mProgressBar.setProgress(cnt);
                mHintText.setText("正在接收数据，请勿退出程序。" + cnt + "/" + total);
            });
        }
    }

    private void finishReceive(int total, ArrayList<RecordInfo> recordList) {
        mReceiving = false;
        mOnReceiveListener.onReceive(total, recordList);
        dismiss();
    }

    private void reportError() {
        mOnReceiveListener.onReceive(0, null);
        dismiss();
    }

}
