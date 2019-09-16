package muchbeer.raum.com.challengeandela.messagefirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import muchbeer.raum.com.challengeandela.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_message);

        FirebaseMessaging.getInstance().subscribeToTopic("gianna").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "Successfull";
                if(!task.isSuccessful()) {
                    msg = "Failed";
                }
                Toast.makeText(getApplicationContext(), "msg", Toast.LENGTH_LONG).show();
              // Toast mssg
            }
        });



        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
//To do//
                            return;
                        }
                        Toast.makeText(getApplicationContext(), "This is happening now", Toast.LENGTH_SHORT).show();

                       /* Intent mainIntent = new Intent(this, muchbeer.raum.com.challengeandela.MainActivity.class);
                        startActivities(mainIntent);*/
// Get the Instance ID token//
                        String token = task.getResult().getToken();
                        String msg = getString(R.string.fcm_token, token);
                        Log.d(TAG, msg);

                    }
                });
    }
}
