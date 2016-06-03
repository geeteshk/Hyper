package io.geeteshk.hyper.helper;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import io.geeteshk.hyper.Constants;

public class HyperDrive extends NanoHTTPD {

    private static final String TAG = HyperDrive.class.getSimpleName();

    private String mProject;

    public HyperDrive(String project) {
        super(8080);
        mProject = project;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String reply = "";
        try {
            FileReader fileReader = new FileReader(Constants.HYPER_ROOT + File.separator + mProject + File.separator + "index.html");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                reply += line;
            }

            bufferedReader.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        return new NanoHTTPD.Response(reply);
    }
}
