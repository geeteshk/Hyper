package io.geeteshk.hyper.fragment;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.ProjectActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.ProjectAdapter;
import io.geeteshk.hyper.util.ProjectUtil;

public class ImproveFragment extends Fragment {

    public ImproveFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_improve, container, false);

        final String[] objects = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Hyper").list();
        ListView listView = (ListView) rootView.findViewById(R.id.project_list);
        listView.setAdapter(new ProjectAdapter(getActivity(), R.layout.item_project, objects));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ProjectActivity.class);
                intent.putExtra("project", objects[position]);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete " + objects[position] + "?");
                builder.setMessage("Are you sure you want to do this?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
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

                builder.setNegativeButton("NO", null);
                builder.show();

                return true;
            }
        });

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
