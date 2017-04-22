package ust.exchange.hackust;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class dbTest extends AppCompatActivity {
    public class myObject {
        private float begintrip_lat;
        private float begintrip_lng;
        private String begintrip_timestamp_local;
        private float dropoff_lat;
        private float dropoff_lng;
        private String is_completed;
        private float original_fare_local;
        private int surge_multiplier;
        private int toll_amount_local;
        private float trip_distance_miles;
        private int weekofday;


        public myObject() {}
    }

    DatabaseReference ref = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://hackust-75a8f.firebaseio.com/");



    ValueEventListener objectListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get Post object and use the values to update the UI
            myObject object = dataSnapshot.getValue(myObject.class);
            // ...
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message

            // ...
        }
    };

    private TextView textViewList;
    //FirebaseDatabase.set


}





