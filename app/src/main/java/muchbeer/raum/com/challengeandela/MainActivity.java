package muchbeer.raum.com.challengeandela;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import muchbeer.raum.com.challengeandela.adapter.CarAdapter;
import muchbeer.raum.com.challengeandela.utility.CarDeals;
import muchbeer.raum.com.challengeandela.utility.FirebaseUtil;

public class MainActivity extends AppCompatActivity {
    ArrayList<CarDeals> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;

    private LinearLayoutManager mCarLayoutManager;
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

  /*      mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;;
*/
       /* mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TextView  txtShowCarName = (TextView) findViewById(R.id.tvCars);
                CarDeals carList = dataSnapshot.getValue(CarDeals.class);
                txtShowCarName.setText(txtShowCarName.getText()+ "\n" + carList.getCarName());


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
*/
       // mDatabaseReference.addChildEventListener(mChildListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.openFbReference("Vehicle", this);

        RecyclerView rvDeals = (RecyclerView) findViewById(R.id.rv_all_cars);
        final CarAdapter adapter = new CarAdapter();
        rvDeals.setAdapter(adapter);

        mCarLayoutManager = new LinearLayoutManager(this);
        rvDeals.setLayoutManager(mCarLayoutManager);
        FirebaseUtil.attachListener();
      //  setMenuTrue(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem insertMenu = menu.findItem(R.id.insert_menu);
        setMenuTrue(insertMenu);
        return true;
    }

    private void setMenuTrue(MenuItem insertMenu) {
        if (FirebaseUtil.isAdmin == true) {

            insertMenu.setVisible(true);
        }
        else {
            insertMenu.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_menu:
                Intent intent = new Intent(this, InsertCarActivity.class);
                startActivity(intent);
                return true;

            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(LOG_TAG, "Logout " + "User Logged Out");
                                FirebaseUtil.attachListener();
                            }
                        });
                FirebaseUtil.detachListener();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void showMenu() {
        invalidateOptionsMenu();
    }
}
