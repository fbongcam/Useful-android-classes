import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.util.Log;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * ----------------------
 * Permissions Helper
 * ----------------------
 * @author Filip Bongcam
 * @github https://github.com/fbongcam
 *
 * @description
 * Helper class to handle permissions more easily in Android.
 * This class looks for permissions specified within the app's
 * manifest (AndroidManifest.xml) and stores all info surrounding them.
 */

public class PermissionsHelper extends AppCompatActivity {
    public static final String TAG = "PermissonsHelper";
    private final Context context;
    private final RequestCodeGenerator requestCodeGenerator;

    // Permissions loaded from Manifest
    public String[] permissions;
    public final ArrayList<PermissionInfo> permissionInfos;

    // Default ActivityResultLauncher
    private ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FROM GOOGLE (Android Developer Docs)
                    // -------------------------------------------------------------
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    // -------------------------------------------------------------
                }
                else {
                    // FROM GOOGLE (Android Developer Docs)
                    // ------------------------------------------------------------------
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    // ------------------------------------------------------------------
                }
            });

    /**
     * Constructor
     *
     * @param context
     */
    public PermissionsHelper(Context context) {
        this.context = context;
        this.requestCodeGenerator = new RequestCodeGenerator();
        this.permissionInfos = new ArrayList<PermissionInfo>();
    }

    /**
     * Constructor
     *
     * @param context
     * @param activityResultLauncher
     */
    public PermissionsHelper(Context context, ActivityResultLauncher<String> activityResultLauncher) {
        this.context = context;
        this.requestCodeGenerator = new RequestCodeGenerator();
        this.permissionInfos = new ArrayList<PermissionInfo>();
        this.activityResultLauncher = activityResultLauncher;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadManifestPermissions();
    }

    public void setActivityResultLauncher(ActivityResultLauncher<String> activityResultLauncher) {
        this.activityResultLauncher = activityResultLauncher;
    }

    /**
     * Request a single permission
     * @param permission name
     */
    public boolean requestPermission(String permission) {
        if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
        {
            activityResultLauncher.launch(permission);
        }
        else
        {
            Log.v(TAG, permission + " already granted.");
            return false;
        }
        return true;
    }

    /**
     * Loads all permissions listed in the manifest
     */
    public void loadManifestPermissions() {
        try
        {
            PackageInfo requestedPermissions = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            this.permissions = requestedPermissions.requestedPermissions;
            for (int i=0; i < permissions.length; i++)
            {
                permissionInfos.add(context.getPackageManager()
                        .getPermissionInfo(permissions[i], PackageManager.GET_META_DATA));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "App permissions: " + permissionInfos.toString());
    }

    /**
     * Request all permissions listed in the manifest at once
     */
    public void requestPermissions() {
        ArrayList<String> nonApprovedPermissions = new ArrayList<String>();
        for (int i=0; i < permissions.length; i++)
        {
            if (ActivityCompat.checkSelfPermission(context, permissionInfos.get(i).name) != PackageManager.PERMISSION_GRANTED)
            {
                nonApprovedPermissions.add(permissionInfos.get(i).name);
            }
        }

        Log.v(TAG, "Non Approved Permissions: " + nonApprovedPermissions);

        // Request missing permissions
        if (nonApprovedPermissions.size() > 0) {
            for (int i = 0; i < nonApprovedPermissions.size(); i++) {
                if (permissionInfos.get(i).getProtection() > PermissionInfo.PROTECTION_NORMAL)
                {
                    activityResultLauncher.launch(nonApprovedPermissions.get(i));
                }
                else
                {
                    // Request all permissions with normal protection level at once
                    if (i == nonApprovedPermissions.size()-1)
                    {
                        System.out.println(requestCodeGenerator.getCode());
                        ((Activity) context).requestPermissions(nonApprovedPermissions.toArray(new String[0]), requestCodeGenerator.getCode());
                    }
                }
            }
            // Request permissions that should show explanation
            String[] nonApproved = getNonApprovedPermissions();
            for (String permission : nonApproved)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission))
                {
                    // SHOW EXPLANATION
                }
            }

        }
    }

    /**
     * Check status of all permissions listed in manifest
     * @return true if all permissions granted, false if not all permissions granted
     */
    public boolean checkPermissions() {
        boolean allGranted = true;
        for (int i=0; i < permissions.length; i++)
        {
            if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED)
            {
                Log.v(TAG, permissions[i] + " was not granted.");
            }
            else
            {
                Log.v(TAG, permissions[i] + " was granted.");
                allGranted = false;
            }
        }
        return allGranted;
    }

    /**
     * Check status of specific permission
     * @param permission name
     * @return true or false (granted or not granted)
     */
    public boolean checkPermission(String permission) {
        if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
        {
            Log.v(TAG, permission + " is not granted.");
            return false;
        }
        Log.v(TAG, permission + " granted.");
        return true;
    }

    /**
     * Returns permissions gathered
     * @return permissions
     */
    public String[] getPermissions() {
        return permissions;
    }

    /**
     * Gets currently non approved permissions
     * @return an array of non approved permissions
     */
    public String[] getNonApprovedPermissions() {
        List<String> nonApproved = new ArrayList<>();
        for (int i=0; i < permissions.length; i++)
        {
            if (ActivityCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED)
            {
                nonApproved.add(permissions[i]);
                Log.v(TAG, permissions[i] + " was not granted.");
            }
        }
        return nonApproved.toArray(new String[0]);
    }

    /**
     * Returns info for all permissions gathered
     * @return permissions info
     */
    public PermissionInfo[] getPermissionsInfo() {
        return permissionInfos.toArray(new PermissionInfo[0]);
    }

    public ActivityResultLauncher getDefaultActivityLauncher() {
        return this.activityResultLauncher;
    }

}

