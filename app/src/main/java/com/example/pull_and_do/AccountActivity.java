package com.example.pull_and_do;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AccountActivity extends AppCompatActivity {
    private EditText nameEditText;
    private TextView nameText;
    private String s;
    private Button verifyBtn;
    private TextView emailText;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private Button saveUserDiscription;
    private Button saveImgUser;
    private ImageButton btn_edit_account;
    private StorageTask mUploadTask;
    private FloatingActionButton exitBtn;
    FirebaseUser user = mAuth.getInstance().getCurrentUser();
    private SlidrInterface slidr;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private StorageReference myStorageRef;
    private ImageView img_account;
    private Context mContext;
    private FloatingActionButton back_btn;

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);
        mContext = this;
        slidr = Slidr.attach(this);
        nameEditText = (EditText) findViewById(R.id.name_text);
        emailText = (TextView) findViewById(R.id.emailText);
        nameText = (TextView) findViewById(R.id.textViewName);
        // secondNameText = (TextView) findViewById(R.id.textViewSecondName);
        saveUserDiscription = (Button) findViewById(R.id.saveUserDiscription_btn);
        saveImgUser = (Button) findViewById(R.id.saveImgUser);
        btn_edit_account = (ImageButton) findViewById(R.id.btn_edit_account);
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getInstance().getCurrentUser();
        verifyBtn = (Button) findViewById(R.id.verifyBtn);
        exitBtn = (FloatingActionButton) findViewById(R.id.exitBtn);
        back_btn = (FloatingActionButton) findViewById(R.id.back_btn);

        img_account = (ImageView) findViewById(R.id.img_account);
        //slidr = Slidr.attach(this);
        nameText.setText("Ваше имя");
        //secondNameText.setText("ФАМИЛИЯ");
        final ArrayList<Object> mUploads = new ArrayList<>();
        myStorageRef = FirebaseStorage.getInstance().getReference().child(user.getUid()).child("Image user");
        myRef = FirebaseDatabase.getInstance().getReference().child(user.getUid()).child("UserImg");

        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.keepSynced(true);

        emailText.setText(user.getEmail().toString());
        saveImgUser.setEnabled(false);

        //редактирование фото user
        btn_edit_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
                saveImgUser.setEnabled(true);
            }
        });

        //сохранение фото на сервер
        saveImgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(AccountActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    // tmpName = mEditTextFileName.getText().toString();
                    uploadFile();

                }
            }
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    String url = dataSnapshot.child(user.getUid()).child("Image user").getValue(String.class);
                    Picasso.with(mContext).load(url).placeholder(R.drawable.ic_account_circle_50)
                            .fit().centerCrop()
                            .into(img_account);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AccountActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        if (!user.isEmailVerified()) {
            // saveUserDiscription.setVisibility(View.INVISIBLE);
            // nameEditText.setVisibility(View.INVISIBLE);
            // nameText.setText("");
            // secondNameText.setText("Для подтверждения нажмите на кнопку");
            Toast.makeText(AccountActivity.this, "Обязательно подтвердите почту!", Toast.LENGTH_SHORT).show();
        } else {
            //saveUserDiscription.setVisibility(View.INVISIBLE);
        }

        //верификация почты
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.isEmailVerified()) {
                    verifyBtn.setEnabled(!user.isEmailVerified());
                    Toast.makeText(AccountActivity.this, "Почта уже подтверждена!", Toast.LENGTH_SHORT).show();
                } else {
                    sendEmailVerification();
                }
            }
        });


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });




        //выход в окно регистрации
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                mAuth = null;
                Intent intent = new Intent(AccountActivity.this, EmailPasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            private String TAG;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name;
                name = dataSnapshot.child(user.getUid()).child("Name").getValue(String.class);
                nameText.setText(name);

                if (name == null) {
                    myRef.child(user.getUid()).child("Name").setValue("Аноним");
            }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                DatabaseError error = null;
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

            //сохраненение имени
        saveUserDiscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(nameEditText.getText().length()!=0) {
                        myRef.child(user.getUid()).child("Name").setValue(nameEditText.getText().toString());
                        nameEditText.setText("");
                        hideKeyboard(v);
                }else{
                    Toast.makeText(AccountActivity.this, "Введите имя!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    public void hideKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void sendEmailVerification() {
        findViewById(R.id.verifyBtn).setEnabled(false);
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                findViewById(R.id.verifyBtn).setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(AccountActivity.this,
                            "Проверьте почту!" + user.getEmail(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    //Log.e(TAG, "sendEmailVerification", task.getException());
                    Toast.makeText(AccountActivity.this,
                            "Ошибка верификации!.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Picasso.with(this).load(mImageUri).into(img_account);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null) {
            StorageReference fileReference = myStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> mUploadTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!mUploadTask.isSuccessful()) ;
                            Uri downloadUrl = mUploadTask.getResult();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //  mProgressCircle.setProgress(0);
                                }
                            }, 500);
                            Toast.makeText(AccountActivity.this, "Upload successful", Toast.LENGTH_LONG).show();

                            myRef.child(user.getUid()).child("Image user").setValue(downloadUrl.toString());
                            saveImgUser.setEnabled(false);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AccountActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            // mProgressCircle.setProgress((int) progress);
                        }

                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }


}