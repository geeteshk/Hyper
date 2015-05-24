package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.util.JsonUtil;
import io.geeteshk.hyper.util.ProjectUtil;

/**
 * Adapter to list all projects
 */
public class ProjectAdapter extends ArrayAdapter<String> {

    /**
     * Context used for various purposes such as loading files and inflating layouts
     */
    Context mContext;

    /**
     * Id of resource to inflate
     */
    int mResource;

    /**
     * Array of objects to fill list
     */
    String[] mObjects;

    /**
     * Public constructor for adapter
     *
     * @param context  used for inflating layouts
     * @param resource id of resource to inflate
     * @param objects  list contents
     */
    public ProjectAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mObjects = objects;
    }

    /**
     * Method to inflate each view
     *
     * @param position    current position in list
     * @param convertView reusable view
     * @param parent      view above this one
     * @return view of specific position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView;

        if (convertView != null) {
            rootView = convertView;
        } else {
            rootView = inflater.inflate(mResource, parent, false);
        }

        ImageView favicon = (ImageView) rootView.findViewById(R.id.favicon);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView desc = (TextView) rootView.findViewById(R.id.desc);

        favicon.setImageBitmap(ProjectUtil.getFavicon(mObjects[position]));
        title.setText(mObjects[position]);
        desc.setText(JsonUtil.getProjectProperty(mObjects[position], "description"));

        return rootView;
    }
}
