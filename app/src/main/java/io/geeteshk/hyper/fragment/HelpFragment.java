package io.geeteshk.hyper.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import io.geeteshk.hyper.Bot;
import io.geeteshk.hyper.Constants;
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

        ListView faqList = (ListView) rootView.findViewById(R.id.faq_list);
        faqList.setAdapter(new FAQAdapter(getActivity(), R.layout.item_faq));
        faqList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 4) showInstalledAppDetails(Constants.PACKAGE);
            }
        });

        FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.fab_feedback);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isNetworkAvailable()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Submit Feedback");
                    @SuppressLint("InflateParams") final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_feedback, null);
                    builder.setView(view);
                    builder.setPositiveButton("SUBMIT", null);
                    final AppCompatDialog dialog = builder.create();
                    dialog.show();

                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText title = (EditText) view.findViewById(R.id.title);
                            EditText content = (EditText) view.findViewById(R.id.content);
                            TextInputLayout titleLayout = (TextInputLayout) view.findViewById(R.id.title_layout);
                            TextInputLayout contentLayout = (TextInputLayout) view.findViewById(R.id.content_layout);
                            if (title.getText().toString().equals("")) {
                                titleLayout.setError("Please enter a title.");
                                titleLayout.setErrorEnabled(true);
                            } else if (content.getText().toString().equals("")) {
                                contentLayout.setError("Please enter some content.");
                                contentLayout.setErrorEnabled(true);
                            } else {
                                new FeedbackTask().execute(
                                        Constants.GITHUB_ISSUES_URL,
                                        title.getText().toString(),
                                        content.getText().toString()
                                );

                                dialog.dismiss();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Oops! Looks like you aren't connected to the internet.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getActivity(), "Submit feedback", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return rootView;
    }

    private void showInstalledAppDetails(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts(Constants.SCHEME, packageName, null);
        intent.setData(uri);
        getActivity().startActivity(intent);
    }

    /**
     * Method to check connectivity status
     *
     * @return true if network is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Task to submit feedback using Github API
     */
    private class FeedbackTask extends AsyncTask<String, String, String> {

        /**
         * Submit feedback in background
         *
         * @param params parameters for task
         * @return null
         */
        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(params[0]);

            httpPost.addHeader(BasicScheme.authenticate(
                    new UsernamePasswordCredentials(Bot.USERNAME, Bot.PASSWORD),
                    HTTP.UTF_8, false));

            JSONObject object = new JSONObject();

            try {
                object.put("title", params[1]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                object.put("body", params[2] + "\n\nFeedback from Hyper at " + java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + "." + "\nInstalled on " + Build.MODEL + ".");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                httpPost.setEntity(new StringEntity(object.toString()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                httpClient.execute(httpPost);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Show message if successful
         *
         * @param s not important
         */
        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getActivity(), "Feedback sent. Thank you. :)", Toast.LENGTH_SHORT).show();
        }
    }
}
