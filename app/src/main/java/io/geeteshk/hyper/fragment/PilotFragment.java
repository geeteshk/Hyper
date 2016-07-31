package io.geeteshk.hyper.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.util.DecorUtil;
import io.geeteshk.hyper.util.ValidatorUtil;

/**
 * Fragment used to test projects
 */
public class PilotFragment extends Fragment {

    ArrayList<String> mObjectsList;
    ProjectAdapter mProjectAdapter;

    /**
     * Default empty constructor
     */
    public PilotFragment() {
    }

    /**
     * Method used to inflate and setup view
     *
     * @param inflater           used to inflate layout
     * @param container          parent view
     * @param savedInstanceState restores state onResume
     * @return fragment view that is created
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pilot, container, false);

        final String[] objects = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Hyper").list();
        mObjectsList = new ArrayList<>(Arrays.asList(objects));
        ValidatorUtil.removeBroken(mObjectsList);
        mProjectAdapter = new ProjectAdapter(getActivity(), mObjectsList.toArray(new String[mObjectsList.size()]), false);
        final RecyclerView projectsList = (RecyclerView) rootView.findViewById(R.id.pilot_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        projectsList.setLayoutManager(layoutManager);
        projectsList.addItemDecoration(new DecorUtil.GridSpacingItemDecoration(2, DecorUtil.dpToPx(getActivity(), 4), true));
        projectsList.setItemAnimator(new DefaultItemAnimator());
        projectsList.setAdapter(mProjectAdapter);

        FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.fab_create);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.update(getActivity(), getActivity().getSupportFragmentManager(), 0);
            }
        });

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getActivity(), R.string.create_project, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        EditText projectSearch = (EditText) rootView.findViewById(R.id.project_search);
        projectSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mObjectsList = new ArrayList<>(Arrays.asList(objects));
                ValidatorUtil.removeBroken(mObjectsList);
                for (Iterator<String> iterator = mObjectsList.iterator(); iterator.hasNext(); ) {
                    String string = iterator.next();
                    if (!string.toLowerCase(Locale.getDefault()).startsWith(s.toString())) {
                        iterator.remove();
                    }
                }

                mProjectAdapter = new ProjectAdapter(getActivity(), mObjectsList.toArray(new String[mObjectsList.size()]), true);
                projectsList.setAdapter(mProjectAdapter);
            }
        });

        return rootView;
    }
}
