package com.example.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.example.gmall.bean.UmsMember;
import com.example.gmall.bean.UmsMemberReceiveAddress;
import com.example.gmall.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class UserController {
    @Reference
    UserService userService;

    @GetMapping("index")
    public String index() {
        return "hello";
    }

    @GetMapping("getReceiveAddressByMemberId")
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        return userService.getReceiveAddressByMemberId(memberId,null);
    }

    @GetMapping("getAllUser")
    public List<UmsMember> getAllUser() {
        return userService.getAllUser();
    }
}
