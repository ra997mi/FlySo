package com.proximadev.flyso.pins.interfaces;

import com.proximadev.flyso.pins.enums.KeyboardButtonEnum;

public interface KeyboardButtonClickedListener {

    void onKeyboardClick(KeyboardButtonEnum keyboardButtonEnum);

    void onRippleAnimationEnd();

}
