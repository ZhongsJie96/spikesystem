package com.zhongsjie.dao;

import com.zhongsjie.domain.SpikeUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SpikeUserDao {

    @Select("SELECT * FROM spike_user WHERE id = #{id}")
    public SpikeUser getById(@Param("id")long id);


    @Update("update spike_user set password = #{password} where id = #{id}")
    void update(SpikeUser toBeUpdate);
}
