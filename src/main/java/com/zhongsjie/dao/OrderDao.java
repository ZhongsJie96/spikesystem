package com.zhongsjie.dao;

import com.zhongsjie.domain.OrderInfo;
import com.zhongsjie.domain.SpikeOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {

    @Select("select * from spike_order where user_Id = #{userId} and goods_Id = #{goodsId}")
    SpikeOrder getSpikeOrderByUserIdGoodsId(@Param("userId")long userId, @Param("goodsId") long goodsId);

    @Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)values("
            + "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
    @SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
    long insert(OrderInfo orderInfo);

    @Insert("insert into spike_order(user_id, goods_id, order_id)values(#{userId},#{goodsId},#{orderId})")
    int insertSpikeOrder(SpikeOrder spikeOrder);

    @Select("select * from order_info where id = #{orderId}")
    OrderInfo getOrderById(@Param("orderId") long orderId);
}
