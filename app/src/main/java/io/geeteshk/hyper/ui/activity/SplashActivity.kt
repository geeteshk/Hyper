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

package io.geeteshk.hyper.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import io.geeteshk.hyper.R
import io.geeteshk.hyper.util.Prefs.defaultPrefs
import io.geeteshk.hyper.util.Prefs.get
import io.geeteshk.hyper.util.action
import io.geeteshk.hyper.util.onAnimationStop
import io.geeteshk.hyper.util.snack
import io.geeteshk.hyper.util.startAndFinish
import io.geeteshk.hyper.util.ui.FontsOverride
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        FontsOverride.setDefaultFont(applicationContext,
                "MONOSPACE", "fonts/Inconsolata-Regular.ttf")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        hyperLogo.animate().alpha(1F).setDuration(1000).onAnimationStop {
            setupPermissions()
        }
    }

    private fun startIntro() {
        val prefs = defaultPrefs(this)
        val classTo = if (prefs["intro_done", false]!!) {
            MainActivity::class.java
        } else {
            IntroActivity::class.java
        }

        startAndFinish(Intent(this, classTo).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
    }

    private fun showPermissionSnack() {
        splashLayout.snack(R.string.permission_storage_rationale, Snackbar.LENGTH_INDEFINITE) {
            action("GRANT") {
                dismiss()
                startActivityForResult(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", packageName, null)
                }, WRITE_PERMISSION_REQUEST)
            }
        }
    }

    private fun setupPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showPermissionSnack()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_PERMISSION_REQUEST)
            }
        } else {
            startIntro()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startIntro()
            }
        } else {
            showPermissionSnack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            setupPermissions()
        }
    }

    companion object {

        private const val WRITE_PERMISSION_REQUEST = 0
    }
}
