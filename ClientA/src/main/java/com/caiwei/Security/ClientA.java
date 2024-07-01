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
public class ClientA {
    private static PublicKey publicKey;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 10000);
            log.info("连接到server!");
            while (true) {
                //发送消息
                String sandMessage = sandMessageToServer(socket);
                //接收消息
                receiveMessageFromServer(socket);

            }

        } catch (IOException e) {
            e.printStackTrace();
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

    public static String sandMessageToServer(Socket socket) {
        Scanner scanner = new Scanner(System.in);
        String sandMessage = null;
        try {
            System.out.print("请输入: ");
            sandMessage = scanner.next();
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            if (isWealthRequest(sandMessage)) {
                //输入的是财富值请求对象
                String[] split = sandMessage.split("-");
                WealthRequest wealthRequest = new WealthRequest();
                wealthRequest.setName(split[0]);
                String wealthEncryption = RASUtil.encrypt(split[1], publicKey);
                wealthRequest.setWealthEncryption(wealthEncryption);
                log.info("Client A 加密的财富值 = " + wealthEncryption);
                //json字符串化
                String wealthRequestJsonStr = JSON.toJSONString(wealthRequest);
                printWriter.println(wealthRequestJsonStr);
            } else {
                printWriter.println(sandMessage);
            }
            printWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sandMessage;
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
                System.out.println("server 发来消息: " + keyMessage);
            } else if (message.contains("richer")) {
                System.out.println("server 比较结果: " + message);
            } else {
                System.out.println("server 发来消息: " + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
