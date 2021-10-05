package com.example.pull_and_do;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.r0adkll.slidr.model.SlidrInterface;

public class EmailPasswordActivity extends AppCompatActivity implements
        View.OnClickListener {
    private static final String TAG = "EmailPassword";
    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseAuth mAuth;
    private TextView reset_btn;


    SlidrInterface slidrInterface;

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        System.gc();
        System.exit(0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_password_activity);
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);
        setProgressBar(R.id.progressBar);
        findViewById(R.id.emailSignInButton).setOnClickListener(this);
        findViewById(R.id.reset_btn).setOnClickListener(this);
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressBar();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, "Ошибка авторизации",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        hideProgressBar();
                    }
                });
    }


    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressBar();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
//                        if (!task.isSuccessful()) {
//                            mStatusTextView.setText(R.string.auth_failed);
//
//                        }
                        hideProgressBar();
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }


    private boolean validateForm() {
        boolean valid = true;
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        if ((TextUtils.isEmpty(email)) || (TextUtils.isEmpty(password))) {
            // mEmailField.setError("Обязательное поле!");
            valid = false;
        } else {
            mEmailField.setError(null);
            mPasswordField.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            // mPasswordField.setError("Обязательное поле!");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            Intent intent = new Intent(EmailPasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {


        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if ((mEmailField.getText().length() != 0) && (mPasswordField.getText().length() != 0)) {
            if (i == R.id.emailCreateAccountButton) {
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
            } else if (i == R.id.emailSignInButton) {
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
            } else if (i == R.id.reset_btn) {
                if (mEmailField.getText().length() != 0) {
                    reset_pass();
                } else {
                    Toast.makeText(EmailPasswordActivity.this, "Введите сначала почту!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(EmailPasswordActivity.this, "Введите сначала почту и пароль!", Toast.LENGTH_SHORT).show();
        }


    }

    @VisibleForTesting
    public ProgressBar mProgressBar;

    public void setProgressBar(int resId) {
        mProgressBar = findViewById(resId);
    }

    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void hideKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressBar();
    }

    public void reset_pass() {
        mAuth.sendPasswordResetEmail(mEmailField.getText().toString())
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(EmailPasswordActivity.this, "Проверьте почту!", Toast.LENGTH_SHORT).show();
                    }

                    public void onSuccess(Void result) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EmailPasswordActivity.this, "Почта не найдена!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}













