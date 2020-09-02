package com.zhongsjie.service;

import com.zhongsjie.dao.SpikeUserDao;
import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.exception.GlobalException;
import com.zhongsjie.redis.RedisService;
import com.zhongsjie.redis.SpikeUserKey;
import com.zhongsjie.result.CodeMsg;
import com.zhongsjie.utils.MD5Util;
import com.zhongsjie.utils.UUIDUtil;
import com.zhongsjie.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * 实现秒杀用户的相关服务
 * 1. 实现登录服务
 */
@Service
public class SpikeUserService {

    public static final String COOKI_NAME_TOKEN = "token";

    @Autowired
    RedisService redisService;
    @Autowired
    SpikeUserDao spikeUserDao;

    public SpikeUser getById(long id) {

        // 取缓存
        SpikeUser user = redisService.get(SpikeUserKey.getById, ""+id, SpikeUser.class);
        if (user != null) {
            return user;
        }
        // 取数据库
        user = spikeUserDao.getById(id);
        if (user != null) {
            redisService.set(SpikeUserKey.getById, ""+id, user);
        }

//        return spikeUserDao.getById(id);
        return user;
    }

    public boolean updatePassword(String token, long id, String formPass) {
        SpikeUser user = getById(id);

        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        // 更新数据库
        SpikeUser toBeUpdate = new SpikeUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        spikeUserDao.update(toBeUpdate);
        // 处理缓存
        redisService.delete(SpikeUserKey.getById,""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(SpikeUserKey.token, token, user);
        return true;
    }

    /**
     * 登录服务
     * 1. 判断用户是否存在
     * 2. 验证密码
     * @param loginVo
     * @return
     */
    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        // 1. 判断用户是否存在，从数据库获取用户
        SpikeUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        // 2. 验证密码
        String dbPass = user.getPassword();
        // 需要和数据库一致
        String saltDb = user.getSalt();
        // MD5
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDb);
        if (!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        // 生成cookie,需要将token添加到cookie中，不用每次都生成token
        String token = UUIDUtil.uuid();
        // 登录成功之后将token和用户信息缓存到redis
        addCookie(response, token, user);
        return true;
    }

    /**
     * 添加cookie
     * @param response
     * @param token
     * @param user
     */
    private void addCookie(HttpServletResponse response, String token, SpikeUser user) {
        // 在redis之中进行缓存
        redisService.set(SpikeUserKey.token, token, user);
        // cookie
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        // 设置过期时间
        cookie.setMaxAge(SpikeUserKey.token.expireSeconds());
        cookie.setPath("/");
        // 添加到response
        response.addCookie(cookie);
    }

    public SpikeUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        SpikeUser user = redisService.get(SpikeUserKey.token, token, SpikeUser.class);
        // 延长有效期
        if (user != null) {
            addCookie(response, token, user);
        }
        return user;
    }
}
