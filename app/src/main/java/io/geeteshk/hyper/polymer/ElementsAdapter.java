package io.geeteshk.hyper.polymer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Jason;

/**
 * Adapter for elements
 */
class ElementsAdapter extends RecyclerView.Adapter<ElementsAdapter.Holder> {

    /**
     * Elements and checked items
     */
    private ArrayList<Element> mElements;
    private ArrayList<Element> mCheckedItems;

    /**
     * Constructor
     *
     * @param context context to get json
     * @param type which catalog
     */
    ElementsAdapter(Context context, String type) {
        mElements = Jason.getElements(context, type);
        mCheckedItems = ElementsHolder.getInstance().getElements();
    }

    /**
     * When view holder is created
     *
     * @param parent parent view
     * @param viewType type of view
     * @return ElementsAdapter.Holder
     */
    @Override
    public ElementsAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_item, parent, false);
        return new Holder(v);
    }

    /**
     * Called when item is bound to position
     *
     * @param holder view holder
     * @param position position of item
     */
    @Override
    public void onBindViewHolder(ElementsAdapter.Holder holder, int position) {
        final int newPos = holder.getAdapterPosition();
        holder.mElementBox.setText(mElements.get(newPos).getName());
        holder.mElementDescription.setText(mElements.get(newPos).getDescription());
        holder.mElementVersion.setText(mElements.get(newPos).getVersion());

        holder.mElementBox.setChecked(mCheckedItems.contains(mElements.get(newPos)));
        holder.mElementBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCheckedItems.add(mElements.get(newPos));
                } else {
                    mCheckedItems.remove(mElements.get(newPos));
                }

                ElementsHolder.getInstance().setElements(mCheckedItems);
            }
        });
    }

    /**
     * Get number of elements
     *
     * @return number of elements
     */
    @Override
    public int getItemCount() {
        return mElements.size();
    }

    /**
     * Holder class
     */
    static class Holder extends RecyclerView.ViewHolder {

        /**
         * Holder views
         */
        CheckBox mElementBox;
        TextView mElementDescription, mElementVersion;

        /**
         * Constructor
         *
         * @param view view
         */
        Holder(View view) {
            super(view);
            mElementBox = (CheckBox) view.findViewById(R.id.element_check);
            mElementDescription = (TextView) view.findViewById(R.id.element_description);
            mElementVersion = (TextView) view.findViewById(R.id.element_version);
        }
    }
}
