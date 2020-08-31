package com.zhongsjie.service;

import com.zhongsjie.dao.GoodsDao;
import com.zhongsjie.domain.SpikeGoods;
import com.zhongsjie.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    public void reduceStock(GoodsVo goods) {
        SpikeGoods g = new SpikeGoods();
        g.setGoodsId(goods.getId());
        goodsDao.reduceStock(g);
    }
}
