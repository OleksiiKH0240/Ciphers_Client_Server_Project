package client_part;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.util.Base64;

public class Message {
//    RSA, AES
    private String encryptingType;
    private PublicKey mPublicKey;
    private PrivateKey mPrivateKey;
    private SecretKey mSecretKey;
    private IvParameterSpec mIv;
    private int mKeySize;

    public Message(String encryptingType, int keySize) throws NoSuchAlgorithmException {
        this.encryptingType = encryptingType;
        this.mKeySize = keySize;

        if (this.encryptingType.contains("RSA")) {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance( this.encryptingType );
            keyGen.initialize( this.mKeySize);
            KeyPair kp = keyGen.genKeyPair();

            mPublicKey = kp.getPublic();
            mPrivateKey = kp.getPrivate();
        }
        else if (this.encryptingType.contains("AES")){
            KeyGenerator generator = KeyGenerator.getInstance("AES");
//            SecureRandom random = new SecureRandom();

//            generator.init(this.mKeySize, random);

            generator.init(this.mKeySize);

            mSecretKey = generator.generateKey();

            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            this.mIv = new IvParameterSpec(iv);
        }

    }

    public String getEncryptingType() {
        return encryptingType;
    }


    public int getMKeySize() {
        return mKeySize;
    }

    public String encodeMsg(String msg) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance( this.encryptingType );
        if (this.encryptingType.contains("RSA")) {
            cipher.init( Cipher.ENCRYPT_MODE, mPublicKey);
        }
        else if (this.encryptingType.contains("AES")){
            cipher.init( Cipher.ENCRYPT_MODE, mSecretKey, mIv);
        }
//        byte[] x = cipher.doFinal( msg.getBytes() );
//        x = Base64.getEncoder().encode(x);
//        return new String(x);

        byte[] cipherText = cipher.doFinal( msg.getBytes() );
        return Base64.getEncoder()
                .encodeToString(cipherText);

    }
    public String decodeMsg(String msg) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance( this.encryptingType );
        if (this.encryptingType.contains("RSA")) {
            cipher.init( Cipher.DECRYPT_MODE, mPrivateKey);
        }
        else if (this.encryptingType.contains("AES")){
            cipher.init( Cipher.DECRYPT_MODE, mSecretKey, mIv);
        }
//        byte[] y;
//        y = Base64.getDecoder().decode( msg.getBytes() );
//        String yStr = new String(y);
//        y = cipher.doFinal( yStr.getBytes() );
//        return new String(y);

        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(msg));
        return new String(plainText);
    }

}
