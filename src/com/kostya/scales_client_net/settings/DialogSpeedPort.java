package com.kostya.scales_client_net.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import com.kostya.scales_client_net.NumberPicker;
import com.kostya.scales_client_net.R;

import java.util.Arrays;

/**
 * @author Kostya
 */
public class DialogSpeedPort extends DialogPreference {
    private int mNumber;
    private final String[] speedPortArray;
    private NumberPicker numberPicker;
    final int minValue;
    final int maxValue;

    private static final SparseArray<String> mapCodeDevice = new SparseArray<>();

    static {
        mapCodeDevice.put(1, "600");
        mapCodeDevice.put(2, "1200");
        mapCodeDevice.put(3, "2400");
        mapCodeDevice.put(4, "4800");
        mapCodeDevice.put(5, "9600");
        mapCodeDevice.put(6, "19200");
        mapCodeDevice.put(7, "38400");
        mapCodeDevice.put(8, "57600");
        mapCodeDevice.put(9, "115200");
        mapCodeDevice.put(10, "230400");
        mapCodeDevice.put(11, "460800");
    }

    public DialogSpeedPort(Context context, AttributeSet attrs) {
        super(context, attrs);
        speedPortArray = context.getResources().getStringArray(R.array.array_speed_port);
        minValue = 0;
        maxValue = speedPortArray.length > 0 ? speedPortArray.length-1 : 0;
        //String speed = getPersistedString("9600");//globals.isScaleConnect()?globals.getScaleModule().getSpeedPort():0;
        int speed = Arrays.asList(speedPortArray).indexOf(String.valueOf(getPersistedInt(9600)));
        if(speed != -1)
            mNumber = speed;
        setPersistent(true);
        setDialogLayoutResource(R.layout.number_picker);
    }

    @Override
    protected void onBindDialogView(View view) {
        numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(maxValue);
        numberPicker.setMinValue(minValue);
        numberPicker.setDisplayedValues(speedPortArray);
        numberPicker.setValue(mNumber);
        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // needed when user edits the text field and clicks OK
            numberPicker.clearFocus();
            setValue(numberPicker.getValue());
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int value = restoreValue? getPersistedInt(mNumber) : (Integer) defaultValue;
        setValue(Arrays.asList(speedPortArray).indexOf(String.valueOf(value)));
    }

    public void setValue(int value) {
        if (shouldPersist()) {
            persistInt(Integer.valueOf(speedPortArray[value]));
        }

        if (value != mNumber) {
            mNumber = value;
            notifyChanged();
            callChangeListener(Integer.valueOf(speedPortArray[value]));
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }



}
