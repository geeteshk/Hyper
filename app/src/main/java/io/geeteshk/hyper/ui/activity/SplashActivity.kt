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
import android.animation.Animator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import de.psdev.licensesdialog.LicenseResolver
import io.geeteshk.hyper.R
import io.geeteshk.hyper.util.ui.FontsOverride
import io.geeteshk.hyper.util.Prefs.defaultPrefs
import io.geeteshk.hyper.util.Prefs.get
import io.geeteshk.hyper.util.ui.Styles
import io.geeteshk.hyper.license.EclipseDistributionLicense10
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        LicenseResolver.registerLicense(EclipseDistributionLicense10())
        FontsOverride.setDefaultFont(applicationContext, "MONOSPACE", "fonts/Inconsolata-Regular.ttf")

        setTheme(Styles.getThemeInt(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        hyperLogo.animate().alpha(1F).setDuration(1000).setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                setupPermissions()
            }

            override fun onAnimationCancel(animator: Animator) {
                setupPermissions()
            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })
    }

    private fun startIntro() {
        val prefs = defaultPrefs(this)
        var classTo: Class<*> = IntroActivity::class.java
        if (prefs["intro_done", false]!!) {
            classTo = MainActivity::class.java
        }

        val intent = Intent(this@SplashActivity, classTo)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun setupPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val snackbar = Snackbar.make(splashLayout, getString(R.string.permission_storage_rationale), Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction("GRANT") {
                    snackbar.dismiss()
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, WRITE_PERMISSION_REQUEST)
                }

                snackbar.show()
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
            val snackbar = Snackbar.make(splashLayout, getString(R.string.permission_storage_rationale), Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction("GRANT") {
                snackbar.dismiss()
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, WRITE_PERMISSION_REQUEST)
            }

            snackbar.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            setupPermissions()
        }
    }

    companion object {

        private val WRITE_PERMISSION_REQUEST = 0
    }
}
