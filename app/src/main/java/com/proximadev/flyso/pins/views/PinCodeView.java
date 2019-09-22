package com.proximadev.flyso.pins.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.proximadev.flyso.R;

public class PinCodeView extends RelativeLayout {

    private Context mContext;

    public PinCodeView(Context context) {
        this(context, null);
    }

    public PinCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.mContext = context;
        initializeView(attrs, defStyleAttr);
    }

    private void initializeView(AttributeSet attrs, int defStyleAttr) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.activity_pin_code, this);
    }

}
