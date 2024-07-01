package com.caiwei.Security;

import com.alibaba.fastjson2.JSON;
import com.caiwei.Security.Entities.ResponseKey;
import com.caiwei.Security.Entities.WealthRequest;
import com.caiwei.Security.Util.Base64Util;
import com.caiwei.Security.Util.RASUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Scanner;

@Slf4j
public class ClientB {
    private static PublicKey publicKey;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 10000);
            log.info("连接到server!");
            while (true) {
                //接收服务端发的消息
                receiveMessageFromServer(socket);
                //发消息给服务端

                sandMessageToServer(socket);


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void receiveMessageFromServer(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String message = bufferedReader.readLine();
            if (message.contains("publicKeyToBase64")) {
                //发来的是响应公钥
                ResponseKey responseKey = JSON.parseObject(message, ResponseKey.class);
                String keyMessage = responseKey.getMessage();
                String publicKeyToBase64 = responseKey.getPublicKeyToBase64();
                publicKey = Base64Util.base64ToPublicKey(publicKeyToBase64);
                //log.info("ClientB 收到 publicKey = " + publicKey);
                System.out.println("server 发来消息: " + keyMessage);
            } else {
                System.out.println("server 发来消息: " + message);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void sandMessageToServer(Socket socket) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入: ");
        String sandMessage = scanner.next();
        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            if (isWealthRequest(sandMessage)) {
                //发送加密的财富值
                WealthRequest wealthRequest = new WealthRequest();
                String[] split = sandMessage.split("-");
                wealthRequest.setName(split[0]);
                //log.info("ClientB 的 publicKey = " + publicKey);
                String wealthEncryption = RASUtil.encrypt(split[1], publicKey);
                log.info("ClientB 加密的财富值 = " + wealthEncryption);
                wealthRequest.setWealthEncryption(wealthEncryption);

                String wealthRequestJsonStr = JSON.toJSONString(wealthRequest);
                printWriter.println(wealthRequestJsonStr);
            } else {
                printWriter.println(sandMessage);
            }
            printWriter.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isWealthRequest(String sandMessage) {
        String[] split = sandMessage.split("-");
        try {
            Double.parseDouble(split[1]);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
