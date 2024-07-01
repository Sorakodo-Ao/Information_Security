package com.caiwei.Security.ServeSocket;

import com.alibaba.fastjson2.JSON;
import com.caiwei.Security.Entities.ResponseKey;
import com.caiwei.Security.Entities.WealthRequest;
import com.caiwei.Security.Service.ServerService;
import com.caiwei.Security.Util.Base64Util;
import com.caiwei.Security.Util.RASUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Server {

    private static KeyPair keyPair;

    private static ServerService serverService = new ServerService();

    public static void main(String[] args) {
        Map<String, Socket> socketMap = new HashMap<>();
        try {
            ServerSocket serverSocket = new ServerSocket(10000);
            while (true) {
                Socket socket = serverSocket.accept();
                int port = socket.getPort();
                String IP = socket.getInetAddress().getHostAddress();
                log.info("客户端连接,ip:port = " + IP + ":" + port);
                socketMap.put(IP + ":" + port, socket);

                new Thread(() -> {
                    while (true) {
                        try {
                            //接收消息
                            String receiveMessage = receiveFromClient(socket);

                            //发送消息
                            sandToClient(receiveMessage, socketMap, socket);
                        } catch (Exception e) {
                            log.info("连接断开" + e.getMessage());
                            //socketMap.remove(IP + ":" + port);
                            break;
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String receiveFromClient(Socket socket) {
        String receiveMessage = null;
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            receiveMessage = bufferedReader.readLine();
            log.info("receiveMessageFromClient = " + receiveMessage);
            if (receiveMessage.contains("wealthEncryption") && receiveMessage.contains("name")) {
                //收到的是财富值对象
                WealthRequest wealthRequest = JSON.parseObject(receiveMessage, WealthRequest.class);
                String addOrCompare = serverService.addOrCompare(wealthRequest, socket, keyPair);
                return addOrCompare;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return receiveMessage;
    }


    public static void sandToClient(String receiveMessage, Map<String, Socket> socketMap, Socket socket) throws Exception {
        if (receiveMessage.equals("compare") && socketMap.size() == 2) {

            keyPair = RASUtil.getgenerateKeyPair();

            log.info("create privateKey = " + keyPair.getPrivate());
            ResponseKey responseKey = new ResponseKey();

            responseKey.setPublicKeyToBase64(Base64Util.publicKeyToBase64(keyPair.getPublic()));


            socketMap.forEach((addressKey, socketValue) -> {
                try {
/*
                    keyPair = RASUtil.getgenerateKeyPair();

                    log.info("create privateKey = " + keyPair.getPrivate());
                    ResponseKey responseKey = new ResponseKey();

                    responseKey.setPublicKeyToBase64(RASUtil.publicKeyToBase64(keyPair.getPublic()));
*/

                    OutputStream outputStream = socketValue.getOutputStream();
                    PrintWriter printWriter = new PrintWriter(outputStream);
                    if (socketValue != socket) {
                        responseKey.setMessage("对方请求进行财富值比较，请按照格式输入(name-wealth)");

                        printWriter.println(JSON.toJSONString(responseKey));
                    } else {
                        responseKey.setMessage("请按照格式输入(name-wealth)");

                        printWriter.println(JSON.toJSONString(responseKey));
                    }
                    printWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });//返回比较信息
        } else if (receiveMessage.equals("BIG")) {
            socketMap.forEach((addressKey, socketValue) -> {
                try {
                    OutputStream outputStream = socketValue.getOutputStream();
                    PrintWriter printWriter = new PrintWriter(outputStream);
                    if (socketValue == socket) {
                        printWriter.println("You are richer");

                    } else {
                        printWriter.println("OtherSide are richer");
                    }
                    printWriter.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (receiveMessage.equals("SMALL")) {
            socketMap.forEach((addressKey, socketValue) -> {
                try {
                    OutputStream outputStream = socketValue.getOutputStream();
                    PrintWriter printWriter = new PrintWriter(outputStream);
                    if (socketValue == socket) {
                        printWriter.println("OtherSide are richer");

                    } else {
                        printWriter.println("You are richer");

                    }
                    printWriter.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (receiveMessage.equals("EQUATION")) {
            socketMap.forEach((addressKey, socketValue) -> {
                try {
                    OutputStream outputStream = socketValue.getOutputStream();
                    PrintWriter printWriter = new PrintWriter(outputStream);
                    printWriter.println("You are equally wealthy");
                    printWriter.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.println("已收到你的消息，message = " + receiveMessage);
            printWriter.flush();
        }
    }
}
