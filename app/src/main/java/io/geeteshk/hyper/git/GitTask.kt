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

package io.geeteshk.hyper.git

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.view.View
import androidx.core.app.NotificationCompat
import io.geeteshk.hyper.R
import org.eclipse.jgit.lib.BatchingProgressMonitor
import java.io.File
import java.lang.ref.WeakReference

abstract class GitTask(var context: WeakReference<Context>, var rootView: WeakReference<View>,
                       var repo: File, private var messages: Array<String>)
    : AsyncTask<String, String, Boolean>() {

    val progressMonitor = object : BatchingProgressMonitor() {

        override fun onUpdate(taskName: String?, workCurr: Int, workTotal: Int, percentDone: Int) {
            publishProgress(taskName, percentDone.toString(), workCurr.toString(), workTotal.toString())
        }

        override fun onEndTask(taskName: String?, workCurr: Int, workTotal: Int, percentDone: Int) {
            publishProgress(taskName, workCurr.toString(), workTotal.toString())
        }

        override fun onUpdate(taskName: String?, workCurr: Int) {}
        override fun onEndTask(taskName: String?, workCurr: Int) {}
    }

    var manager: NotificationManager = context.get()!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var builder: NotificationCompat.Builder
    var id = 1

    init {

        val id = "hyper_git_channel"
        if (Build.VERSION.SDK_INT >= 26) {
            val name = context.get()!!.getString(R.string.app_name)
            val description = context.get()!!.getString(R.string.git)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            manager.createNotificationChannel(channel)
        }

        builder = NotificationCompat.Builder(context.get()!!, id)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        builder.setContentTitle(messages[0])
                .setSmallIcon(R.drawable.ic_git_small)
                .setAutoCancel(false)
                .setOngoing(true)
    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        builder.setContentText(values[0])
                .setProgress(Integer.valueOf(values[2]), Integer.valueOf(values[1]), false)
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(values[0]))
        manager.notify(id, builder.build())
    }

    override fun onPostExecute(aBoolean: Boolean?) {
        super.onPostExecute(aBoolean)
        builder.setContentText(messages[if (aBoolean!!) { 1 } else { 2 }])
        builder.setProgress(0, 0, false)
                .setAutoCancel(true)
                .setOngoing(false)
        manager.notify(id, builder.build())
    }
}
