package com.example.gmall.service;


import com.example.gmall.bean.UmsMember;
import com.example.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId,String id);

    UmsMember login(UmsMember umsMember);

    void addTokenUser(String token, String userJson);

    UmsMember userVerify(String token);

    UmsMember checkLoginRecord(UmsMember umsMember);

    void addUserToDb(UmsMember umsMember);
}
