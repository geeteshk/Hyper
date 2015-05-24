package io.geeteshk.hyper.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import org.apache.http.HttpResponse;
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
import io.geeteshk.hyper.R;

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

        FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.fab_feedback);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isNetworkAvailable()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Submit Feedback");
                    @SuppressLint("InflateParams") final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_feedback, null);
                    builder.setView(view);
                    builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText title = (EditText) view.findViewById(R.id.title);
                            EditText content = (EditText) view.findViewById(R.id.content);
                            if (!content.getText().toString().isEmpty() || !content.getText().toString().isEmpty()) {
                                new FeedbackTask().execute(
                                        "https://api.github.com/repos/OpenMatter/Hyper/issues",
                                        title.getText().toString(),
                                        content.getText().toString()
                                );
                            }
                        }
                    });
                    AppCompatDialog dialog = builder.create();
                    dialog.show();
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
                HttpResponse httpResponse = httpClient.execute(httpPost);
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
