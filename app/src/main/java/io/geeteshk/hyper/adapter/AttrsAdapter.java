package io.geeteshk.hyper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

import io.geeteshk.hyper.R;

public class AttrsAdapter extends RecyclerView.Adapter<AttrsAdapter.AttrsHolder> {

    public ArrayList<Attribute> attributes;

    public AttrsAdapter(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public AttrsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attr, parent, false);
        return new AttrsHolder(rootView);
    }

    @Override
    public void onBindViewHolder(AttrsHolder holder, int position) {
        holder.attrKey.setText(attributes.get(position).getKey());
        holder.attrValue.setText(attributes.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return attributes.size();
    }

    public class AttrsHolder extends RecyclerView.ViewHolder {
        public TextView attrKey, attrValue;

        public AttrsHolder(View view) {
            super(view);
            attrKey = (TextView) view.findViewById(R.id.attr_key);
            attrValue = (TextView) view.findViewById(R.id.attr_value);
        }
    }
}
