package com.example.pull_and_do;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity  {
    private TextView mStatusTextView1;
    private TextView mDetailTextView1;
    private Button verifyBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseRecyclerAdapter<TaskViewHolder.Memo, TaskViewHolder> adapter;
    private EditText et_new_task;
    private FloatingActionButton btn_new_task;
    private FloatingActionButton btn_text_new_task;
    private FloatingActionButton edit_text_button;
    private FloatingActionButton calendar_btn;
    private FloatingActionButton account_btn;
    private FloatingActionButton audio_btn;
    private FloatingActionButton chat_btn;
    private RecyclerView recyclerView;
    private static final String TAG = "OfflineActivity";
    private boolean invis=true;
    private boolean vis=false;
    private FrameLayout frameLayouts;
    private FloatingActionButton btn_image;
    FirebaseUser user = mAuth.getInstance().getCurrentUser();
    private static int SIGN_IN_CODE = 1;
    private Boolean f;
    private ConstraintLayout constraintLayout;

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
        System.gc();
        System.exit(0);
    }

    // __________________________________________________________________________
    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Utils.getDatabase();

        views_group();//связывание элементов с java
        et_new_task.setVisibility(View.INVISIBLE);
        edit_text_button.setVisibility(View.INVISIBLE);
        btn_text_new_task.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        btn_image.setVisibility(View.INVISIBLE);
        audio_btn.setVisibility(View.INVISIBLE);

        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invisible(false);
                hideKeyboard(v);
            }
        });


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            //startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE);
            Toast.makeText(MainActivity.this, "Выполните вход", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, EmailPasswordActivity.class);
            startActivity(intent);
            finish();
        } else {
            //авторизован


            calendar_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                    startActivity(intent);
                }
            });

            if (!user.isEmailVerified()&&user!=null) {
              //  Toast.makeText(MainActivity.this, "Обязательно подтвердите почту!", Toast.LENGTH_SHORT).show();
            }


            account_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                    startActivity(intent);
                }
            });

            btn_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ImagesActivity.class);
                    startActivity(intent);
                }
            });


            //проверка подключения к базе данных
            DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        myRef.keepSynced(true);
                        Log.d(TAG, "connected");
                    } else {
                        Log.d(TAG, "not connected");
                        // FirebaseDatabase.getInstance().setPersistenceEnabled(true); //не работает здесь
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Listener was cancelled");
                }
            });


            audio_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AudioTaskActivity.class);
                    startActivity(intent);
                }
            });


            //вызов кнопки добавления данных
            btn_new_task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    invisible(invis);
                    focus_edit();
                    et_new_task.setText(null);
                }
            });


            //добавление в базу данных заметки
            btn_text_new_task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (et_new_task.getText().length() != 0) {
                        myRef.child(
                                user.getUid()).child("Tasks").
                                push().
                                setValue(
                                        new TaskViewHolder.Memo(et_new_task.getText().toString(), "null")
                                );

                        Toast.makeText(MainActivity.this, "Заметка добавлена!", Toast.LENGTH_SHORT).show();
                        invisible(vis);

                        if (recyclerView.getAdapter().getItemCount()!=0){
                            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                        myRef.keepSynced(true);
                    } else {
                        invisible(vis);
                        Toast.makeText(MainActivity.this, "Напишите заметку!", Toast.LENGTH_SHORT).show();
                    }
                    hideKeyboard(view);
                }
            });

            chat_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, CommonChat.class);
                    startActivity(intent);
                }
            });

            myRef = FirebaseDatabase.getInstance().getReference();
            Query query = myRef.child(user.getUid()).child("Tasks");
            FirebaseRecyclerOptions<TaskViewHolder.Memo> options =
                    new FirebaseRecyclerOptions.Builder<TaskViewHolder.Memo>()
                            .setQuery(myRef.child(user.getUid()).child("Tasks"), TaskViewHolder.Memo.class)
                            .build();

            adapter = new FirebaseRecyclerAdapter<TaskViewHolder.Memo, TaskViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final TaskViewHolder viewHolder, int position, @NonNull final TaskViewHolder.Memo model) {
                    String text, check;
                    text = model.getTextMemo();
                    check = model.getCheck();

                    viewHolder.textCheck.setText(text);
                    viewHolder.textCheck.setChecked(Boolean.valueOf(check));
                    viewHolder.date_memo.setText(DateFormat.format("dd.MM.yyyy", model.getTimeMemo()));


                    // mess_time.setText(DateFormat.format("dd-mm-yyyy HH:mm:ss", model.getTimeMessage()));

                    //кнопка удаления записи
                    viewHolder.mDel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final DatabaseReference itemRef = getRef(viewHolder.getAdapterPosition());

                            AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(MainActivity.this);
                            // Указываем Title
                            deleteConfirm.setTitle("Подтверждение удаления записи");
                            // Указываем текст сообщение
                            deleteConfirm.setMessage("Вы уверены, что хотите удалить?");

                            // задаем иконку
                            deleteConfirm.setIcon(R.drawable.icon_delete_confirm);
                            // Обработчик на нажатие ДА
                            deleteConfirm.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int which) {
                                    // Код который выполнится после закрытия окна
                                    itemRef.removeValue(null);
                                    Toast.makeText(getApplicationContext(), "Удалено", Toast.LENGTH_SHORT).show();
                                }
                            });
                            // Обработчик на нажатие НЕТ
                            deleteConfirm.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Код который выполнится после закрытия окна
                                    //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                }
                            });
                            // показываем Alert
                            deleteConfirm.show();
                        }
                    });

                    //кнопка редактирования записи
                    viewHolder.btn_edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final DatabaseReference itemRef = getRef(viewHolder.getAdapterPosition());

                            invisible(true);
                            btn_text_new_task.setVisibility(View.INVISIBLE);
                            edit_text_button.setVisibility(View.VISIBLE);
                            et_new_task.setText(viewHolder.textCheck.getText().toString());
                            focus_edit();
                            et_new_task.setSelection(et_new_task.getText().length());
                            edit_text_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (et_new_task.getText().length() != 0) {
                                        itemRef.child("textMemo").setValue(et_new_task.getText().toString());
                                        invisible(false);
                                        edit_text_button.setVisibility(View.INVISIBLE);
                                        hideKeyboard(v);
                                    }else{Toast.makeText(getApplicationContext(), "Введите заметку", Toast.LENGTH_SHORT).show();}
                                }
                            });
                            myRef.keepSynced(true);
                        }
                    });

                    viewHolder.textCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                            final DatabaseReference itemRef = getRef(viewHolder.getAdapterPosition());

                            if (isChecked) {    // Устанавливаем флаг зачёркивания
                                itemRef.child("check").setValue("true");
                                viewHolder.textCheck.setChecked(true);
                                //buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);


                            } else {    // Убираем флаг зачёркивания
                                itemRef.child("check").setValue("false");
                                viewHolder.textCheck.setChecked(false);
                                // buttonView.setPaintFlags(buttonView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            }
                        }
                    });
                }


                @Override
                public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.task_item, parent, false);
                    return new TaskViewHolder(view);
                }
            };
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageButton mDel;
        ImageButton btn_edit;
        CheckBox textCheck;
        TextView date_memo;

        public TaskViewHolder(View itemView) {
            super(itemView);
            textCheck = (CheckBox) itemView.findViewById(R.id.checkBox);
            mDel = (ImageButton) itemView.findViewById(R.id.btn_del);
            btn_edit = (ImageButton) itemView.findViewById(R.id.btn_edit);
            date_memo = (TextView) itemView.findViewById(R.id.date_memo);
        }
        public static class Memo{
            private String text;
            private String check;
            private long timeMemo;
            public Memo(String text, String check){
                this.text = text;
                this.check = check;
                this.timeMemo = new Date().getTime();
            }
            public long getTimeMemo() {
                return timeMemo;
            }
            public Memo () {
            }
            public void setTextMemo(String text) {
                this.text = text;
            }
            public void setCheck(String check) {
                this.check = check;
            }
            public String getCheck() {
                return check;
            }
            public String getTextMemo() {
                return text;
            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        et_new_task.clearFocus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void hideKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void views_group(){
        recyclerView = (RecyclerView) findViewById(R.id.rv_list_tasks);
        btn_image = (FloatingActionButton) findViewById(R.id.btn_image);
        btn_new_task = (FloatingActionButton) findViewById(R.id.add_button); //кнопка добавления новой задачи  вызов новой активити
        btn_text_new_task = (FloatingActionButton) findViewById(R.id.add_text_button); //кнопка добавления новой задачи  вызов новой активити
        edit_text_button = (FloatingActionButton) findViewById(R.id.edit_text_button);
        et_new_task = (EditText) findViewById(R.id.et_new_tasks);// текстовое поле для ввода задачи
        calendar_btn = (FloatingActionButton) findViewById(R.id.calendarBtn);
        account_btn = (FloatingActionButton) findViewById(R.id.account_btn);
        audio_btn = (FloatingActionButton) findViewById(R.id.audio_btn);
        chat_btn = (FloatingActionButton) findViewById(R.id.chat_btn);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
    }

    @SuppressLint("RestrictedApi")
    private void invisible(boolean f){
        if (f==true){
            btn_new_task.setVisibility(View.INVISIBLE);
            btn_image.setVisibility(View.INVISIBLE);
            calendar_btn.setVisibility(View.INVISIBLE);
            chat_btn.setVisibility(View.INVISIBLE);
            et_new_task.setVisibility(View.VISIBLE);
            btn_text_new_task.setVisibility(View.VISIBLE);
            account_btn.setVisibility(View.INVISIBLE);
            audio_btn.setVisibility(View.INVISIBLE);
        }else{
            btn_new_task.setVisibility(View.VISIBLE);
            et_new_task.setVisibility(View.INVISIBLE);
            btn_text_new_task.setVisibility(View.INVISIBLE);
            btn_image.setVisibility(View.INVISIBLE);
            calendar_btn.setVisibility(View.VISIBLE);
            account_btn.setVisibility(View.VISIBLE);
            audio_btn.setVisibility(View.INVISIBLE);
            chat_btn.setVisibility(View.VISIBLE);
        }
    }

    public void focus_edit(){
        et_new_task.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(
                        et_new_task.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
                et_new_task.requestFocus();
            }
        });
    }

    public static class Utils {
        private static FirebaseDatabase mDatabase;

        public static FirebaseDatabase getDatabase() {
            if (mDatabase == null) {
                mDatabase = FirebaseDatabase.getInstance();
                mDatabase.setPersistenceEnabled(true);
            }
            return mDatabase;
        }
    }
}


















