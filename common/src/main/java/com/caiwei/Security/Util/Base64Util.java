package com.caiwei.Security.Util;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Base64Util {
    /**
     * 将公钥进行base64编码成字符串
     *
     * @param publicKey 公钥
     * @return
     */
    public static String publicKeyToBase64(PublicKey publicKey) {
        byte[] encodedKey = publicKey.getEncoded();
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(encodedKey);
    }


    /**
     * 从Base64编码的字符串恢复PublicKey对象
     *
     * @param base64EncodedPublicKey Base64编码的公钥字符串
     * @return PublicKey对象
     */
    public static PublicKey base64ToPublicKey(String base64EncodedPublicKey) throws Exception {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decodedBytes = decoder.decode(base64EncodedPublicKey);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // 设置公钥类型调整
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedBytes);

        return keyFactory.generatePublic(keySpec);
    }
}
