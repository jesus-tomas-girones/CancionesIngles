package com.sacedonmg.cancionesingles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Ana María Arrufat on 20/09/2017.
 */
public class LoginActivity extends Fragment implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private String LOG_TAG = "CI:LoginActivity";
    private Context context;
    private FragmentActivity activity;
    private LinearLayout layoutSocialButtons;
    private LinearLayout layoutEmailButtons;
    private TextInputLayout wrapperPassword;
    private TextInputLayout wrapperEmail;
    private RelativeLayout container;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private TextView alertText;
    private ImageView alertImage;

    // Google
    private SignInButton btnGoogle;
    private static final int RC_GOOGLE_SIGN_IN = 123;
    private GoogleApiClient googleApiClient;
    // Email and password
    private EditText inputPassword;
    private EditText inputEmail;
    // Facebook
    private CallbackManager callbackManager;
    private LoginButton btnFacebook;

    private void bindViews(View rootView) {
        btnGoogle = (SignInButton) rootView.findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(this);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        inputEmail = (EditText) rootView.findViewById(R.id.editTxtEmail);
        inputPassword = (EditText) rootView.findViewById(R.id.editTxtPassword);
        wrapperEmail = (TextInputLayout) rootView.findViewById(R.id.wrapperEmail);
        wrapperPassword = (TextInputLayout) rootView.findViewById(R.id.wrapperPassword);
        container = (RelativeLayout) rootView.findViewById(R.id.loginContainer);
        layoutSocialButtons = (LinearLayout) rootView.findViewById(R.id.layoutSocial);
        layoutEmailButtons = (LinearLayout) rootView.findViewById(R.id.layoutEmailButtons);
        alertImage = (ImageView) rootView.findViewById(R.id.verifyEmailImage);
        alertText = (TextView) rootView.findViewById(R.id.verifyEmail);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getContext());
        auth = FirebaseSingleton.getInstance().getAuth();
        context = getContext();
        activity = getActivity();

        View rootView = inflater.inflate(R.layout.login_activity, container, false);
        bindViews(rootView);

        TabLayout tabLayout = MainActivity.getTabLayout();
        tabLayout.setVisibility(View.GONE);

        // Google login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(activity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Facebook login
        callbackManager = CallbackManager.Factory.create();
        btnFacebook = (LoginButton) rootView.findViewById(R.id.btnFacebook);
        btnFacebook.setOnClickListener(this);
        btnFacebook.setReadPermissions("email", "public_profile");
        btnFacebook.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override public void onSuccess(LoginResult loginResult) {
                        Log.d(LOG_TAG, "FACBOOK_REGISTER_SUCCESS");
                        facebookAuth(loginResult.getAccessToken());
                    }
                    @Override public void onCancel() {
                        Log.d(LOG_TAG, "FACBOOK_REGISTER_CANCEL");
                        showSnackbar(getResources().getString(R.string.error_cancelled));
                    }
                    @Override public void onError(FacebookException error) {
                        Log.d(LOG_TAG, "FACBOOK_REGISTER_ERROR");
                        showSnackbar(error.getLocalizedMessage());
                    }
                });

        doLogin();
        return rootView;
    }

    public void googleLogin() {
        showProgress();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent( googleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void facebookAuth(AccessToken accessToken) {
        final AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d(LOG_TAG, "FACBOOK_LOGIN_NOT_SUCCESSFULL");
                            hideProgress();
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                LoginManager.getInstance().logOut();
                            }

                            showSnackbar(task.getException().getLocalizedMessage());
                        } else {
                            Log.d(LOG_TAG, "FACBOOK_LOGIN_SUCCESSFULL");
                            doLogin();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGoogle:
                googleLogin();
                break;
            case R.id.btnFacebook:
                showProgress();
                break;
        }
    }

    private void googleAuth(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            hideProgress();
                            showSnackbar(task.getException().getLocalizedMessage());
                        }else{
                            doLogin();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                googleAuth(account);
            } else {
                hideProgress();
                showSnackbar(getResources().getString(R.string.error_google));
            }
        } else if (requestCode == btnFacebook.getRequestCode()) {
            Log.d(LOG_TAG, "FACBOOK_onActivityResult");
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showSnackbar(getString(R.string.error_connection_failed));
    }

    private void showSnackbar(String message) {
        Snackbar.make(container, message, Snackbar.LENGTH_SHORT).show();
    }

    private void showProgress() {
        layoutSocialButtons.setVisibility(View.GONE);
        layoutEmailButtons.setVisibility(View.GONE);
        wrapperPassword.setVisibility(View.GONE);
        wrapperEmail.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        layoutSocialButtons.setVisibility(View.VISIBLE);
        layoutEmailButtons.setVisibility(View.VISIBLE);
        wrapperPassword.setVisibility(View.VISIBLE);
        wrapperEmail.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void doLogin() {
        alertImage.setVisibility(View.INVISIBLE);
        alertText.setVisibility(View.INVISIBLE);
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.d(LOG_TAG, "currentUser is null");
            return;
        }

        Log.d(LOG_TAG, "currentUser " + currentUser.getDisplayName());
        guardarUsuario(currentUser);
        /*boolean isVerified = currentUser.isEmailVerified();
        if (!isVerified) {
            currentUser.sendEmailVerification();
            hideProgress();
            alertImage.setVisibility(View.VISIBLE);
            alertText.setVisibility(View.VISIBLE);
            return;
        }*/

        String name = currentUser.getDisplayName();
        String email = currentUser.getEmail();
        String provider = currentUser.getProviders().get(0);
        SharedPreferences pref = context.getSharedPreferences("com.sacedonmg.cancionesingles_internal", MODE_PRIVATE);
        pref.edit().putString("provider", provider).commit();
        if (name == null) name = email;
        pref.edit().putString("name", name).commit();
        if (email != null)  pref.edit().putString("email", email).commit();


        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle options = new Bundle();
        options.putInt("SCREEN", R.id.nav_tabbed_activity);
        startActivity(i, options);
    }

    public void signin(View v){
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        if (!email.isEmpty() && !password.isEmpty()) {
            showProgress();
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                doLogin();
                            } else {
                                hideProgress();
                                showSnackbar(task.getException().getLocalizedMessage());
                            }
                        }
                    });
        } else {
            wrapperEmail.setError(getString(R.string.error_empty));
        }
    }
    public void signup(View v){
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        if (!email.isEmpty() && !password.isEmpty()) {
            showProgress();
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                doLogin();
                            } else {
                                hideProgress();
                                showSnackbar(task.getException().getLocalizedMessage());
                            }
                        }
                    });
        } else {
            wrapperEmail.setError(getString(R.string.error_empty));
        }
    }

    void guardarUsuario(final FirebaseUser user) {
        DatabaseReference usersReference = FirebaseSingleton.getInstance().getUsersReference();
        final DatabaseReference currentUserReference = usersReference.child(user.getUid());
        ValueEventListener userListener = new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    currentUserReference.setValue(new User(user.getDisplayName(), user.getEmail()));
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        };
        currentUserReference.addListenerForSingleValueEvent(userListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();
    }
}
