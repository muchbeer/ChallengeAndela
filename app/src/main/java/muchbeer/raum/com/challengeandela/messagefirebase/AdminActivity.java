package muchbeer.raum.com.challengeandela.messagefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.adapter.EmployeesAdapter;
import muchbeer.raum.com.challengeandela.dialog.NewDepartmentDialog;
import muchbeer.raum.com.challengeandela.fcm.Data;
import muchbeer.raum.com.challengeandela.fcm.FirebaseCloudMessage;
import muchbeer.raum.com.challengeandela.models.ServerKey;
import muchbeer.raum.com.challengeandela.models.Users;
import muchbeer.raum.com.challengeandela.utility.FCM;
import muchbeer.raum.com.challengeandela.utility.VerticalSpacingDecorator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class AdminActivity extends AppCompatActivity {

    private static final String TAG = AdminActivity.class.getSimpleName();
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/";

    //widgets
    private TextView mDepartments;
    private Button mAddDepartment, mSendMessage;
    private RecyclerView mRecyclerView;
    private EditText mMessage, mTitle;

    //vars
    private ArrayList<String> mDepartmentsList;
    private Set<String> mSelectedDepartments;
    private EmployeesAdapter mEmployeeAdapter;
    private ArrayList<Users> mUsers;
    private Set<String> mTokens;
    private String mServerKey;
    public static boolean isActivityRunning;

    //firebase correction
    private FirebaseFirestore mDb;
    private CollectionReference serverRef, newsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mDepartments = (TextView) findViewById(R.id.broadcast_departments);
        mAddDepartment = (Button) findViewById(R.id.add_department);
        mSendMessage = (Button) findViewById(R.id.btn_send_message);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mMessage = (EditText) findViewById(R.id.input_message);
        mTitle = (EditText) findViewById(R.id.input_title);

        setupEmployeeList();
        init();

        hideSoftKeyboard();
    }

    private void init(){
        mSelectedDepartments = new HashSet<>();
        mTokens = new HashSet<>();
        /*
            --------- Dialog for selecting departments ---------
         */
        mDepartments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening departments selector dialog.");

                AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
                builder.setIcon(R.drawable.ic_departments);
                builder.setTitle("Select Departments:");

                //create an array of the departments
                String[] departments = new String[mDepartmentsList.size()];
                for(int i = 0; i < mDepartmentsList.size(); i++){
                    departments[i] = mDepartmentsList.get(i);
                }

                //get the departments that are already added to the list
                boolean[] checked = new boolean[mDepartmentsList.size()];
                for(int i = 0; i < mDepartmentsList.size(); i++){
                    if(mSelectedDepartments.contains(mDepartmentsList.get(i))){
                        checked[i] = true;
                    }
                }

                builder.setPositiveButton("done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setMultiChoiceItems(departments, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked){
                            Log.d(TAG, "onClick: adding " + mDepartmentsList.get(which) + " to the list.");
                            mSelectedDepartments.add(mDepartmentsList.get(which));
                        }else{
                            Log.d(TAG, "onClick: removing " + mDepartmentsList.get(which) + " from the list.");
                            mSelectedDepartments.remove(mDepartmentsList.get(which));

                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.d(TAG, "onDismiss: dismissing dialog and refreshing token list.");
                        getDepartmentTokens();
                    }
                });
            }
        });

        mAddDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to add new department");
                NewDepartmentDialog dialog = new NewDepartmentDialog();
                dialog.show(getSupportFragmentManager(), getString(R.string.dialog_add_department));
            }
        });

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to send the message.");
                String message = mMessage.getText().toString();
                String title = mTitle.getText().toString();
                if(!isEmpty(message) && !isEmpty(title)){

                    //send message
                    sendMessageToDepartment(title, message);

                    mMessage.setText("");
                    mTitle.setText("");
                }else{
                    Toast.makeText(AdminActivity.this, "Fill out the title and message fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getDepartments();
        getEmployeeList();
        getServerKey();
    }

    /**
     * Retrieve a list of departments that have been added to the database.
     */
    public void getDepartments(){
        mDepartmentsList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_departments));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String department = snapshot.getValue().toString();
                    Log.d(TAG, "onDataChange: found a department: " + department);
                    mDepartmentsList.add(department);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void sendMessageToDepartment(String title, String message){
        Log.d(TAG, "sendMessageToDepartment: sending message to selected departments.");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FCM fcmAPI = retrofit.create(FCM.class);

        //attach the headers
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "key="+ mServerKey);
        //"key=" + mServerKey
        //send the message to all tokens
        for(String token :  mTokens){

            Log.d(TAG, "sendMessageToDepartment: sending to token: " + token);
           Data data = new Data();
            data.setMessage(message);
            data.setTitle(title);
            data.setData_type(getString(R.string.data_type_admin_broadcast));
            FirebaseCloudMessage firebaseCloudMessage = new FirebaseCloudMessage();
            firebaseCloudMessage.setData(data);
            firebaseCloudMessage.setTo(token);

            Log.d(TAG, "The required headers which is the most true : "+ headers);
            Call<ResponseBody> call = fcmAPI.send(headers, firebaseCloudMessage);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "onResponse: Server Response: " + response.toString());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d(TAG, "onFailure: Unable to send the message: " + t.getMessage());
                    Toast.makeText(AdminActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Retrieves the server key for the Firebase server.
     * This is required to send FCM messages.
     */
    private void getServerKey(){
        Log.d(TAG, "getServerKey: retrieving server key.");
        Query locationQuery = null;
        mDb = FirebaseFirestore.getInstance();
        serverRef = mDb
                .collection("server");

        serverRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onDataChange: got the server key.");
                    for (QueryDocumentSnapshot document : task.getResult()) {
                      //  mServerKey = document.getData().toString();
                        mServerKey = document.toObject(ServerKey.class).getServer_key();
                        Log.d(TAG, "onDataChange: found a server: " + mServerKey);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "The main issue here is: " + e);

            }
        });

    }

    /**
     * Get all the tokens of the users who are in the selected departments
     */
    private void getDepartmentTokens(){
        Log.d(TAG, "getDepartmentTokens: searching for tokens.");
        mTokens.clear(); //clear current token list in case admin has change departments
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        for(String department: mSelectedDepartments){
            Log.d(TAG, "getDepartmentTokens: department: " + department);

            Query query = reference.child(getString(R.string.dbnode_users))
                    .orderByChild(getString(R.string.field_department))
                    .equalTo(department);


            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        String token = snapshot.getValue(Users.class).getMessaging_token();
                        Log.d(TAG, "onDataChange: got a token for user named: "
                                + snapshot.getValue(Users.class).getName());
                        mTokens.add(token);
                        Log.d(TAG, "The tokens are: " + mTokens);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void setDepartmentDialog(final Users user){
        Log.d(TAG, "setDepartmentDialog: setting the department of: " + user.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
        builder.setIcon(R.drawable.ic_departments);
        builder.setTitle("Set a Department for " + user.getName() + ":");

        builder.setPositiveButton("done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //get the index of the department (if the user has a department assigned)
        int index = -1;
        for(int i = 0; i < mDepartmentsList.size(); i++){
            if(mDepartmentsList.contains(user.getDepartment())){
                index = i;
            }
        }

        final ListAdapter adapter = new ArrayAdapter<String>(AdminActivity.this,
                android.R.layout.simple_list_item_1, mDepartmentsList);
        builder.setSingleChoiceItems(adapter, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(AdminActivity.this, "Department Saved", Toast.LENGTH_SHORT).show();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.child(getString(R.string.dbnode_users))
                        .child(user.getUser_id())
                        .child(getString(R.string.field_department))
                        .setValue(mDepartmentsList.get(which));
                dialog.dismiss();
                //refresh the list with the new information
                mUsers.clear();
                getEmployeeList();
            }
        });
        builder.show();
    }


    /**
     * Get a list of all employees
     * @throws NullPointerException
     */
    private void getEmployeeList() throws NullPointerException{
        Log.d(TAG, "getEmployeeList: getting a list of all employees");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbnode_users));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Users user = snapshot.getValue(Users.class);
                  //  String department = snapshot.getValue().toString();
                    Log.d(TAG, "onDataChange: found a user: " + user.getName());
                    Log.d(TAG, "onDataChange: found a profile image: " + user.getProfile_image());

                    Log.d(TAG, "onDataChange: found a user list: " + user);
                    mUsers.add(user);
                }
                mEmployeeAdapter.notifyDataSetChanged();
                getDepartmentTokens();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Setup the list of employees
     */
    private void setupEmployeeList(){
       // getEmployeeList();
        mUsers = new ArrayList<>();
        getEmployeeList();
        Log.d(TAG, "The user needed to be uploaded is: " + mUsers);
        mEmployeeAdapter = new EmployeesAdapter(AdminActivity.this, mUsers);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new VerticalSpacingDecorator(15));
        mRecyclerView.setAdapter(mEmployeeAdapter);
    }



    @Override
    public void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityRunning = false;
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Return true if the @param is null
     * @param string
     * @return
     */
    private boolean isEmpty(String string){
        return string.equals("");
    }
}
