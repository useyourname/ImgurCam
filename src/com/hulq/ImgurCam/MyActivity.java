package com.hulq.ImgurCam;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.content.Context;
import java.util.Date;
import android.os.Environment;
import java.io.File;
import com.hulq.ImgurCam.ImgurUploadTask;
import android.widget.ImageView;
import android.content.Intent;
import java.io.IOException;
import android.util.Log;
import android.provider.MediaStore;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.text.SimpleDateFormat;
import android.net.Uri;
import android.view.View;


public class MyActivity extends Activity {

    private static final int ACTION_TAKE_PHOTO_B = 1;
    private static final int ACTION_TAKE_PHOTO_S = 2;
    private static final int ACTION_TAKE_VIDEO = 3;
    private ImageView imageView;

    private String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "imgurCam_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        try{
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        }catch(IOException ex){
            System.out.println("createTempFile FAILED");
        }
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }//end of createImageFile()*/

    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                photoFile = null;
                currentPhotoPath = null;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, actionCode);
            }
        }
    }//end of dispatchTakePictureIntent(int actionCode)*/

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri photoUri = Uri.parse(currentPhotoPath);
        mediaScanIntent.setData(photoUri);
        this.sendBroadcast(mediaScanIntent);
    }//end of galleryAddPic()*/

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            Log.d("CameraDemo", "Pic saved");
//        }else{
//            finish();
//        }
        if(resultCode != RESULT_OK){
            finish();
        }

        Uri photoUri = Uri.parse(currentPhotoPath);
        imageView.setImageURI(photoUri);
        galleryAddPic();
    }//end of onActivityResult(int requestCode, int resultCode, Intent data)*/

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
        imageView = (ImageView) findViewById(R.id.imageView1);

    }//end of onCreate(Bundle savedInstanceState)*/

    protected void onResume() {
        super.onResume();
        Uri photoUri = Uri.parse(currentPhotoPath);
        new ImgurUploadTask(photoUri, this).execute();
    }//end of onResume()*/
}//end of MyActivity class*/


