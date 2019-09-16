package muchbeer.raum.com.challengeandela.utility;

import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import muchbeer.raum.com.challengeandela.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String LOG_TAG = MyFirebaseMessagingService.class.getSimpleName();

    private DatabaseReference mDatabaseReference;
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

        String notificationTitle = "";
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
 showNotification(notificationTitle, notificationBody);
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
    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
