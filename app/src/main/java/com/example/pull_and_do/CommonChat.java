package com.example.pull_and_do;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

public class CommonChat extends AppCompatActivity {
    private static int SIGN_IN_CODE = 1;
    private RelativeLayout activity_chat;
    private ListView list_of_messages;
    private FloatingActionButton btnSend;
    private EditText message_field;
    public String nameUser;
    DatabaseReference myRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getInstance().getCurrentUser();
    private FirebaseListAdapter<Message> adapter;
    private SlidrInterface slidr;
    private FrameLayout message_frame;
    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;
    // Идентификатор канала
    private static String CHANNEL_ID = "Message channel";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_common_activity);
        btnSend = findViewById(R.id.btnSend);
        message_field = findViewById((R.id.message_field));
        message_frame = findViewById(R.id.message_frame);

        slidr = Slidr.attach(this);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE);
            //не авторизован
        } else {
            //авторизован
            //Snackbar.make(activity_chat, "Вы авторизованы", Snackbar.LENGTH_SHORT).show();
            displayAllMessage();
        }

        //Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            private String TAG;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nameUser = dataSnapshot.child(user.getUid()).child("Name").getValue(String.class);

                //Read from the database
                myRef.addValueEventListener(new ValueEventListener() {
                    private String TAG;

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name;
                        name = dataSnapshot.child(user.getUid()).child("Name").getValue(String.class);
                        if (name == null) {
                            myRef.child(user.getUid()).child("Name").setValue("Аноним");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        DatabaseError error = null;
                        Log.w(TAG, "Ошибка чтения базы данных", error.toException());
                    }
                });

                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (message_field.getText().length() != 0) {
                            myRef.child("Messages").push().setValue(
                                    new Message(
                                            nameUser,
                                            message_field.getText().toString()));
                            message_field.setText("");
                            myRef.keepSynced(true);
                        } else {
                            Toast.makeText(CommonChat.this, "Ошибка!", Toast.LENGTH_SHORT).show();
                        }
                        displayAllMessage();
                        list_of_messages.setSelection(list_of_messages.getAdapter().getCount() - 1); // не работает
                        hideKeyboard(view);
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                DatabaseError error = null;
                Log.w(TAG, "Ошибка чтения базы данных", error.toException());
            }
        });
    }


    private void displayAllMessage() {
        list_of_messages = (ListView) findViewById(R.id.list_of_messages);

        myRef = FirebaseDatabase.getInstance().getReference();

        FirebaseListOptions<Message> options =
                new FirebaseListOptions.Builder<Message>()
                        .setQuery(myRef.child("Messages"), Message.class)
                        .setLayout(R.layout.message_item)
                        .build();

        adapter = new FirebaseListAdapter<Message>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull Message model, int position) {
                TextView mess_user = null, mess_time = null, mess_text = null;
                    mess_user = v.findViewById(R.id.message_user);
                    mess_time = v.findViewById(R.id.message_time);
                    mess_text = v.findViewById(R.id.message_text);
                    mess_user.setText(model.getName());
                    mess_text.setText(model.getText());
                    mess_time.setText(DateFormat.format("HH:mm", model.getTimeMessage()));
            }
        };



        list_of_messages.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }


    static Context ctx=null;

    private static class CustomerTextClick extends ClickableSpan {

        private String mUrl;
        CustomerTextClick(String url) {
            mUrl =url;
        }
        @Override
        public void onClick(View widget) {
            // TODO Auto-generated method stub
            Toast.makeText(ctx, "hello google!",Toast.LENGTH_LONG).show();
        }
    }


//    public void showNoty() {
//        Intent notificationIntent = new Intent(CommonChat.this, CommonChat.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(CommonChat.this,
//                0, notificationIntent,
//                PendingIntent.FLAG_CANCEL_CURRENT);
//
//        NotificationCompat.Builder builder =
//                new NotificationCompat.Builder(CommonChat.this, CHANNEL_ID)
//                        .setSmallIcon(R.drawable.ic_email_white_18dp)
//                        .setContentTitle("Новое сообщение в чате")
//                        .setContentText("Посмотри его")
//                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//        NotificationManagerCompat notificationManager =
//                NotificationManagerCompat.from(CommonChat.this);
//        notificationManager.notify(NOTIFY_ID, builder.build());
//    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activity_chat, "Вы авторизованы", Snackbar.LENGTH_SHORT).show();
                displayAllMessage();
            } else {
                Snackbar.make(activity_chat, "Вы не авторизованы", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    public void hideKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

//    public static class Utils {
//        private static FirebaseDatabase mDatabase;
//
//        public static FirebaseDatabase getDatabase() {
//            if (mDatabase == null) {
//                mDatabase = FirebaseDatabase.getInstance();
//                mDatabase.setPersistenceEnabled(true);
//            }
//            return mDatabase;
//        }
//    }
}
