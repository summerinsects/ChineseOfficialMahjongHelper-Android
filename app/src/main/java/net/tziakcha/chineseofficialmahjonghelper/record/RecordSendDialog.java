package net.tziakcha.chineseofficialmahjonghelper.record;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonConfirmDialog;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RecordSendDialog extends AlertDialog {

    public interface OnSendResultListener {
        void onSendResult(int count, int total);
    }

    private TextView mIPText;
    private Button mSendButton;
    private ProgressBar mProgressBar;
    private TextView mHintText;
    private final ArrayList<RecordInfo> mRecordList;
    private boolean mSending = false;
    private ServerSocket mServerSocket = null;
    private Socket mSocket = null;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ReentrantLock mLock = new ReentrantLock();
    private final Condition mCondition = mLock.newCondition();
    private final OnSendResultListener mOnSendResultListener;

    public RecordSendDialog(@NonNull Context context, final ArrayList<RecordInfo> recordList, OnSendResultListener listener) {
        super(context);
        mRecordList = recordList;
        mOnSendResultListener = listener;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_send_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("点对点传输・发送");

        Button button = contentView.findViewById(R.id.dfl_btn_def);
        button.setText("取消");
        button.setOnClickListener(view -> onCancelButton());

        mIPText = contentView.findViewById(R.id.rvl_txt_ip);
        mIPText.setTextColor(Common.COLOR_RED);
        mSendButton = contentView.findViewById(R.id.rvl_btn_send);
        mProgressBar = contentView.findViewById(R.id.rvl_pb);
        mHintText = contentView.findViewById(R.id.rvl_txt_hint);

        mSendButton.setOnClickListener(view -> onSendButton());
        mHintText.setText("等待连接，即将发送" + mRecordList.size() + "条记录");

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
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

        new Thread(() -> {
            try {
                mServerSocket = new ServerSocket(0, 1);

                String ipText = getLocalIP() + ":" + mServerSocket.getLocalPort();
                mHandler.post(() -> mIPText.setText(ipText));

                mSocket = mServerSocket.accept();
                if (mSocket != null) {
                    mHandler.post(() -> {
                        mSendButton.setEnabled(true);
                        mHintText.setText("连接成功，即将发送" + mRecordList.size() + "条记录");
                    });

                    mLock.lock();
                    try {
                        mCondition.await();

                        final int total = mRecordList.size();
                        mHandler.post(() -> {
                            mSending = true;
                            mProgressBar.setMax(total);
                            mProgressBar.setProgress(0);
                            mProgressBar.setVisibility(View.VISIBLE);
                            mSendButton.setVisibility(View.GONE);
                            mHintText.setText("正在发送数据，请勿退出程序。0/" + total);
                        });

                        final byte[] sep = {0};
                        BufferedOutputStream os = new BufferedOutputStream(mSocket.getOutputStream());
                        os.write(String.valueOf(total).getBytes(StandardCharsets.UTF_8));
                        os.write(sep);
                        os.flush();

                        int count = 0;
                        for (int i = 0; i < total; ++i) {
                            String str = RecordInfo.recordToString(mRecordList.get(i));
                            if (str != null) {
                                os.write(str.getBytes(StandardCharsets.UTF_8));
                                os.write(sep);
                                os.flush();

                                ++count;

                                int cnt = count;
                                mHandler.post(() -> {
                                    mProgressBar.setProgress(cnt);
                                    mHintText.setText("正在发送数据，请勿退出程序。" + cnt + "/" + total);
                                });
                            }
                        }

                        mSocket.close();
                        mSocket = null;

                        final int cnt = count;
                        mHandler.post(() -> finishSend(cnt, total));

                    } finally {
                        mLock.unlock();
                    }
                } else {
                    mHandler.post(() -> {
                        mSendButton.setEnabled(false);
                        mHintText.setText("连接失败，请退出本界面之后重试");
                    });
                }

                mServerSocket.close();
                mServerSocket = null;
            } catch (IOException | InterruptedException e) {
                mHandler.post(this::reportError);
                Utils.printDebugStackTrace(e);
            }
        }).start();
    }

    private void onCancelButton() {
        if (mSending) {
            new CommonConfirmDialog(getContext(),
                    "点对点传输・发送", "确定要终止发送数据？",
                    "终止", "继续", () -> {
                try {
                    if (mSocket != null) {
                        mSocket.close();
                        mSocket = null;
                    }
                    if (mServerSocket != null) {
                        mServerSocket.close();
                        mServerSocket = null;
                    }
                } catch (Exception e) {
                    Utils.printDebugStackTrace(e);
                }
            }).show();
        } else {
            dismiss();
        }
    }

    private void onSendButton() {
        mSendButton.setEnabled(false);
        mHintText.setText("正在发送数据，请勿退出程序。");

        mLock.lock();
        try {
            mCondition.signal();
        } finally {
            mLock.unlock();
        }
    }

    private void finishSend(int count, int total) {
        mSending = false;
        mOnSendResultListener.onSendResult(count, total);
        dismiss();
    }

    private void reportError() {
        mOnSendResultListener.onSendResult(0, 0);
        dismiss();
    }

    private static String getLocalIP() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Utils.printDebugStackTrace(e);
        }

        return null;
    }

}
