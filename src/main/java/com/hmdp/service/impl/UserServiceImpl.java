package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {

        //1获取手机号校验
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(phone);
        //2如果错误，返回错误信息
        if (phoneInvalid){
            return Result.fail("手机号格式不正确");
        }
        //3如果手机号没问题，生成验证码
        String code= RandomUtil.randomNumbers(6);
        //4把验证码存到seession
        session.setAttribute("code",code);
                //5返回验证码
        System.out.println(code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1获取手机号并验证，如果错误返回错误信息
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(loginForm.getPhone());
        if (phoneInvalid==true){
            return Result.fail("手机号有问题");
        }
        //2校验验证码，为空或者错误返回错误信息
        String code = (String) session.getAttribute("code");
        if (code==null|| code.equals(loginForm.getCode())==false){
            return Result.fail("验证码错误");
        }
        //3根据手机号查询用户
        User user = query().eq("phone", loginForm.getPhone()).one();
        //3.1如果不存在就创建新用户
        if (user==null){
            user=new User();
            user.setPhone(loginForm.getPhone());
            user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
            //3.2保存新用户到数据库
            save(user);
        }

        //保存用户到session
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }
}
