package io.geeteshk.hyper.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Constants;

/**
 * Fragment to display LICENSE and link to GitHub
 */
public class AssistFragment extends Fragment {

    /**
     * public Constructor
     */
    public AssistFragment() {
    }

    /**
     * Called when fragment view is created
     *
     * @param inflater used to inflate layout resource
     * @param container parent view
     * @param savedInstanceState state to be restored
     * @return inflated view
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_assist, container, false);

        FloatingActionButton donate = (FloatingActionButton) rootView.findViewById(R.id.donate_to_dev);

        donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(Constants.PAYPAL_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }
}
