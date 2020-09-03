package com.zhongsjie.rabbitmq;

import com.zhongsjie.domain.SpikeUser;

public class SpikeMessage {
    private SpikeUser user;

    public SpikeUser getUser() {
        return user;
    }

    public void setUser(SpikeUser user) {
        this.user = user;
    }

    public long getGoodId() {
        return goodId;
    }

    public void setGoodId(long goodId) {
        this.goodId = goodId;
    }

    private long goodId;
}
