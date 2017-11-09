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

package io.geeteshk.hyper.git;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import java.io.File;

import io.geeteshk.hyper.R;

public abstract class GitTask extends AsyncTask<String, String, Boolean> {

    public NotificationManager manager;
    public NotificationCompat.Builder builder;
    public int id = 1;

    public Context context;
    public View rootView;
    public File repo;
    public String[] messages;

    public GitTask(Context context, View view, File repo, String[] values) {
        this.context = context;
        rootView = view;
        this.repo = repo;
        messages = values;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String id = "hyper_git_channel";
        if (Build.VERSION.SDK_INT >= 26) {
            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.git);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            manager.createNotificationChannel(channel);
        }

        builder = new NotificationCompat.Builder(context, id);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        builder.setContentTitle(messages[0])
                .setSmallIcon(R.drawable.ic_git_small)
                .setAutoCancel(false)
                .setOngoing(true);
    }

    @Override
    protected void onProgressUpdate(final String... values) {
        super.onProgressUpdate(values);
        builder.setContentText(values[0])
                .setProgress(Integer.valueOf(values[2]), Integer.valueOf(values[1]), false);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(values[0]));
        manager.notify(id, builder.build());
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            builder.setContentText(messages[1]);
        } else {
            builder.setContentText(messages[2]);
        }

        builder.setProgress(0, 0, false)
                .setAutoCancel(true)
                .setOngoing(false);
        manager.notify(id, builder.build());
    }
}
