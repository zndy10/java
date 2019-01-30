package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServiceResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("用户名不存在！！！");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);

        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServiceResponse.createByErrorMessage("密码错误！！！");
        }

        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServiceResponse.createBySuccessMessage("登录成功", user);
    }

    public ServiceResponse<String> register(User user) {
        ServiceResponse validResponse = this.checkVaild(user.getUsername(),Const.USERNAME);
        if (!validResponse.isSuccess()) {
            return  validResponse;
        }

        validResponse = this.checkVaild(user.getEmail(),Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return  validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER); // 设置用户角色
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);

        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("注册失败");
        }

        return ServiceResponse.createBySuccessMessage("注册成功！！！");
    }

    public ServiceResponse<String> checkVaild(String str, String type) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(type)) {
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServiceResponse.createByErrorMessage("邮箱已存在");
                }
            }
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServiceResponse.createByErrorMessage("用户名已存在");
                }
            }
        } else {
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        return ServiceResponse.createBySuccessMessage("校验成功！！！");
    }

    public ServiceResponse<String> selectQuestion(String username) {
        ServiceResponse validResponse = this.checkVaild(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(question)) {
            return ServiceResponse.createBySuccessMessage(question);
        }
        return ServiceResponse.createByErrorMessage("找回密码的问题是空的");
    }

    public ServiceResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            String fotgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username, fotgetToken);
            return ServiceResponse.createBySuccessMessage(fotgetToken);
        }
        return  ServiceResponse.createByErrorMessage("问题答案错误");
    }

    public ServiceResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServiceResponse.createByErrorMessage("参数错误，token必须传递");
        }
        ServiceResponse validResponse = this.checkVaild(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)) {
            return ServiceResponse.createByErrorMessage("token无效或已经过期");
        }
        if (StringUtils.equals(forgetToken, token)) {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);

            if (rowCount > 0) {
                return ServiceResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServiceResponse.createByErrorMessage("token错误，重新获取重置密码的token");
        }
        return ServiceResponse.createByErrorMessage("修改密码失败");
    }

    public ServiceResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        // 防止横向越权
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("就密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServiceResponse.createBySuccessMessage("密码更新成功");
        }
        return  ServiceResponse.createByErrorMessage("密码更新失败");
    }

    public ServiceResponse<User> updateInfo(User user) {

        // username 不能更新
        // 检验邮箱时，要确定该邮箱是否存在，如果存在，不能是该用户的
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServiceResponse.createByErrorMessage("email已经存在，请更换email");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setPhone(user.getPhone());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServiceResponse.createBySuccessMessage("更新用户信息成功", updateUser);
        }
        return ServiceResponse.createByErrorMessage("更新用户信息失败");
    }

    public ServiceResponse<User> getInfo(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServiceResponse.createBySuccessMessage("找不到当前用户信息");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServiceResponse.createBySuccessMessage(user);
    }


    //backend 后台管理

    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
    public ServiceResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServiceResponse.createBySuccess();
        }
        return  ServiceResponse.createByError();
    }

 }
