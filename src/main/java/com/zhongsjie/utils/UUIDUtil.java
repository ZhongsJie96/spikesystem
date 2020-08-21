package com.zhongsjie.utils;

import java.util.UUID;

/**
 * 用于生成统一唯一标识符
 */
public class UUIDUtil {
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
