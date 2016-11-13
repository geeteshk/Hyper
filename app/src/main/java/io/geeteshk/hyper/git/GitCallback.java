package io.geeteshk.hyper.git;

public interface GitCallback {

    void onPreExecute(String title);
    void onProgressUpdate(String... values);
    void onPostExecute();
}
