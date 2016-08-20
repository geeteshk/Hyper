package io.geeteshk.hyper.polymer;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.geeteshk.hyper.R;

public class CatalogAdapter extends BaseAdapter {

    public static int[] mColours = {0xffddc9e6, 0xff63ab62, 0xffffffff, 0xff5cacee, 0xffffa54f, 0xff8deeee, 0xffc4c4c4, 0xffee6363};
    public static String[] mSubtitles = {
            "App Elements", "Iron Elements", "Paper Elements",
            "Google Elements", "Gold Elements",
            "Neon Elements", "Platinum Elements", "Molecules"
    };
    public static String[] mVersions = {
            "0.10.0", "1.0.10",
            "1.0.7", "1.1.0",
            "1.0.1", "1.0.0",
            "2.0.0", "1.0.0"
    };
    private Context mContext;
    private String[] mTitles = {
            "App", "Fe", "Md", "Go",
            "Au", "Ne", "Pt", "Mo"
    };
    private String[] mDescriptions = {
            "App elements",
            "Polymer core elements",
            "Material design elements",
            "Components for Google's APIs and services",
            "Ecommerce Elements",
            "Animation and Special Effects",
            "Offline, push, and more",
            "Wrappers for third-party libraries"
    };

    public CatalogAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return 8;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CardView elementCard;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            elementCard = (CardView) inflater.inflate(R.layout.element_card, parent, false);
        } else {
            elementCard = (CardView) convertView;
        }

        TextView elementTitle = (TextView) elementCard.findViewById(R.id.element_group_title);
        TextView elementSubtitle = (TextView) elementCard.findViewById(R.id.element_group_subtitle);
        TextView elementDescription = (TextView) elementCard.findViewById(R.id.element_group_description);
        TextView elementVersion = (TextView) elementCard.findViewById(R.id.element_group_version);

        elementCard.setCardBackgroundColor(mColours[position]);
        elementTitle.setText(mTitles[position]);
        elementSubtitle.setText(mSubtitles[position]);
        elementDescription.setText(mDescriptions[position]);
        elementVersion.setText(mVersions[position]);

        return elementCard;
    }
}
