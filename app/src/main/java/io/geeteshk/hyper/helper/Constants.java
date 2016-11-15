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

import android.os.Environment;

import java.io.File;

/**
 * Constant values used around app
 */
public class Constants {

    /**
     * Hyper root directory in sdcard
     */
    public static final String HYPER_ROOT = Environment.getExternalStorageDirectory().getPath() + File.separator + "Hyper";

    /**
     * Application package
     */
    public static final String PACKAGE = "io.geeteshk.hyper";

    /**
     * Firebase Global Storage Bucket
     */
    public static final String GS_BUCKET = "gs://hyper-a0ee4.appspot.com";

    /**
     * Literally in the name
     */
    public static final int ONE_MEGABYTE = 1024 * 1024;
}
