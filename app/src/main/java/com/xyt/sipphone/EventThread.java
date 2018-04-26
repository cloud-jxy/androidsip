package com.xyt.sipphone;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;

import com.xyt.jni.Call;
import com.xyt.jni.SipEvent;
import com.xyt.jni.JniTools;
import com.xyt.sipphone.activity.DialActivity;
import com.xyt.sipphone.activity.MainActivity;
import com.xyt.sipphone.activity.SessionActivity;
import com.xyt.sqlite.model.Cdr;
import com.xyt.sqlite.dao.CdrRepo;

/**
 * Created by apple on 16/8/12.
 */
public class EventThread extends Thread {
    public static MainActivity mMain;
    public static SessionActivity mSess;
    public static Handler mHandler;
    boolean mLoopRun = false;
    @Override
    public void run() {
        /*
        防止同时多启动.
         */
        if (mLoopRun) {
            return;
        }

        Looper.prepare();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what != JniTools.SIP_EVENT) {
                    return;
                }

                Call call = (Call)msg.obj;
                System.out.println("handleMessage: msg=" + call.event);

                if (call.event == SipEvent.UA_EVENT_REGISTER_FAIL) {
                    DialActivity.title = "未注册";
                } else if (call.event == SipEvent.UA_EVENT_CALL_INCOMING) {
                    notify_incoming(call);
                } else if (call.event == SipEvent.UA_EVENT_CALL_CLOSED) {
                    insert(call);
                    mSess.hangup();

//
//                    /*
//                    UA_EVENT_CALL_CLOSED,UA_EVENT_CALL_ESTABLISHED处理比较麻烦,因为发生事件时,并非真的是开始/结束sip通话.
//                    如:对方拒接电话,baresip内部是这么结束自己的session——UA_EVENT_CALL_ESTABLISHED然后UA_EVENT_CALL_CLOSED.
//
//                    会话开始时间>0,表明以建立通话
//                    链接时间>0,表明自己作为主叫,且sip服务器以链接到被叫
//                    status=STATE_TERMINATED,表明是自己主动终止
//
//                    有个1个bug:自己做主角,对方挂机.status=5 start-time=1471934065 conn-time=1471934065 stop-time=1471934067.
//                    和通话中,对方挂机的数据完全雷同.明明是未拨通的电话,成了正常通话.
//                    简单的以通话<2s判定未被叫未接听.
//                     */
//                    if (call.start_time > 0 && (call.stop_time-call.start_time)>2) {
//                        // 处理通话中挂机
//                        if (call.status == SipStatus.STATE_ESTABLISHED) {
//                            //收到对方挂机消息
//                            notify_b_close(call);
//                        } else if (call.status == SipStatus.STATE_TERMINATED){
//                            //自己手动挂机
//                            notify_a_close(call);
//                        }
//                    } else if (call.start_time == 0) {
//                        //处理未接电话
//                        if (call.status == SipStatus.STATE_INCOMING) {
//                            //未接电话,对方挂机
//                            notify_unanswered(call);
//                        } else if (call.status == SipStatus.STATE_TERMINATED){
//                            //未接电话,自己拒接
//                            notify_refuse(call);
//                        }
//                    } else {
//                        //被叫未接听,之前提及的bug简单处理方式.
//                        notify_uncall(call);
//                    }
                } else if (call.event == SipEvent.UA_EVENT_CALL_ESTABLISHED) {
                    /*
                    自己拒接电话: 只触发UA_EVENT_CALL_CLOSED.
                    对方拒接电话: 触发UA_EVENT_CALL_ESTABLISHED UA_EVENT_CALL_CLOSED,此时stop_time!=0.: Msg: 9 status=5 start-time=1472003437 conn-time=1472003425 stop-time=1472003427
                    baresip在接听电话时,只触发UA_EVENT_CALL_ESTABLISHED,但stop_time也不为0.

                    用x-lite media5-fone都没做专门区分——界面上能短暂的看到通话建立然后结束,这里也不做区分处理了.
                    */
                    mSess.established();
                }
            }
        };
        Looper.loop();//4、启动消息循环
        mLoopRun = false;
    }

    private void insert(Call call) {
        Cdr record = new Cdr();
        record.local_url = call.local_url;
        record.peer_url = call.peer_url;
        record.start_time = call.start_time;
        record.conn_time = call.conn_time;
        record.stop_time = call.stop_time;
        record.status = call.status;

        CdrRepo repo = new CdrRepo(mMain);
        repo.insert(record);
    }

    private void notify_uncall(Call call) {
        String text = "I don't call to  " + call.peer_url;

        AlertDialog.Builder builder = new AlertDialog.Builder(mMain);
        builder.setMessage(text);
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /*
    提示:你拒接接电话
     */
    private void notify_refuse(Call call) {
        String text = "I don't want to answer the call from  " + call.peer_url;

        AlertDialog.Builder builder = new AlertDialog.Builder(mMain);
        builder.setMessage(text);
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /*
    提示,未接电话(不含,自己拒接的电话).
     */
    private void notify_unanswered(Call call) {
        String text = "There is an unanswered call from " + call.peer_url;

        AlertDialog.Builder builder = new AlertDialog.Builder(mMain);
        builder.setMessage(text);
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void notify_incoming(Call call) {
        Intent intent = new Intent(mMain, SessionActivity.class);
        intent.putExtra("number", call.peer_url);
        intent.putExtra("type", 0);
        mMain.startActivity(intent);
    }

    /*
    提示a-leg(自己)挂机
     */
    public void notify_a_close(Call call) {
        String text = "您已挂机,通话时间: " + (call.stop_time - call.start_time) + "s";

        System.out.println("java: close run!");
        AlertDialog.Builder builder = new AlertDialog.Builder(mMain);
        builder.setMessage(text);
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /*
    提示b-leg(对方)挂机
     */
    public void notify_b_close(Call call) {
        String text = "对方挂机,通话时间: " + (call.stop_time - call.start_time) + "s";

        System.out.println("java: close run!");
        AlertDialog.Builder builder = new AlertDialog.Builder(mMain);
        builder.setMessage(text);
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
