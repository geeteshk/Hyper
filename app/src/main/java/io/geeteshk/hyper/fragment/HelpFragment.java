package io.geeteshk.hyper.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

    private static final String TAG = HelpFragment.class.getSimpleName();

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

        FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.fab_feedback);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isNetworkAvailable()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.submit_feedback);
                    @SuppressLint("InflateParams") final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_feedback, null);
                    builder.setView(view);
                    builder.setPositiveButton(R.string.submit, null);
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
                                titleLayout.setError(getString(R.string.error_feedback_title));
                                titleLayout.setErrorEnabled(true);
                            } else if (content.getText().toString().equals("")) {
                                contentLayout.setError(getString(R.string.error_feedback_content));
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
                    Toast.makeText(getActivity(), R.string.oops_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getActivity(), R.string.submit_feedback, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return rootView;
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
                Log.e(TAG, e.getMessage());
            }

            try {
                object.put("body", params[2] + "\n\nFeedback from Hyper at " + java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + "." + "\nInstalled on " + Build.MODEL + ".");
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }

            try {
                httpPost.setEntity(new StringEntity(object.toString()));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getMessage());
            }

            try {
                httpClient.execute(httpPost);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
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
            Toast.makeText(getActivity(), R.string.feedback_thanks, Toast.LENGTH_SHORT).show();
        }
    }
}
