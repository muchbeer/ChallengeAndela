package muchbeer.raum.com.challengeandela.firebaseauth;

import android.graphics.Bitmap;
import android.net.Uri;

public interface OnPhotoReceivedListener {
    public void getImagePath(Uri imagePath);
    public void getImageBitmap(Bitmap bitmap);
}
