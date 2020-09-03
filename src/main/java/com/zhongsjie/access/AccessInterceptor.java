package com.zhongsjie.access;

import com.alibaba.fastjson.JSON;
import com.zhongsjie.domain.SpikeUser;
import com.zhongsjie.redis.AccessKey;
import com.zhongsjie.redis.RedisService;
import com.zhongsjie.result.CodeMsg;
import com.zhongsjie.result.Result;
import com.zhongsjie.service.SpikeUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.thymeleaf.util.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器
 * @author zhong
 */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    SpikeUserService userService;

    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            // 获取用户
            SpikeUser user = getUser(request, response);
            // 保存用户到LocalThread
            UserContext.setUser(user);
            // 注解
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit == null) {
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    // 客户端提示
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            } else {
                // do nothing
            }
            // 通过注解中的有效时间来生成带有有效时间的key
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if (count == null) {
                redisService.set(ak, key, 1);
            } else if (count < maxCount) {
                redisService.incr(ak, key);
            } else {
                render(response, CodeMsg.ACCESS_LIMIT);
                return false;
            }

        }
        return true;

    }

    /**
     * 给客户端提示
     * @param response
     * @param cm
     * @throws Exception
     */
    private void render(HttpServletResponse response, CodeMsg cm) throws Exception{
        // 设置输出格式
        response.setContentType("application/json;charset=UTF-8");
        ServletOutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    public SpikeUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(SpikeUserService.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValue(request, SpikeUserService.COOKI_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        /*有先从paramToken 中取出 cookie值 若没有从 cookieToken 中取*/
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        // 利用token从redis找到用户
        return userService.getByToken(response, token);
    }

    private static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie :cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }




}
