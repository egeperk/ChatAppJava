package com.egeperk.chatappjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    EditText messageText;

    FirebaseDatabase database;
    DatabaseReference databaseReference;

    private ArrayList<String> chatMessages = new ArrayList<>();

    private static final String ONESIGNAL_APP_ID = "9486532a-8b63-4d45-bf37-8f6363c208bb";


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.option_menu_sign_out_menu) {
            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.option_menu_profile_menu) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        messageText = findViewById(R.id.chat_activity_message_text);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerViewAdapter = new RecyclerViewAdapter(chatMessages);

        RecyclerView.LayoutManager recyclerViewManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(recyclerViewManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(recyclerViewAdapter);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://chatapp-8c4f6-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference();

        getData();


        //Push Notification
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        OSDeviceState deviceState = OneSignal.getDeviceState();
        String userId = deviceState != null ? deviceState.getUserId() : null;
        System.out.println("userId" + userId);

        UUID uuid = UUID.randomUUID();
        final String uuidString = uuid.toString();

        DatabaseReference newReference = database.getReference("UserIDs");
        newReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String > userIDsFromServer = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    HashMap<String,String> hashMap = (HashMap<String, String>) ds.getValue();
                    String currentUserId = hashMap.get("userID");
                    userIDsFromServer.add(currentUserId);

                }

                if (!userIDsFromServer.contains(userId)) {
                    databaseReference.child("UserIDs").child(uuidString).child("userID").setValue(userId);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }

    public void sendMessage(View view) {

        String messageToSend = messageText.getText().toString();

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        //databaseReference.child("Chats").child("Chat 1").child("Test Chat").child("Test 1").setValue(messageToSend);

        FirebaseUser user = mAuth.getCurrentUser();
        String userEmail = user.getEmail().toString();

        databaseReference.child("Chat").child(uuidString).child("usermessage").setValue(messageToSend);
        databaseReference.child("Chat").child(uuidString).child("usermail").setValue(userEmail);
        databaseReference.child("Chat").child(uuidString).child("usermessagetime").setValue(ServerValue.TIMESTAMP);

        messageText.setText("");

        getData();

        //oneSignal

        DatabaseReference newReference = database.getReference("UserIDs");
        newReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    HashMap<String,String> hashMap = (HashMap<String, String>) ds.getValue();

                    String userID = hashMap.get("userID");
                    System.out.println("userId Server: " + userID);

                    try {
                        OneSignal.postNotification(new JSONObject("{'contents': {'en':'"+messageToSend+"'}, 'include_player_ids': ['" + userID + "']}"), null);                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void getData() {
        //Referanstaki verileri almak için yeni reference yazdık
        DatabaseReference newReference = database.getReference("Chat");

        Query query = newReference.orderByChild("usermessagetime");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

        /*
         System.out.println("snapshot: " + snapshot.getChildren());
         System.out.println("snapshot: " + snapshot.getValue());
         System.out.println("snapshot: " + snapshot.getKey());
        */

                chatMessages.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    //System.out.println("data value" +ds.getValue());

                    HashMap<String,String> hashMap = (HashMap<String, String>) ds.getValue();
                    String useremail = hashMap.get("usermail");
                    String usermessage = hashMap.get("usermessage");

                    chatMessages.add(useremail + ": " + usermessage);

                    recyclerViewAdapter.notifyDataSetChanged();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}