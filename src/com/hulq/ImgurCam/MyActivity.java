package com.hulq.ImgurCam;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.content.res.Configuration;
import java.util.Date;
import java.io.File;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.content.Intent;
import java.io.IOException;
import android.provider.MediaStore;
import java.text.SimpleDateFormat;
import android.net.Uri;

import android.app.ActivityManager;

public class MyActivity extends Activity {

    private static final int ACTION_TAKE_PHOTO_B = 1;
    private ImageView imageView;

    private String currentPhotoPath;
    private File photoFile;
    public Bitmap bImage;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "imgurCam_" + timeStamp;
        File storageDir = this.getExternalCacheDir();
        File image = null;
        try{
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            image.deleteOnExit();
        }catch(IOException ex){}
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }//end of createImageFile()*/

    public static Bitmap decodeFile(Uri mImageUri, Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageUri.getPath(), options);
        options.inJustDecodeBounds = false;
        options.inScaled = true;
        options.inDensity = options.outWidth;
        options.inTargetDensity = displayMetrics.densityDpi;
        Bitmap bm = BitmapFactory.decodeFile(mImageUri.getPath(), options);
        return bm;
    }

    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            photoFile.delete();
            finish();
        } else {
            Uri photoUri = Uri.parse(currentPhotoPath);
            bImage = decodeFile(photoUri, MyActivity.this);
            imageView.setImageBitmap(bImage);
            new ImgurUploadTask(photoUri, this).execute();
        }
    }//end of onActivityResult(int requestCode, int resultCode, Intent data)*/

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //delete files in cache
        File cacheDir = this.getExternalCacheDir();
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files)
                if(file.getName().contains("imgurCam")) file.delete();
        }
        imageView = (ImageView) findViewById(R.id.imageView1);
        dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
    }//end of onCreate(Bundle savedInstanceState)*/
}//end of MyActivity class*/


