package io.geeteshk.hyper.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.getbase.floatingactionbutton.FloatingActionButton;

import io.geeteshk.hyper.helper.Constants;
import io.geeteshk.hyper.R;

public class AssistFragment extends Fragment {

    public AssistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_assist, container, false);

        FloatingActionButton donate = (FloatingActionButton) rootView.findViewById(R.id.donate_to_dev);
        FloatingActionButton contribute = (FloatingActionButton) rootView.findViewById(R.id.contribute_to_app);

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

        contribute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(Constants.GITHUB_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }
}
