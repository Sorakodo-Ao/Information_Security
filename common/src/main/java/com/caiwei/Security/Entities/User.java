package com.caiwei.Security.Entities;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName user
 */
@Data
public class User implements Serializable {

    private Integer id;

    private String name;

    private String address;

    private String wealthEncryption;//加密后的财富值存储到数据库

}