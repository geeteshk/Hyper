package io.geeteshk.hyper.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.adapter.FAQAdapter;

/**
 * Fragment used to show help about the IDE. A lot of work still needs to be done here.
 */
public class HelpFragment extends Fragment {

    /**
     * Default empty constructor
     */
    public HelpFragment() {
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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_help, container, false);

        RecyclerView faqList = (RecyclerView) rootView.findViewById(R.id.faq_list);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        RecyclerView.Adapter adapter = new FAQAdapter(getActivity());

        faqList.setLayoutManager(manager);
        faqList.setAdapter(adapter);

        return rootView;
    }
}
