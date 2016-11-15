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

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class CommitTask extends GitTask {

    private static final String TAG = CommitTask.class.getSimpleName();

    public CommitTask(Context context, File repo, String[] values) {
        super(context, repo, values);
        id = 4;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        try {
            Git git = Giiit.getGit(mContext, mRepo);
            if (git != null) {
                git.commit()
                        .setMessage(strings[0])
                        .call();
            }
        } catch (GitAPIException e) {
            Log.e(TAG, e.toString());
            return false;
        }

        return true;
    }
}
