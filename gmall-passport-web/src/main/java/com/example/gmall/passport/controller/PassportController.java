package com.example.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.example.gmall.bean.UmsMember;
import com.example.gmall.constant.SysConstant;
import com.example.gmall.service.UserService;
import com.example.gmall.util.CookieUtil;
import com.example.gmall.util.HttpclientUtil;
import com.example.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chy
 * @date 2020/4/12 12:42
 */
@Controller
public class PassportController {
    @Reference
    UserService userService;

    @RequestMapping("index")
    public String index(String originUrl, ModelMap modelMap) {
        modelMap.put("originUrl", originUrl);
        return "index";
    }

    /**
     * 单点登录
     *
     * @param umsMember
     * @param request
     * @param response
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @RequestMapping(value = "login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException {
        String token = "";
        UmsMember user = userService.login(umsMember);
        if (user != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("nickName", user.getNickname());
            userMap.put("memberLevelId", user.getMemberLevelId());
            userMap.put("birthday", user.getBirthday());
            userMap.put("city", user.getCity());
            userMap.put("gender", user.getGender());
            userMap.put("job", user.getJob());
            userMap.put("phone", user.getPhone());
            token = JwtUtil.encode(SysConstant.ENCRYKEY, userMap, SysConstant.SALT);
            CookieUtil.setCookie(request, response, "access_token", token, 60 * 60 * 72, true);
            String userJson = JSON.toJSONString(user);
            userService.addTokenUser(token, userJson);

        }

        return token;
    }

    @RequestMapping(value = "verify")
    @ResponseBody
    public String verify(String token) {
        UmsMember umsMember = userService.userVerify(token);
        if (umsMember != null) {
            return "success";
        }
        return null;

    }

    /**
     * 社交登录
     *
     * @param
     * @return
     */
    @RequestMapping("sologin")
    public String sologin(String code, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("client_id", "2846333812");
        paramMap.put("client_secret", "2c07e480e6bd2b0c2ba6dd0276e2b504");
        paramMap.put("grant_type", "authorization_code");
        paramMap.put("redirect_uri", "http://passport.mall.com:8085/sologin");
        paramMap.put("code", code);
        String jsonStr = HttpclientUtil.doPost(SysConstant.SOCIAL_LOGIN_GET_TOKEN_URL, paramMap);
        Map<String, String> resultMap = new HashMap<>();
        if (StringUtils.isNotBlank(jsonStr))
            resultMap = JSON.parseObject(jsonStr, Map.class);
        String access_token = resultMap.get("access_token");
        String uid = resultMap.get("uid");
        String userJson = HttpclientUtil.doGet(SysConstant.SOCIAL_LOGIN_GET_USERINFO_URL + "?access_token=" + access_token + "&uid=" + uid);
        UmsMember umsMember = new UmsMember();
        Map<String, Object> userMap = new HashMap<>();
        if (StringUtils.isNotBlank(userJson)) {
            Map map = JSON.parseObject(userJson, Map.class);
            umsMember.setCity((String) map.get("location"));
            userMap.put("city", umsMember.getCity());
            umsMember.setCreateTime((Date) map.get("created_at"));
            userMap.put("createTime", map.get("created_at"));
            if ("m".equals(map.get("gender"))) {
                umsMember.setGender(1);
            } else if ("f".equals(map.get("gender"))) {
                umsMember.setGender(1);
            } else {
                umsMember.setGender(0);
            }
            userMap.put("gender", umsMember.getGender());
            umsMember.setSourceUid((String) map.get("id"));
            umsMember.setNickname((String) map.get("screen_name"));
        }
        String token = JwtUtil.encode(SysConstant.ENCRYKEY, userMap, SysConstant.SALT);
        umsMember.setAccessToken(access_token);
        umsMember.setAccessCode(code);

        CookieUtil.setCookie(request, response, "access_token", token, 60 * 60 * 72, true);
        String user = JSON.toJSONString(umsMember);
        // 判断此社交用户是否曾经登录过，未登录过将账户信息存入数据库，登录过则数据库中存在该账户信息不需要存储
        UmsMember checkUser = userService.checkLoginRecord(umsMember);
        if (checkUser == null) {
            userService.addUserToDb(umsMember);
            userService.addTokenUser(token, user);
        }

        return "redirect:search.mall.com:8083/index?access_token=" + token;
    }

}
