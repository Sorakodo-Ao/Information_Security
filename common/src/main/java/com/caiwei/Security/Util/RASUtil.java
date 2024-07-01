package com.caiwei.Security.Util;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RASUtil {
    /**
     * 生成密钥对
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair getgenerateKeyPair() throws NoSuchAlgorithmException {
        // 获取KeyPairGenerator实例，并指定算法为RSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

        // 初始化密钥对生成器，设置密钥长度（例如，2048位）
        keyPairGenerator.initialize(512);

        // 生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
/*
        // 获取公钥和私钥
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();*/

/*        // 打印公钥和私钥（仅用于示例，实际中不建议直接打印或暴露密钥）
        log.info("Public Key: " + publicKey.toString());
        log.info("Private Key: " + privateKey.toString());*/

        return keyPair;
    }



/*    public static byte[] encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(plainText.getBytes());
    }
    public static String decrypt(byte[] encryptedBytes, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }*/

    /**
     * 加密
     *
     * @param plainText
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // 选择加密模式和填充方式
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedBytes = encryptCipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes); // 对加密后的字节进行Base64编码以便于传输或存储
    }

    /**
     * 解密
     *
     * @param encryptedText
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String decrypt(String encryptedText, PrivateKey privateKey) throws Exception {
        Cipher decriptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText); // 首先对Base64编码的密文进行解码
        byte[] decryptedBytes = decriptCipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }

    /**
     * 测试加密和解密
     * @param args
     */
    public static void main(String[] args) {

        try {
            String message = "11.5";
            KeyPair keyPair = getgenerateKeyPair();
            String encrypt = encrypt(message, keyPair.getPublic());
            System.out.println("encrypt = " + encrypt);

            String decrypt = decrypt(encrypt, keyPair.getPrivate());
            System.out.println("decrypt = " + decrypt);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
