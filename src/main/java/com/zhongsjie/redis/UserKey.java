package com.zhongsjie.redis;

public class UserKey extends BasePredix {
    public UserKey(String prefix) {
        super(prefix);
    }
    public static UserKey getById = new UserKey("id");
    public static UserKey getByName = new UserKey("name");
}
