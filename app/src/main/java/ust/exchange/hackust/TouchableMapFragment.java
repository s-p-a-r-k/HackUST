package ust.exchange.hackust;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by com93 on 4/22/2017.
 */

public class TouchableMapFragment extends SupportMapFragment {
    private View mOriginalContentView;
    private TouchableWrapper mTouchView;

    public void setTouchListener(TouchableWrapper.OnTouchListener onTouchListener) {
        mTouchView.setTouchListener(onTouchListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        mTouchView = new TouchableWrapper(getActivity());
        mTouchView.addView(mOriginalContentView);

        return mTouchView;
    }

    @Override
    public View getView() {
        return mOriginalContentView;
    }
}
