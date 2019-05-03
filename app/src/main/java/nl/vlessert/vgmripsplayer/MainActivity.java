package nl.vlessert.vgmripsplayer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity {

    private SlidingUpPanelLayout mLayout;

    private static final int REQUEST_WRITE_STORAGE = 112;

    private static final String TAG = "VGMRipsPlayer";

    private HelperFunctions helpers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helpers = new HelperFunctions();

        setContentView(R.layout.activity_main);
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
            if (!helpers.directoryExists("/")) {
                helpers.makeDirectory("/");
                helpers.makeDirectory("/data");
                helpers.makeDirectory("/tmp");
            }
            helpers.deleteAllFilesInDirectory("/tmp/");
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
