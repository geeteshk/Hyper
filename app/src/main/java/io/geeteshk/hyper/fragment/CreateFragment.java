package io.geeteshk.hyper.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.pavelsikun.vintagechroma.ChromaDialog;
import com.pavelsikun.vintagechroma.ChromaUtil;
import com.pavelsikun.vintagechroma.IndicatorMode;
import com.pavelsikun.vintagechroma.OnColorSelectedListener;
import com.pavelsikun.vintagechroma.colormode.ColorMode;

import java.io.InputStream;

import io.geeteshk.hyper.activity.MainActivity;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Firebase;
import io.geeteshk.hyper.helper.Pref;
import io.geeteshk.hyper.helper.Project;
import io.geeteshk.hyper.helper.Validator;

/**
 * Fragment used to create projects
 */
public class CreateFragment extends Fragment {

    /**
     * Intent code for selecting an icon
     */
    public static final int SELECT_ICON = 100;
    private static final String TAG = CreateFragment.class.getSimpleName();
    /**
     * Input fields for project parameters
     */
    TextInputLayout mNameLayout, mAuthorLayout, mDescriptionLayout, mKeywordsLayout;
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

    TextView mColor;

    FirebaseAuth mAuth;
    FirebaseStorage mStorage;

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

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mNameLayout = (TextInputLayout) rootView.findViewById(R.id.name_layout);
        mAuthorLayout = (TextInputLayout) rootView.findViewById(R.id.author_layout);
        mDescriptionLayout = (TextInputLayout) rootView.findViewById(R.id.description_layout);
        mKeywordsLayout = (TextInputLayout) rootView.findViewById(R.id.keywords_layout);

        mDefaultIcon = (RadioButton) rootView.findViewById(R.id.default_icon);
        mChooseIcon = (RadioButton) rootView.findViewById(R.id.choose_icon);
        mIcon = (ImageView) rootView.findViewById(R.id.favicon_image);
        mColor = (TextView) rootView.findViewById(R.id.color);

        assert mNameLayout.getEditText() != null;
        assert mAuthorLayout.getEditText() != null;
        assert mDescriptionLayout.getEditText() != null;
        assert mKeywordsLayout.getEditText() != null;

        mNameLayout.getEditText().setText(Pref.get(getActivity(), "name", ""));
        mAuthorLayout.getEditText().setText(Pref.get(getActivity(), "author", ""));
        mDescriptionLayout.getEditText().setText(Pref.get(getActivity(), "description", ""));
        mKeywordsLayout.getEditText().setText(Pref.get(getActivity(), "keywords", ""));
        mColor.setText(ChromaUtil.getFormattedColorString(Pref.get(getActivity(), "color", Color.BLACK), false));
        mColor.setTextColor(Pref.get(getActivity(), "color", Color.BLACK));

        mDefaultIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mIcon.setImageResource(R.drawable.icon);
                    mStream = null;
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

        mColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChromaDialog.Builder()
                        .initialColor(mColor.getCurrentTextColor())
                        .colorMode(ColorMode.RGB)
                        .indicatorMode(IndicatorMode.HEX)
                        .onColorSelected(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(@ColorInt int color) {
                                mColor.setText(ChromaUtil.getFormattedColorString(color, false));
                                mColor.setTextColor(color);
                            }
                        })
                        .create()
                        .show(getActivity().getSupportFragmentManager(), "Choose a colour for this project");
            }
        });

        final FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validator.validate(getActivity(), mNameLayout, mAuthorLayout, mDescriptionLayout, mKeywordsLayout)) {
                    Pref.store(getActivity(), "name", mNameLayout.getEditText().getText().toString());
                    Pref.store(getActivity(), "author", mAuthorLayout.getEditText().getText().toString());
                    Pref.store(getActivity(), "description", mDescriptionLayout.getEditText().getText().toString());
                    Pref.store(getActivity(), "keywords", mKeywordsLayout.getEditText().getText().toString());
                    Pref.store(getActivity(), "color", mColor.getCurrentTextColor());

                    Project.generate(getActivity(), mNameLayout.getEditText().getText().toString(), mAuthorLayout.getEditText().getText().toString(), mDescriptionLayout.getEditText().getText().toString(), mKeywordsLayout.getEditText().getText().toString(), mColor.getText().toString(), mStream);
                    Firebase.uploadProject(mAuth, mStorage, mNameLayout.getEditText().getText().toString(), false, true);
                    MainActivity.update(getActivity(), getActivity().getSupportFragmentManager(), 1);
                }
            }
        });

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getActivity(), R.string.create_project, Toast.LENGTH_SHORT).show();
                return true;
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
                        Log.e(TAG, e.getMessage());
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
            Log.e(TAG, e.getMessage());
            return null;
        }
    }
}
