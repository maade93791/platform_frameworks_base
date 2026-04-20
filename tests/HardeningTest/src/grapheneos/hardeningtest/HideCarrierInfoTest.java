package grapheneos.hardeningtest;

import android.content.pm.GosPackageStateFlag;

import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner;
import com.android.tradefed.testtype.junit4.BaseHostJUnit4Test;
import com.android.tradefed.testtype.junit4.DeviceTestRunOptions;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

@RunWith(DeviceJUnit4ClassRunner.class)
public class HideCarrierInfoTest extends BaseHostJUnit4Test {

    private static final String TEST_PACKAGE_BASE_NAME = "app.grapheneos.hardeningtest";

    private static final String TEST_PACKAGE_SDK_27 = TEST_PACKAGE_BASE_NAME + ".sdk_27";
    private static final String TEST_PACKAGE_SDK_LATEST = TEST_PACKAGE_BASE_NAME + ".sdk_latest";

    private void runDeviceTest(String pkgName, String methodName) {
        var opts = new DeviceTestRunOptions(pkgName);
        opts.setTestClassName("app.grapheneos.hardeningtest.HideCarrierInfoDeviceTest");
        opts.setTestMethodName(methodName);
        try {
            runDeviceTests(opts);
        } catch (DeviceNotAvailableException e) {
            throw new IllegalStateException(e);
        }
    }

    private void editGosPackageState(String pkgName, int[] addFlags, int[] clearFlags) {
        try {
            var device = getDevice();
            var cmd = new StringBuilder("pm edit-gos-package-state " + pkgName + " " + device.getCurrentUser());
            for (int flag : addFlags) {
                cmd.append(" add-flag ").append(flag);
            }
            for (int flag : clearFlags) {
                cmd.append(" clear-flag ").append(flag);
            }
            var edRes = device.executeShellV2Command(cmd.toString());
            assertEquals(edRes.toString(), 0L, (long) edRes.getExitCode());
        } catch (DeviceNotAvailableException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setHideCarrierInfo(String pkgName, boolean isSet) {
        int nonDefault = GosPackageStateFlag.HIDE_CARRIER_INFO_NON_DEFAULT;
        int flag = GosPackageStateFlag.HIDE_CARRIER_INFO;
        int[] addFlags = isSet ? new int[] { nonDefault, flag } : new int[] { nonDefault };
        int[] clearFlags = isSet ? new int[0] : new int[] { flag };
        editGosPackageState(pkgName, addFlags, clearFlags);
    }

    private void forEachPackage(Consumer<String> action) {
        for (String pkg : new String[] {TEST_PACKAGE_SDK_27, TEST_PACKAGE_SDK_LATEST}) {
            action.accept(pkg);
        }
    }

    @Test
    public void testCarrierInfoHidden() {
        forEachPackage(pkg -> {
            setHideCarrierInfo(pkg, true);
            runDeviceTest(pkg, "testCarrierInfoHidden");
        });
    }

    @Test
    public void testCarrierInfoVisible() {
        forEachPackage(pkg -> {
            setHideCarrierInfo(pkg, false);
            runDeviceTest(pkg, "testCarrierInfoVisible");
        });
    }
}
