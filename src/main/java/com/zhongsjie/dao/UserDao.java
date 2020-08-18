package com.zhongsjie.dao;

import com.zhongsjie.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {

    @Select("SELECT * FROM user WHERE id = #{id}")
    User getById(@Param("id")int id);

    @Insert("insert into user(id, name)values(#{id}, #{name})")
    public int insert(User user);
}
