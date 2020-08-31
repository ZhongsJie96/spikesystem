package com.zhongsjie.dao;

import com.zhongsjie.domain.SpikeGoods;
import com.zhongsjie.vo.GoodsVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GoodsDao {

    /** 联合查询*/
    @Select("select g.*,sg.stock_count,sg.start_date,sg.end_date,sg.spike_price from spike_goods sg left join goods g on sg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*,sg.stock_count,sg.start_date,sg.end_date,sg.spike_price from spike_goods sg left join goods g on sg.goods_id = g.id where g.id = #{goodsId}")
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = long.class, before = false, statement = "select_insert_id()")
    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId")long goodsId);

    @Update("update spike_goods set stock_count = stock_count - 1 where goods_id = #{goodsId}")
    public int reduceStock(SpikeGoods g);
}
