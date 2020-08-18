package com.zhongsjie.service;


import com.zhongsjie.dao.UserDao;
import com.zhongsjie.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    // 使用构造器方式注入

    @Autowired
    UserDao userDao;

    public User getById(int id) {
        return userDao.getById(id);
    }

    @Transactional
    public boolean tx() {
        User u1 = new User();
        u1.setId(2);
        u1.setName("name2");
        userDao.insert(u1);

//        User u2 = new User();
//        u1.setId(1);
//        u1.setName("name1");
//        userDao.insert(u2);
        return true;
    }
}
