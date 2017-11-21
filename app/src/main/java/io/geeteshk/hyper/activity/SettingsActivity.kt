package io.geeteshk.hyper.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.geeteshk.hyper.R
import io.geeteshk.hyper.fragment.SettingsFragment
import io.geeteshk.hyper.helper.Styles
import kotlinx.android.synthetic.main.widget_toolbar.*

class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Styles.getThemeInt(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)

        fragmentManager
                .beginTransaction()
                .replace(R.id.settingsFragment, SettingsFragment())
                .commit()
    }
}
