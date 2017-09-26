package com.sacedonmg.cancionesingles;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import static com.sacedonmg.cancionesingles.MainActivity.LOGIN_SUCCESS;
import static com.sacedonmg.cancionesingles.UtilidadesCanciones.mostrarMensaje;

public class LoginActivity extends Fragment {
    private LinearLayout layoutSocialButtons;
    private LinearLayout layoutEmailButtons;
    private TextInputLayout wrapperPassword;
    private TextInputLayout wrapperEmail;
    private RelativeLayout container;
    private ProgressBar progressBar;
    private TwitterLoginButton btnTwitter;
    private FirebaseAuth auth;
    private TextView alertText;
    private ImageView alertImage;

    // Email and password
    private EditText inputPassword;
    private EditText inputEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getContext());
        MainActivity.getTabLayout().setVisibility(View.GONE);
        View rootView = inflater.inflate(R.layout.login_activity, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        inputEmail = (EditText) rootView.findViewById(R.id.editTxtEmail);
        inputPassword = (EditText) rootView.findViewById(R.id.editTxtPassword);
        wrapperEmail = (TextInputLayout) rootView.findViewById(R.id.wrapperEmail);
        wrapperPassword = (TextInputLayout) rootView.findViewById(R.id.wrapperPassword);
        container = (RelativeLayout) rootView.findViewById(R.id.loginContainer);
        auth = FirebaseSingleton.getInstance().getAuth();

        rootView.findViewById(R.id.btnSignup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup(view);
            }
        });
        rootView.findViewById(R.id.btnSignin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signin(view);
            }
        });

        alertImage = (ImageView) rootView.findViewById(R.id.verifyEmailImage);
        alertText = (TextView) rootView.findViewById(R.id.verifyEmail);
        doLogin();
        return rootView;
    }

    private void showSnackbar(String message) {
        mostrarMensaje(getContext(), message);
    }

    private void showProgress() {
        wrapperPassword.setVisibility(View.GONE);
        wrapperEmail.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        wrapperPassword.setVisibility(View.VISIBLE);
        wrapperEmail.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void doLogin() {
        alertImage.setVisibility(View.INVISIBLE);
        alertText.setVisibility(View.INVISIBLE);
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            guardarUsuario(currentUser);
            boolean isVerified = currentUser.isEmailVerified();
            if (!isVerified) {
                currentUser.sendEmailVerification();
                hideProgress();
                alertImage.setVisibility(View.VISIBLE);
                alertText.setVisibility(View.VISIBLE);
                return;
            }

            Intent i = new Intent(getContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra("result", LOGIN_SUCCESS);
            startActivity(i);
        }
    }

    public void signin(View v){
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        if (!email.isEmpty() && !password.isEmpty()) {
            showProgress();
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
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
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
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
}
