package com.proximadev.flyso.pins.enums;

public enum KeyboardButtonEnum {

    BUTTON_0(0),
    BUTTON_1(1),
    BUTTON_2(2),
    BUTTON_3(3),
    BUTTON_4(4),
    BUTTON_5(5),
    BUTTON_6(6),
    BUTTON_7(7),
    BUTTON_8(8),
    BUTTON_9(9),
    BUTTON_CLEAR(-1);

    private int mButtonValue;

    KeyboardButtonEnum(int value) {
        this.mButtonValue = value;
    }

    public int getButtonValue() {
        return mButtonValue;
    }
}
