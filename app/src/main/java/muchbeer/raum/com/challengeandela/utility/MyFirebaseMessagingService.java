package muchbeer.raum.com.challengeandela.utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.chatroom.ChatActivity;
import muchbeer.raum.com.challengeandela.chatroom.ChatRoomActivity;
import muchbeer.raum.com.challengeandela.firebaseauth.LoginActivity;
import muchbeer.raum.com.challengeandela.firebaseauth.Register_User;
import muchbeer.raum.com.challengeandela.firebaseauth.SettingsActivity;
import muchbeer.raum.com.challengeandela.firebaseauth.SignedInActivity;
import muchbeer.raum.com.challengeandela.messagefirebase.AdminActivity;
import muchbeer.raum.com.challengeandela.models.ChatRoom;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String LOG_TAG = MyFirebaseMessagingService.class.getSimpleName();
    private static final int BROADCAST_NOTIFICATION_ID = 1;

    private int mNumPendingMessages = 0;
    private DatabaseReference mDatabaseReference;

    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    // Notification manager.
    public static final String NOTIFICATION_CHANNEL_ID = "channel_id";

    NotificationManager mNotifyManager;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.d(LOG_TAG + "  NEW_TOKEN IS: ", newToken);
                sendRegistrationToserver(newToken);
            }
        });
    }

    public void sendRegistrationToserver(String token) {

        Log.d(LOG_TAG, "sendRegistrationToServer: sending token to server: " + token);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        mDatabaseReference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_messaging_token))
                .setValue(token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        //	String notificationBody = "";
//	String notificationTitle = "";
//	String notificationData = "";
//	try{
//	   notificationData = remoteMessage.getData().toString();
//	   notificationTitle = remoteMessage.getNotification().getTitle();
//	   notificationBody = remoteMessage.getNotification().getBody();
//	}catch (NullPointerException e){
//	   Log.e(TAG, "onMessageReceived: NullPointerException: " + e.getMessage() );
//	}
//	Log.d(TAG, "onMessageReceived: data: " + notificationData);
//	Log.d(TAG, "onMessageReceived: notification body: " + notificationBody);
//	Log.d(TAG, "onMessageReceived: notification title: " + notificationTitle);


        //init image loader since this will be the first code that executes if they click a notification
        initImageLoader();

        String identifyDataType = remoteMessage.getData().get(getString(R.string.data_type));
        //SITUATION: Application is in foreground then only send priority notificaitons such as an admin notification
        if (isApplicationInForeground()) {
            if (identifyDataType.equals(getString(R.string.data_type_admin_broadcast))) {
                //build admin broadcast notification
                String title = remoteMessage.getData().get(getString(R.string.data_title));
                String message = remoteMessage.getData().get(getString(R.string.data_message));
                sendBroadcastNotification(title, message);
            }
        }

        //SITUATION: Application is in background or closed
        else if (!isApplicationInForeground()) {
            if (identifyDataType.equals(getString(R.string.data_type_admin_broadcast))) {
                //build admin broadcast notification
                String title = remoteMessage.getData().get(getString(R.string.data_title));
                String message = remoteMessage.getData().get(getString(R.string.data_message));
                String data = remoteMessage.getData().get("data_type_admin_broadcast");

                sendBroadcastNotification(title, message);
                Log.d(LOG_TAG, "Title to be send is: " + title);
                Log.d(LOG_TAG, "message to be received is : " + message);
                Log.d(LOG_TAG, "tHE data received: " + data);

            } else if (identifyDataType.equals(getString(R.string.data_type_chat_message))) {

                Log.d(LOG_TAG, "This has entered the chat zone: ");
                Log.d(LOG_TAG, "The size of data remote is: " + remoteMessage.getData().size());
                // Check if message contains a data payload.
                // if (remoteMessage.getData().size() > 0) {
                Log.d(LOG_TAG, "Message data payload: " + remoteMessage.getData());

                final String title = remoteMessage.getData().get(getString(R.string.data_title));
                final String message = remoteMessage.getData().get(getString(R.string.data_message));
                String chatroomId = remoteMessage.getData().get(getString(R.string.data_chatroom_id));
                Log.d(LOG_TAG, "onMessageReceived: title: " + title);
                Log.d(LOG_TAG, "onMessageReceived: message: " + message);
                Log.d(LOG_TAG, "onMessageReceived: chatroom id: " + chatroomId);

                Query query = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_chatrooms))
                        .orderByKey()
                        .equalTo(chatroomId);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getChildren().iterator().hasNext()) {
                            DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();

                            ChatRoom chatroom = new ChatRoom();
                            Map<String, Object> objectMap = (HashMap<String, Object>) snapshot.getValue();

                            chatroom.setChatroom_id(objectMap.get(getString(R.string.field_chatroom_id)).toString());
                            chatroom.setChatroom_name(objectMap.get(getString(R.string.field_chatroom_name)).toString());
                            chatroom.setCreator_id(objectMap.get(getString(R.string.field_creator_id)).toString());
                            chatroom.setSecurity_level(objectMap.get(getString(R.string.field_security_level)).toString());

                            Log.d(LOG_TAG, "onDataChange using : chatroom: " + chatroom);
                            Log.d(LOG_TAG, "onDataChange using : HashObject Map: " + objectMap);

                            int numMessagesSeen = Integer.parseInt(snapshot
                                    .child(getString(R.string.field_users))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(getString(R.string.field_last_message_seen))
                                    .getValue().toString());

                            int numMessages = (int) snapshot
                                    .child(getString(R.string.field_chatroom_messages)).getChildrenCount();

                            mNumPendingMessages = (numMessages - numMessagesSeen);
                            Log.d(LOG_TAG, "onDataChange: num pending messages: " + mNumPendingMessages);


                            sendChatmessageNotification(title, message, chatroom);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }//build chat message notification
            else {
                Log.d(LOG_TAG, "Fail to get the message to load notification: ");
            }

    }

/*        String notificationTitle = "";
        String notificationBody = "";
        String notificationData="";

        try{
notificationData = remoteMessage.getData().toString();
notificationBody = remoteMessage.getNotification().getBody();
notificationTitle = remoteMessage.getNotification().getTitle();

        }catch (NullPointerException e) {
            Log.d(LOG_TAG, "NullPointException error is: "+ e.getMessage());
        }

      Log.d(LOG_TAG, "Notification Data is: "+ notificationData);
        Log.d(LOG_TAG, "Notification Body is: "+ notificationBody);
        Log.d(LOG_TAG, "Notification Title is: "+ notificationTitle);
 showNotification(notificationTitle, notificationBody);*/
}


    private boolean isApplicationInForeground(){
        //check all the activities to see if any of them are running
        boolean isActivityRunning = SignedInActivity.isActivityRunning
                || ChatActivity.isActivityRunning || AdminActivity.isActivityRunning
                || ChatRoomActivity.isActivityRunning || LoginActivity.isActivityRunning
                || Register_User.isActivityRunning || SettingsActivity.isActivityRunning;
        if(isActivityRunning) {
            Log.d(LOG_TAG, "isApplicationInForeground: application is in foreground.");
            return true;
        }
        Log.d(LOG_TAG, "isApplicationInForeground: application is in background or closed.");    return false;
    }

    /**
     * init universal image loader
     */
    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private int buildNotificationId(String id){
        Log.d(LOG_TAG, "buildNotificationId: building a notification id.");

        int notificationId = 0;
        for(int i = 0; i < 9; i++){
            notificationId = notificationId + id.charAt(0);
        }
        Log.d(LOG_TAG, "buildNotificationId: id: " + id);
        Log.d(LOG_TAG, "buildNotificationId: notification id:" + notificationId);
        return notificationId;
    }

    /**
     * Build a push notification for an Admin Broadcast
     * @param title
     * @param message
     */
    private void sendBroadcastNotification(String title, String message){
        Log.d(LOG_TAG, "sendBroadcastNotification: building a admin broadcast notification");

        createNotificationChannel();


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Intent notifyIntent = new Intent(this, SignedInActivity.class);

        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.tabian_consulting_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.tabian_consulting_logo))
                .setColor(getColor(R.color.blue4))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(notifyPendingIntent);

        Notification notification = builder.build();

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);

        mNotificationManager.notify(BROADCAST_NOTIFICATION_ID, notification);
    }

    /**
     * Build a push notification for a chat message
     * @param title
     * @param message
     */
    private void sendChatmessageNotification(String title, String message, ChatRoom chatroom){
        Log.d(LOG_TAG, "sendChatmessageNotification: building a chatmessage notification");

        createNotificationChannel();

        int notificationId = buildNotificationId(chatroom.getChatroom_id());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        Intent pendingIntent = new Intent(this, SignedInActivity.class);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent.putExtra(getString(R.string.intent_chatroom), chatroom);

        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        builder.setSmallIcon(R.drawable.tabian_consulting_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.tabian_consulting_logo))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText("New messages in " + chatroom.getChatroom_name())
                .setColor(getColor(R.color.blue4))
                .setAutoCancel(true)
                .setSubText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("New messages in " + chatroom.getChatroom_name()).setSummaryText(message))
                .setNumber(mNumPendingMessages)
                .setOnlyAlertOnce(true)
                .setContentIntent(notifyPendingIntent);

        Notification notification = builder.build();
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        mNotificationManager.notify(BROADCAST_NOTIFICATION_ID, notification);
   }
    public void showNotification(String title, String message) {

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "ccmChannel";
            String description = "This notification is for campaign";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
    }
    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
