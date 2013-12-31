package com.hulq.ImgurCam;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.hulq.ImgurCam.ImgurAuthorization;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


//new imports
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.graphics.BitmapFactory;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.content.Context;
import android.os.Environment;
import java.io.File;
import android.view.Gravity;
import android.content.Intent;
import android.content.DialogInterface;
import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.widget.TextView;

public class ImgurUploadTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = ImgurUploadTask.class.getSimpleName();

    private static final String UPLOAD_URL = "https://api.imgur.com/3/image";

    private Activity mActivity;
    private Uri mImageUri;  // local Uri to upload

    private NotificationCompat.Builder notiBuilder;
    private NotificationManager mNotificationManager;

    public ImgurUploadTask(Uri imageUri, Activity activity) {
        this.mImageUri = imageUri;
        this.mActivity = activity;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();

        notiBuilder = new NotificationCompat.Builder(mActivity);
        notiBuilder.setContentTitle("Picture Upload")
                .setContentText("Upload in progress")
                .setSmallIcon(R.drawable.upload);
        // Sets an activity indicator for an operation of indeterminate length
        notiBuilder.setProgress(0, 0, true);
        // Issues the notification
        mNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, notiBuilder.build());
    }

    @Override
    protected String doInBackground(Void... params) {
        InputStream imageIn;
        try {
            imageIn = mActivity.getContentResolver().openInputStream(mImageUri);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "could not open InputStream", e);
            return null;
        }

        HttpURLConnection conn = null;
        InputStream responseIn = null;

        try {
            conn = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);

            ImgurAuthorization.getInstance(mActivity).addToHttpURLConnection(conn);

            OutputStream out = conn.getOutputStream();
            copy(imageIn, out);
            out.flush();
            out.close();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseIn = conn.getInputStream();
                return onInput(responseIn);
            }
            else {
                Log.i(TAG, "responseCode=" + conn.getResponseCode());
                responseIn = conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                Scanner scanner = new Scanner(responseIn);
                while (scanner.hasNext()) {
                    sb.append(scanner.next());
                }
                Log.i(TAG, "error response: " + sb.toString());
                return null;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error during POST", ex);
            return null;
        } finally {
            try {
                responseIn.close();
            } catch (Exception ignore) {}
            try {
                conn.disconnect();
            } catch (Exception ignore) {}
            try {
                imageIn.close();
            } catch (Exception ignore) {}
        }
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    protected String onInput(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(in);
        while (scanner.hasNext()) {
            sb.append(scanner.next());
        }

        JSONObject root = new JSONObject(sb.toString());
        String id = root.getJSONObject("data").getString("id");

        //String deletehash = root.getJSONObject("data").getString("deletehash");
        //Log.i(TAG, "new imgur url: http://imgur.com/" + id + " (delete hash: " + deletehash + ")");
        return id;
    }

    protected void onPostExecute(String result){
        ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Imgur URL", "imgur.com/" + result));

        TextView title = new TextView(mActivity);
        title.setText("URL has been copied");
        title.setGravity(Gravity.CENTER);
        title.setTextSize(25.f);
        title.setTextColor(Color.CYAN);

        TextView url = new TextView(mActivity);
        url.setText("\nimgur.com/" + result);
        url.setGravity(Gravity.CENTER);
        url.setTextIsSelectable(true);

        Builder popup = new AlertDialog.Builder(mActivity);
        popup.setCustomTitle(title);
        popup.setView(url);
        popup.setCancelable(false);
        popup.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                        dialog.cancel();
                        mActivity.recreate();
                    }
                });

        final AlertDialog alert = popup.create();
        if(!mActivity.isFinishing()){
            alert.show();
        }

        //Notification
//        NotificationCompat.Builder notiBuilder =
//                new NotificationCompat.Builder(mActivity)
                notiBuilder.setProgress(0, 0, false)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Image URL copied")
                .setContentText("imgur.com/" + result)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle("Image URL copied");
        bigPictureStyle.setSummaryText("imgur.com/" + result);
        bigPictureStyle.bigPicture(BitmapFactory.decodeFile(mImageUri.getPath()));

        notiBuilder.setStyle(bigPictureStyle);

        mNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, notiBuilder.build());

        //delete picture from memory
        File toBeDeleted = new File(mImageUri.getPath());
        toBeDeleted.delete();
        
    }//end of onPostExecute(String result)*/

}//end of class

