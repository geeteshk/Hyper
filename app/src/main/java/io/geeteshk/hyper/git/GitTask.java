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

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.view.View;

import java.io.File;

import io.geeteshk.hyper.R;

public abstract class GitTask extends AsyncTask<String, String, Boolean> {

    public NotificationManager mManager;
    public NotificationCompat.Builder mBuilder;
    public int id = 1;

    public Context mContext;
    public View mView;
    public File mRepo;
    public String[] mValues;

    public GitTask(Context context, View view, File repo, String[] values) {
        mContext = context;
        mView = view;
        mRepo = repo;
        mValues = values;
        mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBuilder.setContentTitle(mValues[0])
            .setSmallIcon(R.drawable.ic_git_small);
    }

    @Override
    protected void onProgressUpdate(final String... values) {
        super.onProgressUpdate(values);
        mBuilder.setContentText(values[0])
                .setProgress(Integer.valueOf(values[2]), Integer.valueOf(values[1]), false);

        mManager.notify(id, mBuilder.build());
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean) {
            mBuilder.setContentText(mValues[1]);
        } else {
            mBuilder.setContentText(mValues[2]);
        }

        mBuilder.setProgress(0, 0, false);
        mManager.notify(id, mBuilder.build());
    }
}
