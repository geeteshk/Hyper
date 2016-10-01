package io.geeteshk.hyper.polymer;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Decor;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.text.HtmlCompat;

/**
 * Activity to display elements
 */
public class ElementsActivity extends AppCompatActivity {

    /**
     * Method called when activity is created
     *
     * @param savedInstanceState previously stored state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Pref.get(this, "dark_theme", false)) {
            setTheme(R.style.Hyper_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elements);

        int mElementId = getIntent().getIntExtra("element_id", -1);
        float[] hsv = new float[3];
        int color = CatalogAdapter.mColours[mElementId];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        color = Color.HSVToColor(hsv);

        Decor.setStatusBarColor(this, color);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=\"#333333\">" + CatalogAdapter.mSubtitles[mElementId] + "</font>"));
            getSupportActionBar().setSubtitle(HtmlCompat.fromHtml("<font color=\"#333333\">" + CatalogAdapter.mVersions[mElementId] + "</font>"));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(CatalogAdapter.mColours[mElementId]));
        }

        RecyclerView mElementsList = (RecyclerView) findViewById(R.id.elements_list);
        mElementsList.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mElementsList.setLayoutManager(mLayoutManager);

        String elementType;
        switch (mElementId) {
            case 0:
                elementType = "app";
                break;
            case 1:
                elementType = "iron";
                break;
            case 2:
                elementType = "paper";
                break;
            case 3:
                elementType = "google";
                break;
            case 4:
                elementType = "gold";
                break;
            case 5:
                elementType = "neon";
                break;
            case 6:
                elementType = "platinum";
                break;
            case 7:
                elementType = "molecules";
                break;
            default:
                elementType = "";
        }

        RecyclerView.Adapter mElementsAdapter = new ElementsAdapter(this, elementType);
        mElementsList.setAdapter(mElementsAdapter);
    }
}
