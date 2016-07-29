package io.geeteshk.hyper.util;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import io.geeteshk.hyper.helper.HyperDrive;

public class NetworkUtil {

    private static final String TAG = NetworkUtil.class.getSimpleName();

    private static HyperDrive mDrive;

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

    public static HyperDrive getDrive() {
        return mDrive;
    }

    public static void setDrive(HyperDrive drive) {
        mDrive = drive;
    }
}
