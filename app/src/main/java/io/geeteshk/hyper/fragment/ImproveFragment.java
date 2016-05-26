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

public class ImproveFragment extends Fragment {

    public ImproveFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_improve, container, false);

        final String[] objects = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Hyper").list();
        ProjectAdapter projectAdapter = new ProjectAdapter(getActivity(), objects, true);
        RecyclerView projectsList = (RecyclerView) rootView.findViewById(R.id.project_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        projectsList.setLayoutManager(layoutManager);
        projectsList.addItemDecoration(new DecorUtil.GridSpacingItemDecoration(2, DecorUtil.dpToPx(getActivity(), 8), true));
        projectsList.setItemAnimator(new DefaultItemAnimator());
        projectsList.setAdapter(projectAdapter);

        /*ListView listView = (ListView) rootView.findViewById(R.id.project_list);
        listView.setAdapter(new ProjectAdapter(getActivity(), R.layout.item_project, objects));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                if (PreferenceUtil.get(getActivity(), "pin", "").equals("")) {
                    intent = new Intent(getActivity(), ProjectActivity.class);
                    intent.putExtra("project", objects[position]);
                    startActivityForResult(intent, 0);
                } else {
                    intent = new Intent(getActivity(), EncryptActivity.class);
                    intent.putExtra("project", objects[position]);
                    startActivity(intent);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete " + objects[position] + "?");
                builder.setMessage("This change cannot be undone.");
                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ProjectUtil.deleteProject(objects[position])) {
                            view.animate().alpha(0).setDuration(300).setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    MainActivity.update(getActivity(), getActivity().getSupportFragmentManager(), 1);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    MainActivity.update(getActivity(), getActivity().getSupportFragmentManager(), 1);
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                            Toast.makeText(getActivity(), "Goodbye " + objects[position] + ".", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(getActivity(), "Oops! Something went wrong while deleting " + objects[position] + ".", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("CANCEL", null);
                builder.show();

                return true;
            }
        });*/

        FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.fab_create);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerFragment.select(getActivity(), 0);
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
