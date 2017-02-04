package com.imdanggui;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.imdanggui.util.Dlog;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.imdanggui.CommonUtilities.SENDER_ID;
import static com.imdanggui.CommonUtilities.displayMessage;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Dlog.d("========onRegistered=========");
        ServerUtilities.register(context, StartActivity.device, registrationId);
    }

    /**
     * Method called on device un registred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        //displayMessage(context, getString(R.string.gcm_unregistered));
        ServerUtilities.unregister(context, registrationId);
    }

    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
        String message = intent.getExtras().getString("price");
        String type = intent.getExtras().getString("type");
        String postId = intent.getExtras().getString("postId");
        //displayMessage(context, message);
        // notifies user
        Dlog.d("####postID### : " + String.valueOf(postId));
        Dlog.d("######" + message);
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        Boolean isRunning = false;
        Boolean isPushAct = false;
        Boolean isSettingAct = false;

        List<ActivityManager.RunningTaskInfo> taskInfos = activityManager.getRunningTasks(9999);
        for(int j = 0 ; j < taskInfos.size() ; j++){
            Dlog.d("##className##"+taskInfos.get(j).topActivity.getClassName());
            if( taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.MyPushListActivity") ) {
                isRunning = true;
                isPushAct = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.SettingActivity")){
                isRunning = true;
                isSettingAct = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.IconTabActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.MyPostListActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.MyReplyListActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.PostActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.PostDetailActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.PwdActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.AgreeActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.CategoryActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.CategoryDetailActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.CategoryPostActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.HelpActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.MainActivity")){
                isRunning = true;
            }else if(taskInfos.get(j).topActivity.getClassName().equals("com.imdanggui.StartActivity")){
                isRunning = true;
            }
        }
        if(isRunning == false){
            generateNotification(context, message ,type, postId);
        }else{
            if(isPushAct == true){
                Intent intent1 = new Intent();
                intent1.setAction("com.imdanggui.MyPushListActivity");
                sendBroadcast(intent1);
            }else if(isSettingAct == true){
                Intent intent1 = new Intent();
                intent1.setAction("com.imdanggui.SettingActivity");
                sendBroadcast(intent1);
            }
            if(message != null){
                showMessage(context, message);
            }


        }
        if(type != null){
            if(type.equals("reply")){
                SharedPreferences setting = getSharedPreferences("setting" , 0);
                SharedPreferences.Editor editor;
                editor = setting.edit();
                editor.putInt("pushcount" , setting.getInt("pushcount" , 0) + 1);
                editor.commit();
            }
        }

    }

    public void showMessage(final Context context, final String message){
        new Runnable() {
            @Override
            public void run() {
                Map<String, Object> map= new Hashtable<String, Object>();
                Message msg = new Message();
                msg.what = 0;
                map.put("message", message);
                map.put("context", context);
                msg.obj = map;
                handler.sendMessage(msg);
            }
        }.run();
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Map<String, Object> map = (Hashtable<String, Object>)msg.obj;
            String message = (String)map.get("message");
            Context context = (Context)map.get("context");
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        //String message = getString(com.imdanggui.R.string.gcm_deleted, total);
        //displayMessage(context, message);
        // notifies user
        //generateNotification(context, message);
    }

    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        //displayMessage(context, getString(R.string.gcm_error, errorId));
    }
    @Override
    protected boolean onRecoverableError(Context context, String errorId) {

        //displayMessage(context, getString(R.string.gcm_recoverable_error,
               // errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message , String type, String postId) {
        int icon = com.imdanggui.R.mipmap.icon;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        Dlog.d("#####postID######" + String.valueOf(postId));
        String title = context.getString(com.imdanggui.R.string.app_name);
        Intent notificationIntent = new Intent(context, StartActivity.class);
        if (type.equals("reply")){
            Dlog.d("#####reply######");
         notificationIntent.putExtra("postId" , String.valueOf(postId));
        }

        // set intent so it does not start a new activity
        /*notificationIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);*/

        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;
        //notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "your_sound_file_name.mp3");
        
        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);      

    }

}
