import client_part.Message;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class test {
    public static void main(String[] args) throws Exception {
        PublicKey sPublicKey;
        DSAPrivateKey sPrivateKey;
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
//        keyGen.initialize(1024);
//        KeyPair kp = keyGen.genKeyPair();

//        sPublicKey = kp.getPublic();
//        sPrivateKey = kp.getPrivate();

//        String s = "qwerty";
//        Cipher cipher = Cipher.getInstance( "ECIES" );
//        cipher.init( Cipher.ENCRYPT_MODE, sPublicKey );
//        byte[] x = cipher.doFinal( s.getBytes() );
//        String enc_s = new String(x);


//        byte[] y =
//        cipher.init( Cipher.DECRYPT_MODE, sPrivateKey );
//        byte[] y = cipher.doFinal( enc_s.getBytes() );
//        String dec_s = new String(y);
//        System.out.println(dec_s);
//        MainClass.main(new String[]{"",});

        Message msg = new Message("RSA", 2048);
        String s = "hello world";
        String encodedMsg = msg.encodeMsg(s);
        String decodedMsg = msg.decodeMsg(encodedMsg);

        System.out.println(decodedMsg);

//        String input = "baeldung";
//        SecretKey key = test.generateKey(256);
//        IvParameterSpec ivParameterSpec = test.generateIv();
//        String algorithm = "AES/CBC/PKCS5Padding";
//        String cipherText = test.encrypt(algorithm, input, key, ivParameterSpec);
//        String plainText = test.decrypt(algorithm, cipherText, key, ivParameterSpec);
//        System.out.println(input.equals(plainText));
//




    }
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static String encrypt(String algorithm, String input, SecretKey key,
                                 IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    public static String decrypt(String algorithm, String cipherText, SecretKey key,
                                 IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(cipherText));
        return new String(plainText);
    }

}
