package com.caiwei.Security.Entities;

import lombok.Data;

@Data
public class ResponseKey {
    String message;//返回的消息
    String publicKeyToBase64;//Base64编码后的公钥
}
