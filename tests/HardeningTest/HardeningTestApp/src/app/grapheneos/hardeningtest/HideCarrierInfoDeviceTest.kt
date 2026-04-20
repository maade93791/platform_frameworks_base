package app.grapheneos.hardeningtest

import android.app.Application
import android.ext.carrierinfo.HideCarrierInfo
import android.location.Country
import android.location.CountryDetector
import android.os.SystemProperties
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HideCarrierInfoDeviceTest {
    private val ctx = ApplicationProvider.getApplicationContext<Application>()
    private val tm = ctx.getSystemService(TelephonyManager::class.java)!!

    @Test
    fun testCarrierInfoHidden() {
        Assert.assertTrue("HideCarrierInfo.isEnabled()", HideCarrierInfo.isEnabled())

        // TelephonyManager
        Assert.assertEquals("", tm.networkOperatorName)
        Assert.assertEquals("", tm.networkOperator)
        Assert.assertEquals("", tm.networkCountryIso)
        Assert.assertEquals("", tm.simOperator)
        Assert.assertEquals("", tm.simOperatorName)
        Assert.assertEquals("", tm.simCountryIso)
        Assert.assertFalse(tm.isNetworkRoaming)
        Assert.assertEquals(TelephonyManager.SIM_STATE_ABSENT, tm.simState)
        Assert.assertEquals(TelephonyManager.SIM_STATE_ABSENT, tm.getSimState(0))

        // sysprops shouldFilter()
        Assert.assertTrue(HideCarrierInfo.shouldFilter("gsm.sim.operator.alpha"))
        Assert.assertTrue(HideCarrierInfo.shouldFilter("gsm.sim.operator.numeric"))
        Assert.assertTrue(HideCarrierInfo.shouldFilter("gsm.operator.alpha"))
        Assert.assertTrue(HideCarrierInfo.shouldFilter("gsm.operator.numeric"))
        Assert.assertTrue(HideCarrierInfo.shouldFilter("gsm.operator.iso-country"))
        Assert.assertTrue(HideCarrierInfo.shouldFilter("gsm.sim.state"))

        // non-replacements
        Assert.assertFalse(HideCarrierInfo.shouldFilter("ro.product.cpu.abi"))
        Assert.assertFalse(HideCarrierInfo.shouldFilter("gsm.version.baseband"))

        // sysprops replacements
        Assert.assertEquals("", SystemProperties.get("gsm.sim.operator.alpha"))
        Assert.assertEquals("", SystemProperties.get("gsm.sim.operator.numeric"))
        Assert.assertEquals("", SystemProperties.get("gsm.operator.alpha"))
        Assert.assertEquals("", SystemProperties.get("gsm.operator.numeric"))
        Assert.assertEquals("default-val",
            SystemProperties.get("gsm.sim.operator.alpha", "default-val"))
        Assert.assertNull(SystemProperties.find("gsm.sim.operator.alpha"))

        // non-replacements
        Assert.assertNotEquals("", SystemProperties.get("ro.product.cpu.abi"))

        // CountryDetector
        val cd = ctx.getSystemService(CountryDetector::class.java)
        if (cd != null) {
            val country = cd.detectCountry()
            if (country != null) {
                val src = country.source
                Assert.assertTrue(
                    "Country.getSource()=$src must be LOCATION or LOCALE when hidden",
                    src == Country.COUNTRY_SOURCE_LOCATION ||
                        src == Country.COUNTRY_SOURCE_LOCALE,
                )
            }
        }
    }

    @Test
    fun testCarrierInfoVisible() {
        Assert.assertFalse("HideCarrierInfo.isEnabled()", HideCarrierInfo.isEnabled())

        Assert.assertFalse(HideCarrierInfo.shouldFilter("gsm.sim.operator.alpha"))
        Assert.assertFalse(HideCarrierInfo.shouldFilter("gsm.operator.alpha"))
        Assert.assertFalse(HideCarrierInfo.shouldFilter("ro.product.cpu.abi"))

        Assert.assertNotEquals("", SystemProperties.get("ro.product.cpu.abi"))

        // skip the rest, as we cant predict the actual output
    }
}
