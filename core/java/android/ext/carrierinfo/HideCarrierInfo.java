package android.ext.carrierinfo;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.GosPackageState;
import android.ext.settings.app.AswHideCarrierInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

/** @hide */
public class HideCarrierInfo {
    private static final String TAG = HideCarrierInfo.class.getSimpleName();

    /** @hide */
    public static final int FLAG_HIDE_CARRIER_INFO = 1;

    private static volatile int flags;

    /** @hide */
    public static int getAppBindFlags(Context ctx, int userId, ApplicationInfo appInfo,
                                      GosPackageState gosPs) {
        if (AswHideCarrierInfo.I.get(ctx, userId, appInfo, gosPs)) {
            return FLAG_HIDE_CARRIER_INFO;
        }
        return 0;
    }

    /** @hide */
    public static void handleAppBindFlags(int v) {
        flags = v;
    }

    /** @hide */
    public static void onGosPackageStateChanged(Context ctx, GosPackageState gosPs) {
        Application app = AppGlobals.getInitialApplication();
        if (app == null) {
            return;
        }
        flags = getAppBindFlags(ctx, UserHandle.myUserId(), app.getApplicationInfo(), gosPs);
    }

    /** @hide */
    public static boolean isEnabled() {
        return (flags & FLAG_HIDE_CARRIER_INFO) != 0;
    }

    /** @hide */
    public static boolean shouldFilter(String key) {
        if (!isEnabled() || key == null) {
            return false;
        }
        var filter = key.startsWith("gsm.sim.") || key.startsWith("gsm.operator.");
        if (filter) {
            reportAccess(key);
        }
        return filter;
    }

    /** @hide */
    public static boolean hideFromSelf(String apiName) {
        if (!isEnabled()) {
            return false;
        }
        reportAccess(apiName);
        return true;
    }

    private static void reportAccess(String apiName) {
        Application app = AppGlobals.getInitialApplication();
        if (app == null) {
            return;
        }
        try {
            ActivityManager.getService()
                    .showCarrierInfoAccessNotification(app.getPackageName(), apiName);
        } catch (RemoteException e) {
            Log.d(TAG, "", e);
        }
    }

    private HideCarrierInfo() {}
}
