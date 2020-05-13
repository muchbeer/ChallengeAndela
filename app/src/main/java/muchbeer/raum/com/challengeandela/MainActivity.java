package muchbeer.raum.com.challengeandela;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;

    private LinearLayoutManager mCarLayoutManager;
    FloatingActionButton fab;
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "working", Toast.LENGTH_SHORT).show();
            }
        });

        if (FirebaseUtil.isAdmin == true) {fab.show();}
        else { fab.hide(); }


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
      //  setMenuTrue(insertMenu);
        return true;
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
