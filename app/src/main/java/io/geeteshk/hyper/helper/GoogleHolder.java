package io.geeteshk.hyper.helper;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class GoogleHolder {

    private static final GoogleHolder holder = new GoogleHolder();
    private GoogleSignInAccount mAccount;

    public static GoogleHolder getInstance() {
        return holder;
    }

    public GoogleSignInAccount getAccount() {
        return mAccount;
    }

    public void setAccount(GoogleSignInAccount account) {
        mAccount = account;
    }
}
