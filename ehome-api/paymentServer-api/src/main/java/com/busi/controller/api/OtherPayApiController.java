package com.busi.controller.api;

import com.busi.entity.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * 第三方支付平台 加签 回调相关接口
 * author：SunTianJie
 * create time：2018/8/31 14:18
 */
public interface OtherPayApiController {

    /***
     *  第三方平台加签接口
     * @param platformType 第三方支付平台类型  1：支付宝, 2：微信, 3银联, 4银联token版
     * @param payType      支付类型 1：充值，2：预留...
     * @param sum          充值金额 小数点后两位
     * @return
     */
    @GetMapping("rechargeDataSign/{platformType}/{payType}/{sum}")
    ReturnData rechargeDataSign(@PathVariable int platformType,@PathVariable int payType,@PathVariable double sum);

    /***
     * 支付宝回调验签接口
     * @param alipayBean
     * @return
     * 注意：此处对象不能加 @RequestBody 注解 否则无法回调到接口中
     */
    @PostMapping("checkAlipaySign")
    String checkAlipaySign(AlipayBean alipayBean);

    /***
     * 微信回调验签接口
     * @param weixinSignBean
     * @return
     */
    @PostMapping("checkWeixinSign")
    String checkWeixinSign(@RequestBody WeixinSignBean weixinSignBean);

    /***
     * 银联回调验签接口
     * @param unionpayBean
     * @return
     */
    @PostMapping("checkUnionPaySign")
    String checkUnionPaySign(@RequestBody UnionpayBean unionpayBean);

}
