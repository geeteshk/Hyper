/*
 * Copyright 2016 Geetesh Kalakoti <kalakotig@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.geeteshk.hyper.helper;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;

import eu.bitwalker.useragentutils.UserAgent;

/**
 * Helper class to handle network stuffs
 */
public class NetworkUtils {

    /**
     * Log TAG
     */
    private static final String TAG = NetworkUtils.class.getSimpleName();

    /**
     * Instance of web server
     */
    private static HyperServer mServer;

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
    public static HyperServer getServer() {
        return mServer;
    }

    /**
     * Set instance of web server
     *
     * @param server to set
     */
    public static void setServer(HyperServer server) {
        mServer = server;
    }

    public static String parseUA(String ua) {
        UserAgent agent = UserAgent.parseUserAgentString(ua);
        return agent.getOperatingSystem().getName() + " / " + agent.getBrowser().getName() + " " + agent.getBrowserVersion().getVersion();
    }

    public static LinkedList<String> parseUAList(LinkedList<String> uaList) {
        LinkedList<String> parsedList = new LinkedList<>();
        for (String ua : uaList) {
            parsedList.add(parseUA(ua));
        }

        return parsedList;
    }
}
