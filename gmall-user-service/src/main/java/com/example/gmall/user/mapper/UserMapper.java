package com.example.gmall.user.mapper;


import com.example.gmall.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

public interface UserMapper extends Mapper<UmsMember> {
    // @Select("select username from ums_member where id = 1")
    // String selectAllUser();
}
