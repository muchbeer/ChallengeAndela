package muchbeer.raum.com.challengeandela;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static muchbeer.raum.com.challengeandela.utility.FirebaseUtil.mStorage;

public class ReservedUploadImageWithText extends AppCompatActivity {

    private View mImageContainer;
    private TextView mImageText;
    private Button mUploadButton;
    private ProgressBar mUploadProgressBar;
    private TextView mDownloadUrlTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* mImageContainer = findViewById
        mImageText = findViewById
        EditText imageEditText = findViewById
        mUploadButton = findViewById
        mUploadProgressBar
                mDownloadUrlTextView=
                mUploadButton.setOnClickListener(mUploadClickHandler);
        imageEditText.addTextChangeListener(mEditTextTextWatcher);*/

    }


  /*  private TextWatcher mEditTextTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged
        @Override
        public void onTextChanged
        @Override
        public void afterTextChanged(Editable s) {
            mImageText.setText(s);
        }
    }

    private View.OnClickListener mUploadClickHandler = new View.OnClickListener() {

        @Override
        public void onClick(View v) {



            Bitmap capture = Bitmap.createBitmap(
                    mImageContainer.getWidth(),
                    mImageContainer.getHeight(),
                    Bitmap.Config.ARGB_8888);

            Canvas captureCanvas = new Canvas(capture);
            mImageContainer.draw(captureCanvas);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            capture.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] data = outputStream.toByteArray();

            String path = "raum_picture/" + UUID.randomUUID() +".png";
            StorageReference firememeRef = mStorage.getReference(path);

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("caption", mImageText.getText().toString())
                    .build();

            UploadTask uploadTask = firememeRef.putBytes(data, metadata);*/

        }
