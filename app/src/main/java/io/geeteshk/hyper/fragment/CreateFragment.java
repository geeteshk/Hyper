package io.geeteshk.hyper.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ObservableScrollView;

import java.io.InputStream;

import io.geeteshk.hyper.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.util.PreferenceUtil;
import io.geeteshk.hyper.util.ProjectUtil;
import io.geeteshk.hyper.util.ValidatorUtil;

/**
 * Fragment used to create projects
 */
public class CreateFragment extends Fragment {

    /**
     * Intent code for selecting an icon
     */
    public static final int SELECT_ICON = 100;
    /**
     * Input fields for project parameters
     */
    EditText mName, mAuthor, mDescription, mKeywords;
    /**
     * Options to choose favicon
     */
    RadioButton mDefaultIcon, mChooseIcon;
    /**
     * Favicon preview
     */
    ImageView mIcon;
    /**
     * InputStream to read image from strorage
     */
    InputStream mStream;

    /**
     * Default empty constructor
     */
    public CreateFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_create, container, false);

        mName = (EditText) rootView.findViewById(R.id.name);
        mAuthor = (EditText) rootView.findViewById(R.id.author);
        mDescription = (EditText) rootView.findViewById(R.id.description);
        mKeywords = (EditText) rootView.findViewById(R.id.keywords);

        mDefaultIcon = (RadioButton) rootView.findViewById(R.id.default_icon);
        mChooseIcon = (RadioButton) rootView.findViewById(R.id.choose_icon);
        mIcon = (ImageView) rootView.findViewById(R.id.favicon_image);

        mName.setText(PreferenceUtil.get(getActivity(), "name", ""));
        mAuthor.setText(PreferenceUtil.get(getActivity(), "author", ""));
        mDescription.setText(PreferenceUtil.get(getActivity(), "description", ""));
        mKeywords.setText(PreferenceUtil.get(getActivity(), "keywords", ""));

        mDefaultIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mIcon.setImageResource(R.drawable.icon);
                }
            }
        });

        mChooseIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_ICON);
                }
            }
        });

        ObservableScrollView scrollView = (ObservableScrollView) rootView.findViewById(R.id.scroll_view);
        final FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.fab);
        button.attachToScrollView(scrollView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ValidatorUtil.validate(getActivity(), mName.getText().toString(), mAuthor.getText().toString(), mDescription.getText().toString(), mKeywords.getText().toString())) {
                    PreferenceUtil.store(getActivity(), "name", mName.getText().toString());
                    PreferenceUtil.store(getActivity(), "author", mAuthor.getText().toString());
                    PreferenceUtil.store(getActivity(), "description", mDescription.getText().toString());
                    PreferenceUtil.store(getActivity(), "keywords", mKeywords.getText().toString());

                    ProjectUtil.generate(getActivity(), mName.getText().toString(), mAuthor.getText().toString(), mDescription.getText().toString(), mKeywords.getText().toString(), mStream);
                    MainActivity.update(getActivity(), getActivity().getSupportFragmentManager(), 1);
                    DrawerFragment.select(getActivity(), 1);
                }
            }
        });

        return rootView;
    }

    /**
     * Called when returning from an external activity
     *
     * @param requestCode code used to request intent
     * @param resultCode  code returned from activity
     * @param data        data returned from activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_ICON:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    try {
                        Uri selectedImage = data.getData();
                        mStream = getActivity().getContentResolver().openInputStream(selectedImage);
                        mIcon.setImageBitmap(decodeUri(selectedImage));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
        }
    }

    /**
     * Method to prevent OOM errors when calling setImageBitmap()
     *
     * @param selectedImage uri of the image
     * @return resized bitmap
     */
    private Bitmap decodeUri(Uri selectedImage) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 140;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o2);
        } catch (Exception e) {
            return null;
        }
    }
}
