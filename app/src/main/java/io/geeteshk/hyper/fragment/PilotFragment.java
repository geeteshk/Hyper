package io.geeteshk.hyper.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.util.DecorUtil;

/**
 * Fragment used to test projects
 */
public class PilotFragment extends Fragment {

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
        ProjectAdapter projectAdapter = new ProjectAdapter(getActivity(), objects, false);
        RecyclerView projectsList = (RecyclerView) rootView.findViewById(R.id.pilot_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        projectsList.setLayoutManager(layoutManager);
        projectsList.addItemDecoration(new DecorUtil.GridSpacingItemDecoration(2, DecorUtil.dpToPx(getActivity(), 8), true));
        projectsList.setItemAnimator(new DefaultItemAnimator());
        projectsList.setAdapter(projectAdapter);

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
                Toast.makeText(getActivity(), "Create project", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return rootView;
    }
}
