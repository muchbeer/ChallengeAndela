package muchbeer.raum.com.challengeandela;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import muchbeer.raum.com.challengeandela.utility.CarDeals;
import muchbeer.raum.com.challengeandela.utility.FirebaseUtil;

public class InsertCarActivity extends AppCompatActivity {
    private static String LOG_DATA = InsertCarActivity.class.getSimpleName();
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private Uri imageUri;
    EditText edtCarName;
    EditText edtDescription;
    EditText edtPrice;
    ImageView imageView;
    ProgressBar mProgressBar;
    CarDeals deal;
    Boolean setMenuInsert;
    Button btnImage;
    private static final int PICTURE_RESULT =42;

    private FirebaseStorage storage = FirebaseStorage.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_car);

        Toolbar toolbar = findViewById(R.id.toolbar_insert);
        setSupportActionBar(toolbar);

       // FirebaseUtil.openFbReference("Vehicle", this);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;


        edtCarName = (EditText) findViewById(R.id.edt_car);
        edtDescription = (EditText) findViewById(R.id.edt_model);
        edtPrice = (EditText) findViewById(R.id.edt_price);
        imageView = (ImageView) findViewById(R.id.imageUrl);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        Intent intent = getIntent();
        CarDeals deal = (CarDeals) intent.getSerializableExtra("Deal");
        if (deal==null) {
            deal = new CarDeals();
        }
        this.deal = deal;
        edtCarName.setText(deal.getCarName());
        edtDescription.setText(deal.getDescription());
        edtPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());
        btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,
                        "Insert Picture"), PICTURE_RESULT);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
          imageUri = data.getData();

            //final StorageReference firememeRef = storage.getReference(imageUri.getLastPathSegment());
            final StorageReference firememeRef = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());

            UploadTask uploadTask = firememeRef.putFile(imageUri);
            mProgressBar.setVisibility(View.VISIBLE);
          /*  Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setProgress(0);
                }
            }, 50);*/
            uploadTask.addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskSnapshot) {
                    Log.d(LOG_DATA,  "Upload Task Complete with no doubt " );
                    mProgressBar.setVisibility(View.GONE);

                 //   String pictureName = taskSnapshot.getStorage().getPath();
                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                       Toast.makeText(InsertCarActivity.this, "Error in uploading the failure message is: " +e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    Log.d(LOG_DATA, "The total count is: "+ progress);
                    mProgressBar.setProgress((int) progress);
                }
            });

            Task<Uri> getDownloadUriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                                           @Override
                                                                           public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                                               if(!task.isSuccessful()) {
                                                                                   Log.d(LOG_DATA,  "Something went wrong here " );
                                                                                   throw task.getException();

                                                                               }
                                                                               return firememeRef.getDownloadUrl();
                                                                           }
                                                                       });

            getDownloadUriTask.addOnCompleteListener(this, new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {

                        Uri downloadUri = task.getResult();
                        deal.setImageUrl(downloadUri.toString());
                       // deal.setImageName(pictureName);
                        showImage(downloadUri.toString());
                        Log.d(LOG_DATA, "The best link url now is : " + downloadUri);
                    }
                }
            });


/*
                    StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  // String url = taskSnapshot.getDownloadUrl().toString();
                    String url_second_url = taskSnapshot.getUploadSessionUri().toString();
                 String url = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
              String url_storage =   taskSnapshot.getStorage().getDownloadUrl().toString();
                    String pictureName = taskSnapshot.getStorage().getPath();

                    deal.setImageUrl(url);
                    deal.setImageName(pictureName);

                    Log.d(LOG_DATA,  "Url: " +  url);
                    Log.d(LOG_DATA, "Name  "+ pictureName);
                    Log.d(LOG_DATA, " Second Option url is: " + url_second_url);
                    Log.d(LOG_DATA, " url Storage link hope is  : " + url_storage);
                    showImage(imageUri.toString());
                }
            });*/

        }
    }

    private void showImage(String url) {
        if (url != null && url.isEmpty() == false) {
            Log.d(LOG_DATA, "The url that is giving hard time is:  "+ url);
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
           Picasso.get()
                    .load(url)
                   .resize(width, width*2/3)
                   .centerCrop()
                 //  .placeholder(R.drawable.fail_image)
                   .error(R.drawable.fail_image)
                    .into(imageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_car, menu);

        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            btnImage.setVisibility(View.VISIBLE);
            enableEditTexts(true);
        }
        else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            btnImage.setVisibility(View.GONE);
            enableEditTexts(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveCar();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                 clean();
                 backToList();
                return true;
            case R.id.delete_menu:
                  deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                 backToList();
                return true;

            case R.id.main_menu:

                Toast.makeText(this, "Going to main Menu", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveCar() {
        deal.setCarName(edtCarName.getText().toString());
        deal.setDescription(edtDescription.getText().toString());
        deal.setPrice(edtPrice.getText().toString());
        if(deal.getId()==null) {
            mDatabaseReference.push().setValue(deal);
            Log.d(LOG_DATA, "This show a success message for the new Record");
        }
        else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
            Log.d(LOG_DATA, "This show an Edit message need to be edit and saved");
        }
    }

    private void deleteDeal() {
        if (deal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
       // mDatabaseReference.child(deal.getId()).removeValue();

        mDatabaseReference.child(deal.getId()).removeValue();
        //Log.d(LOG_DATA,"The deal is " + deal.getImageName());
       // if(deal.getImageName() != null && deal.getImageName().isEmpty() == false) {
            if(deal.getImageName() != null && deal.getImageName().isEmpty() == false) {
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image Successfully Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image", e.getMessage());
                }
            });
        }

    }
    private void backToList() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void clean() {
        edtCarName.setText("");
        edtPrice.setText("");
        edtDescription.setText("");
        edtCarName.requestFocus();
    }

    private void enableEditTexts(boolean isEnabled) {
        edtCarName.setEnabled(isEnabled);
        edtDescription.setEnabled(isEnabled);
        edtPrice.setEnabled(isEnabled);
    }

}
