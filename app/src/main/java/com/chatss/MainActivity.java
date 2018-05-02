package com.chatss;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private Button btnSend;
    private EditText edtMessage;
    private ImageButton btnAddImage;
    private String mUsername;
    private MessageAdapter mMessageAdapter;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 123;
    private static final int RC_PHOTO_PICKER = 7;
    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mDatabaseReference = mFirebaseDatabase.getReference().child("messages");
        mStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        edtMessage = (EditText) findViewById(R.id.edtMessage);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnAddImage = (ImageButton) findViewById(R.id.btnAddImage);

        edtMessage.addTextChangedListener(new EditTextWatcher());


        mMessageAdapter = new MessageAdapter(this,R.layout.list_item);

        ListView msgList = (ListView) findViewById(R.id.messageList);
        msgList.setAdapter(mMessageAdapter);

        btnSend.setOnClickListener(this);
        btnAddImage.setOnClickListener(this);
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null)
                {
                    //user is signed in
                    onSignInInitialise(user.getDisplayName());
                }
                else
                {
                    onSignOutCleanUp();
                    //show firebase UI for user to sign in
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void onSignInInitialise(String displayName) {
        mUsername = displayName;
        attachDataBaseReadListener();
    }

    private void onSignOutCleanUp()
    {
        mUsername = "";
        mMessageAdapter.clear();
        detachDataBaseListener();
    }

    private void detachDataBaseListener()
    {
        if(mChildEventListener != null)
        {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

    private void attachDataBaseReadListener()
    {
        if(mChildEventListener == null)
        {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    mMessageAdapter.add(chatMessage);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN)
        {
            if(resultCode == RESULT_OK)
            {

            }
            else if(resultCode == RESULT_CANCELED) {
                finish();
            }
        }
        else if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK)
        {
            Uri selectedImageUri = data.getData();
            StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment());

            photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadedUri = taskSnapshot.getDownloadUrl();
                    ChatMessage chatMessage = new ChatMessage(null,mUsername,downloadedUri.toString());
                    mDatabaseReference.push().setValue(chatMessage);

                    mMessageAdapter.add(chatMessage);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_signout:
                AuthUI.getInstance().signOut(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener != null)
        {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDataBaseListener();
        mMessageAdapter.clear();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch(id)
        {
            case R.id.btnSend:
                    String msg = edtMessage.getText().toString();
                ChatMessage chatMessage = new ChatMessage(msg, mUsername, null);
                mDatabaseReference.push().setValue(chatMessage);

                edtMessage.setText("");
                break;

            case R.id.btnAddImage:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"Choose your photo..."),RC_PHOTO_PICKER);
                break;
        }
    }

    private class EditTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(charSequence.length() > 0)
            {
                btnSend.setEnabled(true);
            }
            else
            {
                btnSend.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
