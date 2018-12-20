package io.geeteshk.hyper.ui.activity

import android.os.Bundle
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.fragment.SettingsFragment
import kotlinx.android.synthetic.main.widget_toolbar.*

class SettingsActivity : ThemedActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settingsFragment, SettingsFragment())
                .commit()
    }
}
