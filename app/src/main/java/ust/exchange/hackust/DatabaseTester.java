package ust.exchange.hackust;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by bigjohnlin on 4/23/2017.
 */

public class DatabaseTester {
    public class myObject {
        private int surge_multiplier;
        private int weekofday;
        private float begintrip_lat;
        private float begintrip_long;

        public myObject() {
        }
    }

    public static void testing() {
    DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://hackust-75a8f.firebaseio.com/");
    ValueEventListener query = createSurgeQuery(ref).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for(DataSnapshot postSnap: dataSnapshot.getChildren()) {
                Log.i("Record",postSnap.toString());
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.i("Error","Canceled");
        }
    });
    }

    public static Query createSurgeQuery(DatabaseReference ref) {
        Query surge_query = ref.orderByChild("surge_multiplier").startAt(1.4);
        return surge_query;
    }
}
