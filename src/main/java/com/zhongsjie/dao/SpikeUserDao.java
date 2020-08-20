package com.zhongsjie.dao;

import com.zhongsjie.domain.SpikeUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SpikeUserDao {

    @Select("SELECT * FROM spike_user WHERE id = #{id}")
    public SpikeUser getById(@Param("id")long id);
}
