package io.geeteshk.hyper.polymer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Pref;

/**
 * Activity to browse polymer catalog
 */
public class CatalogActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    /**
     * Press when done
     */
    FloatingActionButton mFinishCreate;

    /**
     * Grid to display items
     */
    GridView mCatalog;

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
        setContentView(R.layout.activity_catalog);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Polymer Elements Catalog");
        }

        mFinishCreate = (FloatingActionButton) findViewById(R.id.finish_create);
        mFinishCreate.setOnClickListener(this);

        mCatalog = (GridView) findViewById(R.id.elements_catalog);
        mCatalog.setAdapter(new CatalogAdapter(this));
        mCatalog.setOnItemClickListener(this);
    }

    /**
     * When grid item is clicked
     *
     * @param parent parent view
     * @param view view
     * @param position position of view
     * @param id view id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent elementsIntent = new Intent(CatalogActivity.this, ElementsActivity.class);
         elementsIntent.putExtra("element_id", position);
        startActivity(elementsIntent);
    }

    /**
     * When FAB is clicked
     *
     * @param v FAB
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish_create:
                Intent intent = new Intent(CatalogActivity.this, SetupActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
