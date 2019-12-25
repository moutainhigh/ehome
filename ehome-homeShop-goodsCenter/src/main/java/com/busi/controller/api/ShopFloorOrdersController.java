package com.busi.controller.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.busi.controller.BaseController;
import com.busi.entity.*;
import com.busi.service.ShopFloorGoodsService;
import com.busi.service.ShopFloorOrdersService;
import com.busi.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: ehome
 * @description: 楼店订单相关接口
 * @author: ZhaoJiaJie
 * @create: 2019-12-17 15:13
 */
@RestController
public class ShopFloorOrdersController extends BaseController implements ShopFloorOrdersApiController {

    @Autowired
    private MqUtils mqUtils;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    UserInfoUtils userInfoUtils;

    @Autowired
    ShippingAddressUtils addressUtils;

    @Autowired
    ShopFloorGoodsService shopFloorGoodsService;

    @Autowired
    ShopFloorOrdersService shopFloorOrdersService;

    /***
     * 新增订单
     * @param shopFloorOrders
     * @param bindingResult
     * @return
     */
    @Override
    public ReturnData addSForders(@Valid @RequestBody ShopFloorOrders shopFloorOrders, BindingResult bindingResult) {
        //验证参数格式是否正确
        if (bindingResult.hasErrors()) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, checkParams(bindingResult), new JSONObject());
        }
        ShippingAddress shippingAddress = addressUtils.findAddress(shopFloorOrders.getAddressId());
        if (shippingAddress == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "下单失败！收货地址不存在！", new JSONObject());
        }
        String goods = ""; //商品信息
        String goodsTitle = ""; //商品标题
        double cost = 0.00; //商品价格
        double money = 0.00; // 商品总金额
        String imgUrl = ""; //图片
        List iup = null;
        ShopFloorGoods laf = null;
        if (CommonUtils.checkFull(shopFloorOrders.getGoodsIds()) || CommonUtils.checkFull(shopFloorOrders.getGoodsNumber())) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
        }
        String[] sd = shopFloorOrders.getGoodsIds().split(",");//商品ID
        String[] fn = shopFloorOrders.getGoodsNumber().split(",");//商品数量

        iup = shopFloorGoodsService.findList(sd);
        if (iup == null || iup.size() <= 0) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
        }
        laf = (ShopFloorGoods) iup.get(0);
        if (laf == null || iup.size() != sd.length) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
        }
        for (int i = 0; i < iup.size(); i++) {
            laf = null;
            laf = (ShopFloorGoods) iup.get(i);
            for (int j = 0; j < sd.length; j++) {
                if (laf.getId() == Long.parseLong(sd[j])) {//确认是当前商品ID
                    //判断是否有折扣
                    if (laf.getDiscountPrice() > 0) {
                        cost = laf.getDiscountPrice();//折扣价
                    } else {
                        cost = laf.getPrice();//原价
                    }
                    goodsTitle = laf.getGoodsTitle();//标题
                    if (CommonUtils.checkFull(laf.getGoodsCoverUrl())) {
                        String[] img = laf.getImgUrl().split(",");
                        imgUrl = img[0];//用第一张图做封面
                    } else {
                        imgUrl = laf.getGoodsCoverUrl();//图片
                    }
                    goods += laf.getId() + "," + goodsTitle + "," + Integer.parseInt(fn[j]) + "," + cost + "," + imgUrl + (i == iup.size() - 1 ? "" : ";");//商品ID,标题,数量,价格，图片;
                    money += Integer.parseInt(fn[j]) * cost;//总价格
                }
            }
        }
        Date date = new Date();
        long time = new Date().getTime();
        String noTime = String.valueOf(time);
        String random = CommonUtils.getRandom(6, 1);
        String noRandom = CommonUtils.strToMD5(noTime + shopFloorOrders.getBuyerId() + random, 16);

        shopFloorOrders.setNo(noRandom);//订单编号【MD5】
        shopFloorOrders.setAddTime(date);
        shopFloorOrders.setGoods(goods);//商品ID,标题,数量,价格，图片;
        shopFloorOrders.setMoney(money + shopFloorOrders.getFreight());//总价
        shopFloorOrders.setOrdersType(0);

        shopFloorOrdersService.addOrders(shopFloorOrders);
        Map<String, Object> map = new HashMap<>();
        map.put("no", shopFloorOrders.getNo());

        //放入缓存
        // 付款超时 45分钟
        Map<String, Object> ordersMap = CommonUtils.objectToMap(shopFloorOrders);
        redisUtils.hmset(Constants.REDIS_KEY_SHOPFLOORORDERS + shopFloorOrders.getBuyerId() + "_" + shopFloorOrders.getNo(), ordersMap, Constants.TIME_OUT_MINUTE_45);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", map);
    }

    /***
     * 删除订单
     * @param id 订单ID
     * @return
     */
    @Override
    public ReturnData delSForders(@PathVariable long id) {
        ShopFloorOrders io = shopFloorOrdersService.findById(id, CommonUtils.getMyId(), 0);
        if (io == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "订单不存在！", new JSONObject());
        }
//        if (io.getOrdersState() != 0) {
//            io.setOrdersState(io.getSellerId() == CommonUtils.getMyId() ? (io.getOrdersState() == 1 ? 3 : 2) : (io.getOrdersState() == 2 ? 3 : 1));
//        } else {
//            io.setOrdersState(io.getSellerId() == CommonUtils.getMyId() ? 2 : 1);
//        }
        io.setUpdateCategory(0);
        shopFloorOrdersService.updateOrders(io);
        //清除缓存中的厨房订座订单信息
        redisUtils.expire(Constants.REDIS_KEY_SHOPFLOORORDERS + io.getBuyerId() + "_" + io.getNo(), 0);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 更改发货状态
     * 由未发货改为已发货
     * @param id  订单Id
     * @return
     */
    @Override
    public ReturnData changeSFdeliver(@PathVariable long id) {
        ShopFloorOrders io = shopFloorOrdersService.findById(id, CommonUtils.getMyId(), 1);
        if (io == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "订单不存在！", new JSONObject());
        }
        if (io.getBuyerId() != CommonUtils.getMyId()) {
            io.setOrdersType(2);
            io.setDeliveryTime(new Date());
            io.setUpdateCategory(1);
            shopFloorOrdersService.updateOrders(io);

            //清除缓存中的订单信息
            redisUtils.expire(Constants.REDIS_KEY_SHOPFLOORORDERS + io.getNo(), 0);
            //订单放入缓存
            Map<String, Object> ordersMap = CommonUtils.objectToMap(io);
            redisUtils.hmset(Constants.REDIS_KEY_SHOPFLOORORDERS + io.getBuyerId() + "_" + io.getNo(), ordersMap, Constants.USER_TIME_OUT);
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 更改收货状态
     * 由未收货改为已收货
     * @param id  订单Id
     * @return
     */
    @Override
    public ReturnData changeSFreceipt(@PathVariable long id) {
        ShopFloorOrders io = shopFloorOrdersService.findById(id, CommonUtils.getMyId(), 2);
        if (io == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "订单不存在！", new JSONObject());
        }
        if (io.getBuyerId() == CommonUtils.getMyId()) {
            //由已接单改为已完成
            io.setOrdersType(3);        //已收货
            io.setReceivingTime(new Date());
            io.setUpdateCategory(2);
            shopFloorOrdersService.updateOrders(io);
            //更新销量

            //清除缓存中的商品信息

            //清除缓存中的订单信息
            redisUtils.expire(Constants.REDIS_KEY_SHOPFLOORORDERS + io.getNo(), 0);
            //订单放入缓存
            Map<String, Object> ordersMap = CommonUtils.objectToMap(io);
            redisUtils.hmset(Constants.REDIS_KEY_SHOPFLOORORDERS + io.getBuyerId() + "_" + io.getNo(), ordersMap, 0);
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 分页查询订单列表
     * @param ordersType 订单类型: 0全部 1待付款,2待发货(已付款),3已发货（待收货）, 4已收货（待评价）  5已评价  6付款超时、发货超时、买家取消订单、卖家取消订单
     * @param page     页码 第几页 起始值1
     * @param count    每页条数
     * @return
     */
    @Override
    public ReturnData findSFordersList(@PathVariable int ordersType, @PathVariable int page, @PathVariable int count) {
        //验证参数
        if (page < 0 || count <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "分页参数有误", new JSONObject());
        }
        //开始查询
        PageBean<ShopFloorOrders> pageBean;
        pageBean = shopFloorOrdersService.findOrderList(CommonUtils.getMyId(), ordersType, page, count);
        if (pageBean == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, StatusCode.CODE_SUCCESS.CODE_DESC, new JSONArray());
        }
        List list = null;
        list = pageBean.getList();
        ShopFloorOrders t = null;
        UserInfo userCache = null;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                t = (ShopFloorOrders) list.get(i);
                if (t != null) {
                    userCache = userInfoUtils.getUserInfo(t.getBuyerId());
                    if (userCache != null) {
                        t.setName(userCache.getName());
                        t.setHead(userCache.getHead());
                        t.setProTypeId(userCache.getProType());
                        t.setHouseNumber(userCache.getHouseNumber());
                    }
                }
            }
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, StatusCode.CODE_SUCCESS.CODE_DESC, list);
    }

    /***
     * 取消订单
     * @param id  订单Id
     * @return
     */
    @Override
    public ReturnData cancelSForders(@PathVariable long id) {
        ShopFloorOrders ko = null;
        ko = shopFloorOrdersService.findById(id, CommonUtils.getMyId(), 3);
        if (ko == null) {
            return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "您要查看的订单不存在", new JSONObject());
        }
        ko.setOrdersType(7);
        ko.setUpdateCategory(3);
        shopFloorOrdersService.updateOrders(ko);//更新订单
        //更新缓存、钱包、账单
        if (ko.getOrdersType() > 0) {
            mqUtils.sendPurseMQ(ko.getBuyerId(), 27, 0, ko.getMoney());
        }
        //清除缓存中的订单信息
        redisUtils.expire(Constants.REDIS_KEY_SHOPFLOORORDERS + ko.getNo(), 0);
        //放入缓存
        Map<String, Object> ordersMap = CommonUtils.objectToMap(ko);
        redisUtils.hmset(Constants.REDIS_KEY_SHOPFLOORORDERS + ko.getBuyerId() + "_" + ko.getNo(), ordersMap, Constants.USER_TIME_OUT);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 查看订单详情
     * @param no  订单编号
     * @return
     */
    @Override
    public ReturnData orderDetails(@PathVariable String no) {
        //查询缓存 缓存中不存在 查询数据库
        ShopFloorOrders io = null;
        Map<String, Object> ordersMap = redisUtils.hmget(Constants.REDIS_KEY_SHOPFLOORORDERS + CommonUtils.getMyId() + "_" + no);
        if (ordersMap == null || ordersMap.size() <= 0) {
            io = shopFloorOrdersService.findNo(no);
            if (io == null) {
                return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "您要查看的订单不存在", new JSONObject());
            }
            UserInfo userInfo = null;
            userInfo = userInfoUtils.getUserInfo(io.getBuyerId());
            if (userInfo != null) {
                io.setName(userInfo.getName());
                io.setHead(userInfo.getHead());
                io.setProTypeId(userInfo.getProType());
                io.setHouseNumber(userInfo.getHouseNumber());
            }
            //放入缓存
            ordersMap = CommonUtils.objectToMap(io);
            redisUtils.hmset(Constants.REDIS_KEY_SHOPFLOORORDERS + CommonUtils.getMyId() + "_" + no, ordersMap, Constants.USER_TIME_OUT);
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", ordersMap);
    }

    /***
     * 统计各类订单数量
     * @return
     */
    @Override
    public ReturnData findSFordersCount() {
        int orderCont0 = 0;
        int orderCont1 = 0;
        int orderCont2 = 0;
        int orderCont3 = 0;
        int orderCont4 = 0;
        int orderCont5 = 0;

        ShopFloorOrders kh = null;
        List list = null;
        list = shopFloorOrdersService.findIdentity(CommonUtils.getMyId());//全部
        for (int i = 0; i < list.size(); i++) {
            kh = (ShopFloorOrders) list.get(i);
            switch (kh.getOrdersType()) {
                case 0://待付款
                    orderCont1++;
                    orderCont0++;
                    break;
                case 1://待发货
                    orderCont2++;
                    orderCont0++;
                    break;
                case 2://待收货
                    orderCont3++;
                    orderCont0++;
                    break;
                case 3://待评价
                    orderCont4++;
                    orderCont0++;
                    break;
                case 7://买家取消
                    orderCont5++;
                    orderCont0++;
                    break;
                default:
                    orderCont0++;
                    break;
            }
        }
        Map<String, Integer> map = new HashMap<>();
        map.put("orderCont0", orderCont0);
        map.put("orderCont1", orderCont1);
        map.put("orderCont2", orderCont2);
        map.put("orderCont3", orderCont3);
        map.put("orderCont4", orderCont4);
        map.put("orderCont5", orderCont5);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", map);
    }
}
