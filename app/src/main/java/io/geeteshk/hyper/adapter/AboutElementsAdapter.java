package io.geeteshk.hyper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import io.geeteshk.hyper.Constants;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.polymer.Element;
import io.geeteshk.hyper.util.JsonUtil;

public class AboutElementsAdapter extends RecyclerView.Adapter<AboutElementsAdapter.ViewHolder> {

    ArrayList<Element> mElements;

    public AboutElementsAdapter(String project) {
        if (!new File(Constants.HYPER_ROOT + File.separator + project + File.separator + "packages.hyper").exists()) {
            mElements = new ArrayList<>();
            mElements.add(new Element("No Polymer elements installed.", "Please select Polymer from the overflow to add elements to your project.", "", ""));
        } else {
            mElements = JsonUtil.getPreviousElements(project);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_polymer_element, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTitle.setText(mElements.get(position).getName());
        holder.mDescription.setText(mElements.get(position).getDescription());
        holder.mVersion.setText(mElements.get(position).getVersion());
    }

    @Override
    public int getItemCount() {
        return mElements.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTitle;
        public TextView mDescription;
        public TextView mVersion;

        public ViewHolder(View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.element_title_about);
            mDescription = (TextView) view.findViewById(R.id.element_description_about);
            mVersion = (TextView) view.findViewById(R.id.element_version_about);
        }
    }
}
