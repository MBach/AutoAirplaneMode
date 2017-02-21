package org.miamplayer.autoairplanemode;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * RootUtil class checks if device has root enabled or not.
 *
 * @author Kevin Kowalewski
 */
final class RootUtil
{
    /**
     * Combines 3 ways to check if device is rooted.
     *
     * @return true is device is rooted
     */
    static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    /**
     *
     * @return true is device is rooted
     */
    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    /**
     *
     * @return true is device is rooted
     */
    private static boolean checkRootMethod2() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    /**
     *
     * @return true is device is rooted
     */
    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
}