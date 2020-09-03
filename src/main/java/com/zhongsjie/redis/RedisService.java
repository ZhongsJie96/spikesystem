package com.zhongsjie.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {

    /** 注入连接池*/
    @Autowired
    JedisPool jedisPool;

    /**
     * 获取单个对象
     */
    public <T> T get(KeyPrefix predix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 生成真正的key
            String realKey = predix.getPrefix() + key;
            String str = jedis.get(realKey);
            // 通过StringToBean将str类型转化为T类型
            T t = stringToBean(str, clazz);
            return t;
        } finally {
            returnToPool(jedis);
        }
    }
    /**
     * 设置对象
     */
    public <T> boolean set(KeyPrefix predix, String key, T value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if (str == null || str.length() == 0) {
                return false;
            }
            // 生成真正的key
            String realKey = predix.getPrefix() + key;
            int seconds = predix.expireSeconds();
            if (seconds <= 0) {
                jedis.set(realKey, str);
            } else {
                jedis.setex(realKey, seconds, str);
            }

            return true;
        } finally {
            returnToPool(jedis);
        }
    }
    /**
     * 判断key是否存在
     */
    public <T> boolean exit(KeyPrefix predix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 生成真正的key
            String realKey = predix.getPrefix() + key;
            return jedis.exists(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 增加值
     * */
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            return  jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 减少值
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            return  jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 删除
     * @param prefix
     * @param key
     * @return
     */
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            long ret = jedis.del(key);
            return ret > 0;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 为了存储到redis中 将传入的不同类型的数据转化为String
     * */
    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        // 获取类型，对不同类型的值分别进行处理
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String)value;
        } else if (clazz == long.class || clazz == Long.class) {
            return "" + value;
        } else {
            // 其他类型 默认为Bean对象 利用JSON 转化为json串
            // 利用阿里的 库 fastjson 将对象转化为 json 串  序列化和反序列化
            return JSON.toJSONString(value);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() == 0) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T)Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T)str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T)Long.valueOf(str);
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }


    }

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }


}
