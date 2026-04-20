package com.android.server.ext;

import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.GosPackageState;
import android.content.pm.GosPackageStateFlag;
import android.content.pm.PackageManagerInternal;
import android.ext.SettingsIntents;
import android.ext.settings.app.AswHideCarrierInfo;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;

import com.android.internal.R;
import com.android.server.LocalServices;

public class CarrierInfoAccessUtils {
    private static final String TAG = "CarrierInfoAccess";

    private CarrierInfoAccessUtils() {}

    public static boolean shouldHideAndReport(Context ctx, int callingUid, String apiName) {
        int userId = UserHandle.getUserId(callingUid);
        String[] pkgs;
        try {
            pkgs = AppGlobals.getPackageManager().getPackagesForUid(callingUid);
        } catch (RemoteException e) {
            return false;
        }
        if (pkgs == null || pkgs.length == 0) {
            return false;
        }
        var pmi = LocalServices.getService(PackageManagerInternal.class);
        String reportPkg = null;
        for (String pkg : pkgs) {
            ApplicationInfo appInfo = pmi.getApplicationInfo(pkg, 0, Process.SYSTEM_UID, userId);
            if (appInfo == null) continue;
            GosPackageState gosPs = pmi.getGosPackageState(pkg, userId);
            if (AswHideCarrierInfo.I.get(ctx, userId, appInfo, gosPs)) {
                reportPkg = pkg;
                break;
            }
        }
        if (reportPkg == null) {
            return false;
        }
        reportAccess(ctx, reportPkg, userId, apiName);
        return true;
    }

    public static void reportAccess(Context ctx, String pkgName, int userId, String apiName) {
        var pm = LocalServices.getService(PackageManagerInternal.class);
        ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0, Process.SYSTEM_UID, userId);
        if (appInfo == null) {
            return;
        }

        Slog.d(TAG, "reportAccess: pkg=" + pkgName + " api=" + apiName);

        AppSwitchNotification n = AppSwitchNotification.create(
                ctx, appInfo, SettingsIntents.APP_HIDE_CARRIER_INFO);
        n.titleRes = R.string.notif_hide_carrier_info_title;
        n.gosPsFlagSuppressNotif = GosPackageStateFlag.HIDE_CARRIER_INFO_SUPPRESS_NOTIF;
        n.maybeShow();
    }
}
