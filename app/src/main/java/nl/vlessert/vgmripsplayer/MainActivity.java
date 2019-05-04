package nl.vlessert.vgmripsplayer;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private SlidingUpPanelLayout mLayout;

    private IntentFilter filter;
    private BroadcastReceiver receiver;
    private DownloadManager downloadManager;

    private static final int REQUEST_WRITE_STORAGE = 112;

    private static final String TAG = "VGMRipsPlayer";

    private HelperFunctions helpers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helpers = new HelperFunctions();

        WebView.setWebContentsDebuggingEnabled(true);

        setContentView(R.layout.activity_main);

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    Log.d(TAG, "Download event!!: " + intent.getAction());
                    unpackDownloadedFile(context, intent);
                }
            }
        };

        registerReceiver(receiver, filter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager
                .PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PermissionUtils.neverAskAgainSelected(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    displayNeverAskAgainDialog();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE);
                }
            }
        } else {
            downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);


            if (!helpers.directoryExists("/")) {
                helpers.makeDirectory("/");
                helpers.makeDirectory("/data");
                helpers.makeDirectory("/tmp");
            }
            helpers.deleteAllFilesInDirectory("/tmp/");
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.setDownloadListener(new DownloadListener() {
                public void onDownloadStart(String url, String userAgent,
                                            String contentDisposition, String mimetype,
                                            long contentLength) {
                    Log.d(TAG, "oooooooooo url: " + url);
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(url));

                    request.allowScanningByMediaScanner();
                    //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                    //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Name of your downloadble file goes here, example: Mathematics II ");
                    helpers.deleteFile("/tmp", "1.vgz");
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "VGMRipsPlayer/tmp/1.vgz");
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                            Toast.LENGTH_LONG).show();

                }
            });

            myWebView.clearCache(true);
            myWebView.getSettings().setAppCacheEnabled(false);
            myWebView.getSettings().setJavaScriptEnabled(true);

            myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            myWebView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    try {
                        Log.d(TAG, "oooooooooo url: " + url);
                        // do whatever you want to do on a web link click

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }

            });
            myWebView.loadUrl("http://vgmrips.net/packs/pack/xak-the-art-of-visual-stage-msx2?browser=webview");
            //myWebView.loadUrl("http://192.168.1.20/test2/index.php?TEST=blaat");
        }
    }

    private void unpackDownloadedFile(Context context, Intent intent) {
        //Log.d(LOG_TAG,"Download event!!!!");
        String action = intent.getAction();
        //Log.d("vigamup download result", action + ", " + downloadReference);
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {// && downloadReference!=null) {

            // get the DownloadManager instance
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            DownloadManager.Query q = new DownloadManager.Query();
            //q.setFilterById(downloadReference);

            Bundle extras = intent.getExtras();
            q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = manager.query(q);

            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                Log.d(TAG, "status: " + status + ", " + DownloadManager.STATUS_FAILED);
                if (status != DownloadManager.STATUS_FAILED) {
                    //String name = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                    String downloadFileLocalUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    String name = "";
                    if (downloadFileLocalUri != null) {
                        File mFile = new File(Uri.parse(downloadFileLocalUri).getPath());
                        name = mFile.getAbsolutePath();
                    }
                    Log.i(TAG, "file name: " + name);
                    Log.i(TAG, "uri: " + uri);
                }
            }
        }
    }

    private void displayNeverAskAgainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("VGMRipsPlayer requires access to the external storage to store downloaded VGM data. "
                + "\n\nSince you did not allow it permanently you need to enable it in the Settings screen or reinstall VGMRipsPlayer.");
        builder.setCancelable(false);
        builder.setPositiveButton("Permit Manually", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (REQUEST_WRITE_STORAGE == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted successfully");
                Toast.makeText(this, "Permission granted successfully", Toast.LENGTH_LONG).show();
            } else {
                PermissionUtils.setShouldShowStatus(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }
}
