package com.zhongsjie.service;

import com.zhongsjie.dao.SpikeUserDao;
import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.exception.GlobalException;
import com.zhongsjie.result.CodeMsg;
import com.zhongsjie.utils.MD5Util;
import com.zhongsjie.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 实现秒杀用户的相关服务
 * 1. 实现登录服务
 */
@Service
public class SpikeUserService {

    @Autowired
    SpikeUserDao spikeUserDao;

    public SpikeUser getById(long id) {
        return spikeUserDao.getById(id);
    }

    /**
     * 登录服务
     * 1. 判断用户是否存在
     * 2. 验证密码
     * @param loginVo
     * @return
     */
    public boolean login(LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        // 1. 判断用户是否存在，从数据库获取用户
        SpikeUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NO_EXIST);
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
        return true;
    }
}
