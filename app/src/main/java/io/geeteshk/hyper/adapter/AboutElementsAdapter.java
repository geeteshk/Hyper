package io.geeteshk.hyper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Jason;
import io.geeteshk.hyper.polymer.Element;

/**
 * Adapter to display elements of a project and their descriptions
 */
public class AboutElementsAdapter extends RecyclerView.Adapter<AboutElementsAdapter.ViewHolder> {

    /**
     * List of polymer elements
     */
    private ArrayList<Element> mElements;

    /**
     * public Constructor
     *
     * @param project project that is being worked on
     */
    public AboutElementsAdapter(String project) {
        if (!new File(Constants.HYPER_ROOT + File.separator + project + File.separator + "packages.hyper").exists()) {
            mElements = new ArrayList<>();
            mElements.add(new Element("No Polymer elements installed.", "Please select Polymer from the overflow to add elements to your project.", "", ""));
        } else {
            mElements = Jason.getPreviousElements(project);
        }
    }

    /**
     * When view holder is create
     *
     * @param parent parent view
     * @param viewType type of view
     * @return AboutElementsAdapter.ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_polymer_element, parent, false);
        return new ViewHolder(rootView);
    }

    /**
     * View holder is bound to a position
     *
     * @param holder view holder
     * @param position position of item
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTitle.setText(mElements.get(position).getName());
        holder.mDescription.setText(mElements.get(position).getDescription());
        holder.mVersion.setText(mElements.get(position).getVersion());
    }

    /**
     * Gets the size of objects
     *
     * @return array size
     */
    @Override
    public int getItemCount() {
        return mElements.size();
    }

    /**
     * ViewHolder class for RecyclerView
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * Views in holder
         */
        TextView mTitle;
        TextView mDescription;
        TextView mVersion;

        /**
         * Constructor
         *
         * @param view parent view
         */
        ViewHolder(View view) {
            super(view);
            mTitle = (TextView) view.findViewById(R.id.element_title_about);
            mDescription = (TextView) view.findViewById(R.id.element_description_about);
            mVersion = (TextView) view.findViewById(R.id.element_version_about);
        }
    }
}
