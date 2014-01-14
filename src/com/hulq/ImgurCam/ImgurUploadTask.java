package com.hulq.ImgurCam;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
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
import android.os.Build;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.content.Context;
import android.content.ComponentName;
import android.content.DialogInterface;
import java.io.File;
import android.view.Gravity;
import android.content.Intent;
import android.net.NetworkInfo;
import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.widget.TextView;
import android.widget.Toast;

public class ImgurUploadTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = ImgurUploadTask.class.getSimpleName();
    private static final String UPLOAD_URL = "https://api.imgur.com/3/image";

    private Activity mActivity;
    private Uri mImageUri;  // local Uri to upload
    private Bitmap bImage;

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
        mNotificationManager.cancel(0);
        mNotificationManager.notify(0, notiBuilder.build());
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpURLConnection conn = null;
        InputStream responseIn = null;

        Bitmap bitmap = BitmapFactory.decodeFile(mImageUri.getPath());

        try {
            conn = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);

            ImgurAuthorization.getInstance(mActivity).addToHttpURLConnection(conn);

            OutputStream out = conn.getOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
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
        }
    }//end of doInBackground()*/

    private static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }//end of copy

    protected String onInput(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(in);
        while (scanner.hasNext()) {
            sb.append(scanner.next());
        }

        JSONObject root = new JSONObject(sb.toString());
        String id = root.getJSONObject("data").getString("id");

        String deletehash = root.getJSONObject("data").getString("deletehash");
        Log.i(TAG, "new imgur url: http://imgur.com/" + id + " (delete hash: " + deletehash + ")");
        return id;
    }//end of onInput(InputStream)

    @Override
    protected void onCancelled(String result){
        File toBeDeleted = new File(mImageUri.getPath());
        if(toBeDeleted.exists() && !mImageUri.getPath().contains("Screenshots")){
            toBeDeleted.delete();
        }
        mNotificationManager.cancel(0);
        handleFailedUpload();
    }//end of onCancelled

    @Override
    protected void onPostExecute(String result){
        if(result == null || result.isEmpty()){
            if(isOnline()){
                new ImgurUploadTask(mImageUri, mActivity).execute();
            }else{
                handleFailedUpload();
            }
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Imgur URL", "imgur.com/" + result));

        if(!mImageUri.getPath().contains("Screenshots")){
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
            if(!mActivity.isDestroyed() && !mActivity.isFinishing() && !mImageUri.getPath().contains("Screenshots")){
                alert.show();
            }
        }

        //Notification
        Intent notificationIntent = new Intent("android.intent.action.MAIN");
        notificationIntent.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
        notificationIntent.addCategory("android.intent.category.LAUNCHER");
        notificationIntent.setData(Uri.parse("imgur.com/" + result));
        PendingIntent pIntent = PendingIntent.getActivity(mActivity, 0, notificationIntent, 0);


        notiBuilder.setProgress(0, 0, false)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Image URL copied")
            .setContentText("imgur.com/" + result)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setContentIntent(pIntent);

        if(Build.VERSION.SDK_INT >= 16){
            bImage = BitmapFactory.decodeFile(mImageUri.getPath());
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
            bigPictureStyle.setBigContentTitle("Image URL copied");
            bigPictureStyle.setSummaryText("imgur.com/" + result);
            bigPictureStyle.bigPicture(bImage);
            notiBuilder.setStyle(bigPictureStyle);
        }

        mNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
        mNotificationManager.notify(0, notiBuilder.build());

        //Toast
        Toast toast = Toast.makeText(mActivity, "Copied to clipboard:\nimgur.com/" + result, 1);
        toast.show();

        //delete picture
        File toBeDeleted = new File(mImageUri.getPath());
        if(toBeDeleted.exists() && !mImageUri.getPath().contains("Screenshots")){
            toBeDeleted.delete();
        }
    }//end of onPostExecute(String result)*/

    private void handleFailedUpload(){
        if(!mActivity.isDestroyed() && !mActivity.isFinishing() && !mImageUri.getPath().contains("Screenshots")){
            TextView title = new TextView(mActivity);
            title.setText("Image upload has failed :(");
            title.setGravity(Gravity.CENTER);
            title.setTextSize(25.f);
            title.setTextColor(Color.CYAN);

            TextView body = new TextView(mActivity);
            body.setText("\nCheck your network connection!");
            body.setGravity(Gravity.CENTER);
            body.setTextIsSelectable(false);

            Builder popup = new AlertDialog.Builder(mActivity);
            popup.setCustomTitle(title);
            popup.setView(body);
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
            alert.show();
        }

        notiBuilder.setProgress(0, 0, false)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Image upload FAILED :(")
                .setContentText("Please check your network connection")
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
        mNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
        mNotificationManager.notify(0, notiBuilder.build());
    }//end of handleNullResult()*/

}//end of class

