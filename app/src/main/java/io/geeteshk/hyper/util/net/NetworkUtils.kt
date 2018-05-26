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

package io.geeteshk.hyper.util.net

import android.util.Log
import eu.bitwalker.useragentutils.UserAgent
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

object NetworkUtils {

    private val TAG = NetworkUtils::class.java.simpleName

    var server: HyperServer? = null

    val ipAddress: String?
        get() {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement() as NetworkInterface
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement() as InetAddress
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            return inetAddress.getHostAddress()
                        }
                    }
                }
            } catch (ex: SocketException) {
                Log.e(TAG, ex.message)
            }

            return null
        }

    fun parseUA(ua: String): String {
        val agent = UserAgent.parseUserAgentString(ua)
        return agent.operatingSystem.getName() + " / " + agent.browser.getName() + " " + agent.browserVersion.version
    }

    fun parseUAList(uaList: LinkedList<String>): LinkedList<String> =
            uaList.mapTo(LinkedList()) { parseUA(it) }
}
