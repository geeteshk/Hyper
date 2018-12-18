package io.geeteshk.hyper.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.fragment.SettingsFragment
import io.geeteshk.hyper.util.ui.Styles
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
