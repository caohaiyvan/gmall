package com.example.gmall.service;

import com.example.gmall.bean.OmsCartItem;

import java.util.List;

/**
 * @author chy
 * @date 2020/4/10 12:11
 */

public interface CartService {

    OmsCartItem is_exist_cartItemDb(String memberId,String skuId);

    void editCartItem(OmsCartItem dbCartItem);

    void flushCache(OmsCartItem dbCartItem);

    void addCartItem(OmsCartItem omsCartItem);

    List<OmsCartItem> cartList(String memberId);

    void removeCartByMemberIdAndIschecked(String memberId);
}
