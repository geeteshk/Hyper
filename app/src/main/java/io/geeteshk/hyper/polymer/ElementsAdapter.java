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
import io.geeteshk.hyper.util.JsonUtil;

public class ElementsAdapter extends RecyclerView.Adapter<ElementsAdapter.Holder> {

    ArrayList<Element> mElements;
    ArrayList<Element> mCheckedItems;

    public ElementsAdapter(Context context, String type) {
        mElements = JsonUtil.getElements(context, type);
        mCheckedItems = ElementsHolder.getInstance().getElements();
    }

    @Override
    public ElementsAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_item, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(ElementsAdapter.Holder holder, final int position) {
        holder.mElementBox.setText(mElements.get(position).getName());
        holder.mElementDescription.setText(mElements.get(position).getDescription());
        holder.mElementVersion.setText(mElements.get(position).getVersion());

        holder.mElementBox.setChecked(mCheckedItems.contains(mElements.get(position)));
        holder.mElementBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCheckedItems.add(mElements.get(position));
                } else {
                    mCheckedItems.remove(mElements.get(position));
                }

                ElementsHolder.getInstance().setElements(mCheckedItems);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mElements.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public CheckBox mElementBox;
        public TextView mElementDescription, mElementVersion;

        public Holder(View view) {
            super(view);
            mElementBox = (CheckBox) view.findViewById(R.id.element_check);
            mElementDescription = (TextView) view.findViewById(R.id.element_description);
            mElementVersion = (TextView) view.findViewById(R.id.element_version);
        }
    }
}
