package ust.exchange.hackust;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by com93 on 4/22/2017.
 */

public class TouchableWrapper extends FrameLayout {
    private OnTouchListener onTouchListener;
    private OnLongClickListener onLongClickListener;

    public TouchableWrapper(@NonNull Context context) {
        super(context);
    }

    public void setTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchListener.onTouch();
                break;
            case MotionEvent.ACTION_UP:
                onTouchListener.onRelease();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public interface OnTouchListener {
        void onTouch();
        void onRelease();
    }
}
