package com.proximadev.flyso.pins.encryption;

import android.text.TextUtils;

import com.proximadev.flyso.pins.enums.Algorithm;

import java.security.MessageDigest;
import java.util.Locale;

public class Encryptor {

    private static String bytes2Hex(byte[] bytes) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (byte aByte : bytes) {
            stmp = (Integer.toHexString(aByte & 0XFF));
            if (stmp.length() == 1) {
                hs.append("0").append(stmp);
            } else {
                hs.append(stmp);
            }
        }
        return hs.toString().toLowerCase(Locale.ENGLISH);
    }

    public static String getSHA(String text, Algorithm algorithm) {
        String sha = "";
        if (TextUtils.isEmpty(text)) {
            return sha;
        }

        MessageDigest shaDigest = getShaDigest(algorithm);

        if (shaDigest != null) {
            byte[] textBytes = text.getBytes();
            shaDigest.update(textBytes, 0, text.length());
            byte[] shahash = shaDigest.digest();
            return bytes2Hex(shahash);
        }

        return null;
    }

    private static MessageDigest getShaDigest(Algorithm algorithm) {
        switch (algorithm) {
            case SHA256:
                try {
                    return MessageDigest.getInstance("SHA-256");
                } catch (Exception e) {
                    try {
                        return MessageDigest.getInstance("SHA-1");
                    } catch (Exception e2) {
                        return null;
                    }
                }
            case SHA1:
            default:
                try {
                    return MessageDigest.getInstance("SHA-1");
                } catch (Exception e2) {
                    return null;
                }
        }
    }
}
