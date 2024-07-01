package com.caiwei.Security.Mapper;


import com.caiwei.Security.Entities.User;

public interface UserMapper {

    Integer addUser(User user);

    User selectOtherSide(String address);

    void updateToDelete();
}
