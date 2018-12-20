package io.geeteshk.hyper.ui.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import io.geeteshk.hyper.util.ui.Styles

abstract class ThemedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        setTheme(Styles.getThemeInt(this))
        super.onCreate(savedInstanceState, persistentState)
    }
}