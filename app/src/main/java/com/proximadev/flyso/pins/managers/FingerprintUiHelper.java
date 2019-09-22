package com.proximadev.flyso.pins.managers;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.ImageView;
import android.widget.TextView;

import com.proximadev.flyso.R;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintUiHelper extends FingerprintManager.AuthenticationCallback {

    private static final long ERROR_TIMEOUT_MILLIS = 1600;

    private static final long SUCCESS_DELAY_MILLIS = 1300;

    private static final String KEY_NAME = "my_key";

    private Cipher mCipher;

    private KeyStore mKeyStore;

    private final FingerprintManager mFingerprintManager;

    private final ImageView mIcon;

    private final TextView mErrorTextView;

    private final Callback mCallback;

    private CancellationSignal mCancellationSignal;

    private boolean mSelfCancelled;

    public static class FingerprintUiHelperBuilder {
        private final FingerprintManager mFingerPrintManager;

        public FingerprintUiHelperBuilder(FingerprintManager fingerprintManager) {
            mFingerPrintManager = fingerprintManager;
        }

        public FingerprintUiHelper build(ImageView icon, TextView errorTextView, Callback callback) {
            return new FingerprintUiHelper(mFingerPrintManager, icon, errorTextView,
                    callback);
        }
    }

    private FingerprintUiHelper(FingerprintManager fingerprintManager,
                                ImageView icon, TextView errorTextView, Callback callback) {
        mFingerprintManager = fingerprintManager;
        mIcon = icon;
        mErrorTextView = errorTextView;
        mCallback = callback;
    }

    public void startListening() throws SecurityException {
        if (initCipher()) {
            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(mCipher);
            if (!isFingerprintAuthAvailable()) {
                return;
            }
            mCancellationSignal = new CancellationSignal();
            mSelfCancelled = false;
            mFingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0 /* flags */, this, null);
            mIcon.setImageResource(R.drawable.ic_fingerprint);
        }
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            showError(errString);
            mIcon.postDelayed(() -> mCallback.onError(), ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        showError(mIcon.getResources().getString(
                R.string.pin_code_fingerprint_not_recognized));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.whiteColor, null));
        mErrorTextView.setText(
                mErrorTextView.getResources().getString(R.string.pin_code_fingerprint_success));
        mIcon.postDelayed(() -> mCallback.onAuthenticated(), SUCCESS_DELAY_MILLIS);
    }

    public boolean isFingerprintAuthAvailable() throws SecurityException {
        return mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints()
                && ((KeyguardManager) mIcon.getContext().getSystemService(Context.KEYGUARD_SERVICE)).isDeviceSecure();
    }

    private boolean initCipher() {
        try {
            if (mKeyStore == null) {
                mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            }
            createKey();
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_NAME, null);
            mCipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            mCipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (NoSuchPaddingException | KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    private void createKey() {
        try {
            KeyGenerator mKeyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            mKeyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            mKeyGenerator.generateKey();
        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    private void showError(CharSequence error) {
        mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mErrorTextView.setText(error);
        mErrorTextView.setTextColor(
                mErrorTextView.getResources().getColor(R.color.whiteColor, null));
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    private Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mErrorTextView.setTextColor(
                    mErrorTextView.getResources().getColor(R.color.whiteColor, null));
            mErrorTextView.setText(
                    mErrorTextView.getResources().getString(R.string.pin_code_fingerprint_text));
            mIcon.setImageResource(R.drawable.ic_fingerprint);
        }
    };

    public interface Callback {
        void onAuthenticated();

        void onError();
    }
}
