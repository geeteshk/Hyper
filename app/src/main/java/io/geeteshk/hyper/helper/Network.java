package io.geeteshk.hyper.helper;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Helper class to handle network stuffs
 */
public class Network {

    /**
     * Log TAG
     */
    private static final String TAG = Network.class.getSimpleName();

    /**
     * Instance of web server
     */
    private static Hyperion mDrive;

    /**
     * Gets device IP Address
     *
     * @return ip address
     */
    public static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.getMessage());
        }

        return null;
    }

    /**
     * Get current instance of web server
     *
     * @return web server
     */
    public static Hyperion getDrive() {
        return mDrive;
    }

    /**
     * Set instance of web server
     *
     * @param drive to set
     */
    public static void setDrive(Hyperion drive) {
        mDrive = drive;
    }
}
