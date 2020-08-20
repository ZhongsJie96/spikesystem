package com.zhongsjie.utils;


import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 验证工具类，用于验证用户输入的手机号是否正确。
 * @author zhong
 */
public class ValidatorUtil {
    /** 模式匹配*/
    private static final Pattern MOBILE_PATTERN = Pattern.compile("1\\d{10}");
    public static boolean isMobile(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        Matcher m = MOBILE_PATTERN.matcher(str);
        return m.matches();
    }
}
