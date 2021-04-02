package edu.wschain.china_b;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import io.flutter.embedding.android.FlutterActivity;

public class MainActivity extends FlutterActivity {

    private static final String DEFAULT_KEY_NAME = "default_key";
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private CancellationSignal cancellationSignal;
    private FingerprintManager fingerprintManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if(supportFingerprint()){
//            initKey();
//            initCipher();
//        }

        fingerText();

    }

    @TargetApi(23)
    private void fingerText() {
        fingerprintManager = getContext().getSystemService(FingerprintManager.class);
        fingerprintManager.authenticate(null,null,0,new FingerprintManager.AuthenticationCallback(){
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(MainActivity.this, errString.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
                Toast.makeText(MainActivity.this, helpString.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "指纹认证成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(MainActivity.this, "指纹认证失败，请再试一次", Toast.LENGTH_SHORT).show();
            }
        },null);
    }


    private void showFingerPrintDialog(Cipher cipher) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            fingerprintManager = getContext().getSystemService(FingerprintManager.class);
        }
        startListening(cipher);
    }

    private void startListening(Cipher cipher) {
        boolean isSelfCancelled = false;
        cancellationSignal = new CancellationSignal();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if(!fingerprintManager.isHardwareDetected()) {
                Toast.makeText(this, "设备不支持指纹", Toast.LENGTH_SHORT).show();
                return;
            }else if(!fingerprintManager.hasEnrolledFingerprints()){
                Toast.makeText(this, "请现在设备中添加指纹", Toast.LENGTH_SHORT).show();
                return;
            }

            fingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), cancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    if (!isSelfCancelled) {
//                        errorMsg.setText(errString);
                        System.out.println(errString.toString());
                        if (errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                            Toast.makeText(MainActivity.this, errString.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    Toast.makeText(MainActivity.this, helpString.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    Toast.makeText(MainActivity.this, "指纹认证成功", Toast.LENGTH_SHORT).show();
                    MainActivity.onAuthenticated();
                }

                @Override
                public void onAuthenticationFailed() {
                    Toast.makeText(MainActivity.this, "指纹认证失败，请再试一次", Toast.LENGTH_SHORT).show();
                }
            }, null);
        }
    }

    private static void onAuthenticated() {

    }

    public boolean supportFingerprint(){
        if(Build.VERSION.SDK_INT < 23){
            Toast.makeText(this, "您的系统版本过低，不支持指纹", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            keyguardManager = getSystemService(KeyguardManager.class);
            FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
            if(!fingerprintManager.isHardwareDetected()){
                Toast.makeText(this, "您的手机不支持指纹功能", Toast.LENGTH_SHORT).show();
                return false;
            }else if(!keyguardManager.isKeyguardSecure()){
                Toast.makeText(this, "您还未设置锁屏，请先设置一个锁屏", Toast.LENGTH_SHORT).show();
                return false;
            }else if(!fingerprintManager.hasEnrolledFingerprints()){
                Toast.makeText(this, "您至少需要在系统设置中添加一个指纹", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @TargetApi(23)
    private void initKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
            keyGenerator.init(builder.build());
            keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(23)
    private void initCipher() {
        try {
            SecretKey key = (SecretKey) keyStore.getKey(DEFAULT_KEY_NAME, null);
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            showFingerPrintDialog(cipher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
