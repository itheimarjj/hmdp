package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.entity.User;

import javax.servlet.http.HttpSession;


public interface IUserService extends IService<User> {

    void sendCode(String phone, HttpSession session);
}
