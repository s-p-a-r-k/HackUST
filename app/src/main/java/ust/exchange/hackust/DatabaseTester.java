package ust.exchange.hackust;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by bigjohnlin on 4/23/2017.
 */

public class DatabaseTester {
    public static class myObject {
        private double surge_multiplier;
        private long weekofday;
        private double begintrip_lat;
        private double begintrip_long;
        private String timestamp;

        public myObject() {
        }
        public void setSurge(double surge) {
            surge_multiplier = surge;
        }
        public double getSurge() {
            return surge_multiplier;
        }
        public void setWeekofday(long weekofday) {
            this.weekofday = weekofday;
        }
        public long getWeekofday() {
            return weekofday;
        }
        public void setBegintrip_lat(double lat) {
            begintrip_lat = lat;
        }
        public double getBegintrip_lat() {
            return begintrip_lat;
        }
        public void setBegintrip_long(double lng) {
            begintrip_long = lng;
        }
        public double getBegintrip_long() {
            return begintrip_long;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
    private static ArrayList<myObject> dataList;

    public static ArrayList<myObject> testing() {
        dataList = new ArrayList<myObject>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://hackust-75a8f.firebaseio.com/");
        ValueEventListener query = createSurgeQuery(ref).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for(DataSnapshot postSnap: dataSnapshot.getChildren()) {
                myObject newRecord = new myObject();
                newRecord.setSurge((Double.parseDouble(postSnap.child("surge_multiplier").getValue().toString())));
                newRecord.setBegintrip_lat((double)postSnap.child("begintrip_lat").getValue());
                newRecord.setBegintrip_long((double)postSnap.child("begintrip_lng").getValue());
                newRecord.setTimestamp((String)postSnap.child("begintrip_timestamp_local").getValue());
                newRecord.setWeekofday((long)postSnap.child("weekofday").getValue());
                dataList.add(newRecord);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.i("Error","Canceled");
        }
    });
        return dataList;
    }

    public static Query createSurgeQuery(DatabaseReference ref) {
        Query surge_query = ref.orderByChild("surge_multiplier").startAt(1.6);
        return surge_query;
    }
}
