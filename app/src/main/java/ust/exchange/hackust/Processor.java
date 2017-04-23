package ust.exchange.hackust;

import android.provider.ContactsContract;

import java.util.ArrayList;

/**
 * Created by bigjohnlin on 4/23/2017.
 */

public class Processor {
    private ArrayList<DatabaseTester.myObject> dataList;
    private int timestamp;
    private long weekofday;
    public Processor(long weekofday, int timestamp) {
        this.weekofday = weekofday;
        this.timestamp = timestamp;
        dataList = Loading_Activity.getDataList();
    }

    public ArrayList<DatabaseTester.myObject> sort() {
        ArrayList<DatabaseTester.myObject> newList = new ArrayList<>();
        for (DatabaseTester.myObject obj: dataList) {
            if (obj.getWeekofday() == weekofday) {
                newList.add(obj);
            }
        }
        return newList;
    }

}
