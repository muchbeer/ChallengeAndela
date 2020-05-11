package muchbeer.raum.com.challengeandela.firebaseauth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nostra13.universalimageloader.core.ImageLoader;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.chatroom.ChatActivity;
import muchbeer.raum.com.challengeandela.chatroom.ChatRoomActivity;
import muchbeer.raum.com.challengeandela.messagefirebase.AdminActivity;
import muchbeer.raum.com.challengeandela.models.ChatRoom;
import muchbeer.raum.com.challengeandela.models.Users;
import muchbeer.raum.com.challengeandela.utility.FirebaseUtil;
import muchbeer.raum.com.challengeandela.utility.UniversalImageLoader;

public class SignedInActivity extends AppCompatActivity {


    private static final String TAG = SignedInActivity.class.getSimpleName();

    //vars
    public static boolean isActivityRunning;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    // widgets and UI References

    private Boolean mIsAdmin = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signedin);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d(TAG, "onCreate: started.");

        setupFirebaseAuth();

      //  getUserDetails();
        setUserDetails();
        initFCM();
        initImageLoader();
       // isAdmin();

        getPendingIntent();

    }

    //get intent plus detail from MyFirebaseMessagingService
    private void getPendingIntent(){
        Log.d(TAG, "getPendingIntent: checking for pending intents.");

        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.intent_chatroom))){
            Log.d(TAG, "getPendingIntent: pending intent detected.");

            //get the chatroom
            ChatRoom chatroom = intent.getParcelableExtra(getString(R.string.intent_chatroom));
            //navigate to the chatoom
            Intent chatroomIntent = new Intent(SignedInActivity.this, ChatRoomActivity.class);
            chatroomIntent.putExtra(getString(R.string.intent_chatroom), chatroom);
            startActivity(chatroomIntent);
        }
    }


    private void initImageLoader() {
        UniversalImageLoader imageLoader = new UniversalImageLoader(SignedInActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void initFCM(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String token = instanceIdResult.getToken();
                        Log.d(TAG, "initFCM: token: " + token);
                        sendRegistrationToserver(token);
                    }
                });


    }

    public void sendRegistrationToserver(String token) {

        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        reference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_messaging_token))
                .setValue(token);

    }


    private void setUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName("Muchbeer")
                    .setPhotoUri(Uri.parse("set url of photo"))
                    .build();

            user.updateProfile(profileUpdate)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: user profile updated");
                                getUserDetails();
                            }
                        }
                    });


        }

    }
    /*
           ----------------------------- Firebase setup ---------------------------------
        */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(SignedInActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                // ...
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    private void getUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            String uid = user.getUid();
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            String properties = "uid: " + uid + "  Name: "+ name;
            String photoPicture = photoUrl.toString();

        }

    }
    private void checkAuthenticationState() {
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(SignedInActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }

    private void isAdmin(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            private String securityLeveling;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: datasnapshot: " + dataSnapshot);

                           //    DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Users user = snapshot.getValue(Users.class);
                  securityLeveling = user.getSecurity_level();
                   Log.d(TAG, "onDataChange: The security level is:" + user.getSecurity_level());
                   int securityLevel = Integer.parseInt(securityLeveling);
                  if (securityLevel == 8) {
                        Log.d(TAG, "onDataChange: user is an admin.");
                        mIsAdmin = true;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
   //   getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optionSignOut:
                signOut();
                return true;

            case R.id.optionAccountSettings:
                Intent intent = new Intent(SignedInActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.optionChat2:
                intent = new Intent(SignedInActivity.this, ChatActivity.class);
                startActivity(intent);
                return true;

            case R.id.optionAdmin:
                intent = new Intent(SignedInActivity.this, AdminActivity.class);
                startActivity(intent);
             /*   if(mIsAdmin){
                    intent = new Intent(SignedInActivity.this, AdminActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(this, "You're not an Admin", Toast.LENGTH_SHORT).show();
                }*/

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /**
     * Sign out the current user
     */
    private void signOut(){
        Log.d(TAG, "signOut: signing out");
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
        isActivityRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityRunning = false;
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }
}
