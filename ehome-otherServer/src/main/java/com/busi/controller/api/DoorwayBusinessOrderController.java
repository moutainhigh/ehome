package com.busi.controller.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.busi.controller.BaseController;
import com.busi.entity.*;
import com.busi.service.DoorwayBusinessOrderService;
import com.busi.service.DoorwayBusinessService;
import com.busi.service.ShippingAddressService;
import com.busi.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: ehome
 * @description: 家门口隐形商家订单相关接口
 * @author: ZhaoJiaJie
 * @create: 2020-11-18 14:29:08
 */
@RestController
public class DoorwayBusinessOrderController extends BaseController implements DoorwayBusinessOrderApiController {

    @Autowired
    MqUtils mqUtils;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    UserInfoUtils userInfoUtils;

    @Autowired
    DoorwayBusinessService travelService;

    @Autowired
    DoorwayBusinessOrderService travelOrderService;

    @Autowired
    ShippingAddressService shippingAddressService;

    /***
     * 新增订单
     * @param scenicSpotOrder
     * @param bindingResult
     * @return
     */
    @Override
    public ReturnData addBusinessOrder(@Valid @RequestBody DoorwayBusinessOrder scenicSpotOrder, BindingResult bindingResult) {
        //验证参数格式是否正确
        if (bindingResult.hasErrors()) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, checkParams(bindingResult), new JSONObject());
        }
        String dishame = "";
        String dishes = "";
        double money = 0.0;
        String imgUrl = "";   //图片
//        String specs = "";    //规格
        Date date = new Date();
        DoorwayBusinessCommodity laf = null;
        DoorwayBusinessCommodity dis = null;
        List iup = null;
        ShippingAddress s = null;
        Map<String, Object> map = new HashMap<>();
        //查询商家 缓存中不存在 查询数据库
        Map<String, Object> kitchenMap = redisUtils.hmget(Constants.REDIS_KEY_DOORWAYBUSINESS + scenicSpotOrder.getUserId());
        if (kitchenMap == null || kitchenMap.size() <= 0) {
            DoorwayBusiness kitchen = travelService.findReserve(scenicSpotOrder.getUserId());
            if (kitchen == null) {
                return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "新增订单失败,商家不存在", new JSONObject());
            }
            //放入缓存
            kitchenMap = CommonUtils.objectToMap(kitchen);
            redisUtils.hmset(Constants.REDIS_KEY_DOORWAYBUSINESS + kitchen.getUserId(), kitchenMap, Constants.USER_TIME_OUT);
        }
        DoorwayBusiness kh = (DoorwayBusiness) CommonUtils.mapToObject(kitchenMap, DoorwayBusiness.class);
        if (kh == null) {
            return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "新增订单失败,商家不存在", new JSONObject());
        }
        if (CommonUtils.checkFull(scenicSpotOrder.getTicketsIds()) || CommonUtils.checkFull(scenicSpotOrder.getTicketsNumber())) {
            return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "新增订单失败,商品信息不可为空", new JSONObject());
        }
        if (scenicSpotOrder.getDistributionMode() == 0) {
            s = shippingAddressService.findUserById(scenicSpotOrder.getAddressId());
            if (s == null) {
                return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "新增订单失败,收货地址有误", new JSONObject());
            }
            scenicSpotOrder.setAddress(s.getAddress());
            scenicSpotOrder.setAddress_Name(s.getContactsName());
            scenicSpotOrder.setAddress_Phone(s.getContactsPhone());
        }
        String[] sd = scenicSpotOrder.getTicketsIds().split(",");//商品ID
        String[] fn = scenicSpotOrder.getTicketsNumber().split(",");//商品数量
        if (sd != null && fn != null) {
            iup = travelService.findDishesList(sd);
            if (iup == null || iup.size() <= 0) {
                return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "新增订单失败,商品不存在", new JSONObject());
            }
            laf = (DoorwayBusinessCommodity) iup.get(0);
            if (laf == null || scenicSpotOrder.getMyId() == laf.getUserId() || iup.size() != sd.length) {
                return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "新增订单失败,商品信息错误", new JSONObject());
            }
            for (int i = 0; i < iup.size(); i++) {
                dis = (DoorwayBusinessCommodity) iup.get(i);
                for (int j = 0; j < sd.length; j++) {
                    if (dis.getId() == Long.parseLong(sd[j])) {//确认是当前商品ID
                        double cost = dis.getCost();//单价
                        dishame = dis.getName();//商品名称
                        if (!CommonUtils.checkFull(dis.getPicture())) {
                            String[] img = dis.getPicture().split(",");
                            imgUrl = img[0];//用第一张图做封面
                        }
//                        specs = dis.getSpecifications();//规格
                        dishes += dis.getId() + "," + dishame + "," + Integer.parseInt(fn[j]) + "," + cost + "," + imgUrl + "," + (i == iup.size() - 1 ? "" : ";");//商品ID,名称,数量,价格,图片;
                        money += Integer.parseInt(fn[j]) * cost;//总价格
                    }
                }
            }
        }
        if (money <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "订单总金额不能为0，请核实后重新下单！", new JSONObject());
        }
        long time = date.getTime();
        String noTime = String.valueOf(time);
        String random = CommonUtils.getRandom(6, 1);
        String noRandom = CommonUtils.strToMD5(noTime + scenicSpotOrder.getMyId() + random, 16);
        scenicSpotOrder.setNo(noRandom);//订单编号【MD5】

        String random2 = CommonUtils.getRandom(6, 1);
        String noRandom2 = CommonUtils.strToMD5(noTime + scenicSpotOrder.getMyId() + random2, 16);
        scenicSpotOrder.setVoucherCode(noRandom2);//凭证码【MD5】
        scenicSpotOrder.setAddTime(date);
        scenicSpotOrder.setPharmacyId(kh.getId());
        scenicSpotOrder.setPharmacyName(kh.getBusinessName());
        if (!CommonUtils.checkFull(kh.getPicture())) {
            String[] strings = kh.getPicture().split(",");
            scenicSpotOrder.setSmallMap(strings[0]);
        }
        scenicSpotOrder.setDishameCost(dishes);//名称,数量,价格,图片
        scenicSpotOrder.setMoney(money);//总价
        travelOrderService.addOrders(scenicSpotOrder);
        map.put("infoId", scenicSpotOrder.getNo());

        //放入缓存
        Map<String, Object> ordersMap = CommonUtils.objectToMap(scenicSpotOrder);
        redisUtils.hmset(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + scenicSpotOrder.getMyId() + "_" + scenicSpotOrder.getNo(), ordersMap, Constants.USER_TIME_OUT);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", map);
    }

    /***
     * 删除订单
     * @param id 订单ID
     * @return
     */
    @Override
    public ReturnData delBusinessOrder(@PathVariable long id) {
        DoorwayBusinessOrder io = travelOrderService.findById(id, CommonUtils.getMyId(), 0);
        if (io == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "订单不存在！", new JSONObject());
        }
        if (io.getOrdersState() != 0) {
            io.setOrdersState(io.getUserId() == CommonUtils.getMyId() ? (io.getOrdersState() == 1 ? 3 : 2) : (io.getOrdersState() == 2 ? 3 : 1));
        } else {
            io.setOrdersState(io.getUserId() == CommonUtils.getMyId() ? 2 : 1);
        }
        io.setUpdateCategory(0);
        travelOrderService.updateOrders(io);
        //清除缓存中的商家订单信息
        redisUtils.expire(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + io.getMyId() + "_" + io.getNo(), 0);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 更改接单状态
     * @param id  订单Id
     * @return
     */
    @Override
    public ReturnData receivingBusiness(@PathVariable long id) {
        DoorwayBusinessOrder io = travelOrderService.findById(id, CommonUtils.getMyId(), 1);
        if (io == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "订单不存在！", new JSONObject());
        }
        //由未接单改为待配送
        io.setOrdersType(1);
        io.setOrderTime(new Date());
        io.setUpdateCategory(2);
        travelOrderService.updateOrders(io);
        //清除缓存中的商家订单信息
        redisUtils.expire(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + CommonUtils.getMyId() + "_" + io.getNo(), 0);
        //商家订单放入缓存
        Map<String, Object> ordersMap = CommonUtils.objectToMap(io);
        redisUtils.hmset(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + CommonUtils.getMyId() + "_" + io.getNo(), ordersMap, 0);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 更改配送状态
     * @param id  订单Id
     * @return
     */
    @Override
    public ReturnData distributionBusiness(@PathVariable long id) {
        DoorwayBusinessOrder io = travelOrderService.findById(id, CommonUtils.getMyId(), 2);
        if (io == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "订单不存在！", new JSONObject());
        }
        //由待配送改为配送中
        io.setOrdersType(2);        //配送中
        io.setDeliveryTime(new Date());
        io.setUpdateCategory(2);
        travelOrderService.updateOrders(io);
        //清除缓存中的商家订单信息
        redisUtils.expire(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + CommonUtils.getMyId() + "_" + io.getNo(), 0);
        //商家订单放入缓存
        Map<String, Object> ordersMap = CommonUtils.objectToMap(io);
        redisUtils.hmset(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + CommonUtils.getMyId() + "_" + io.getNo(), ordersMap, 0);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 更改验票状态
     * @param id  订单Id
     * @param voucherCode  凭证码
     * @return
     */
    @Override
    public ReturnData receiptBusiness(@PathVariable long id, @PathVariable String voucherCode) {
        DoorwayBusinessOrder io = travelOrderService.findById(id, CommonUtils.getMyId(), 3);
        if (io == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "订单不存在！", new JSONObject());
        }
        if (io.getPaymentStatus() == 0) {//未付款
            return returnData(StatusCode.CODE_TRAVEL_NOPAYMENT.CODE_VALUE, "您的订单尚未支付，请尽快支付再扫码", io);
        }
        if (io.getVerificationType() == 1) {//防止多次验票成功后多次打款
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = dateFormat.format(io.getInspectTicketTime());
            return returnData(StatusCode.CODE_TRAVEL_REPEAT.CODE_VALUE, "您已于" + time + "扫码成功", io);
        }
        if (io.getVerificationType() == 0) {
            if (io.getUserId() != CommonUtils.getMyId()) {
                return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "您无权限核验", io);
            }
            if (!io.getVoucherCode().equals(voucherCode)) {
                return returnData(StatusCode.CODE_PHARMACY_INVALID.CODE_VALUE, "凭证码无效", io);
            }
            //由未验票改为已验票
            io.setCompleteTime(new Date());
            io.setInspectTicketTime(new Date());
            io.setVerificationType(1);
//            io.setOrdersType(3);
            io.setUpdateCategory(6);
            travelOrderService.updateOrders(io);
            Map<String, Object> ordersMap = CommonUtils.objectToMap(io);
            //商家入账
            mqUtils.sendPurseMQ(io.getUserId(), 43, 0, io.getMoney());
            //清除缓存中的商家 订单信息
            redisUtils.expire(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + io.getMyId() + "_" + io.getNo(), 0);
            //商家订单放入缓存
            redisUtils.hmset(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + io.getMyId() + "_" + io.getNo(), ordersMap, Constants.USER_TIME_OUT);
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", io);
    }

    /***
     * 完成订单
     * @param id  订单Id
     * @return
     */
    @Override
    public ReturnData completeBusiness(@PathVariable long id) {
        DoorwayBusinessOrder io = travelOrderService.findById(id, CommonUtils.getMyId(), 4);
        if (io == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "订单不存在！", new JSONObject());
        }
        //由已验票改为已完成
        io.setOrdersType(2);        //已完成
        io.setCompleteTime(new Date());
        io.setUpdateCategory(3);
        travelOrderService.updateOrders(io);
        //清除缓存中的商家订单信息
        redisUtils.expire(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + CommonUtils.getMyId() + "_" + io.getNo(), 0);
        //商家订单放入缓存
        Map<String, Object> ordersMap = CommonUtils.objectToMap(io);
        redisUtils.hmset(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + CommonUtils.getMyId() + "_" + io.getNo(), ordersMap, 0);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 查看订单详情
     * @param no  订单编号
     * @return
     */
    @Override
    public ReturnData findBusinessOrder(@PathVariable String no) {
        //查询缓存 缓存中不存在 查询数据库
        DoorwayBusinessOrder io = null;
        Map<String, Object> ordersMap = redisUtils.hmget(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + CommonUtils.getMyId() + "_" + no);
        if (ordersMap == null || ordersMap.size() <= 0) {
            io = travelOrderService.findNo(no);
            if (io == null) {
                return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "您要查看的订单不存在", new JSONObject());
            }
            UserInfo userInfo = null;
            userInfo = userInfoUtils.getUserInfo(io.getUserId() == CommonUtils.getMyId() ? io.getUserId() : io.getMyId());
            if (userInfo != null) {
                io.setName(userInfo.getName());
                io.setHead(userInfo.getHead());
                io.setProTypeId(userInfo.getProType());
                io.setHouseNumber(userInfo.getHouseNumber());
            }
            //放入缓存
            ordersMap = CommonUtils.objectToMap(io);
            redisUtils.hmset(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + io.getMyId() + "_" + no, ordersMap, Constants.USER_TIME_OUT);
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", ordersMap);
    }

    /***
     * 取消订单（更新订单类型）
     * @param id
     * @return
     */
    @Override
    public ReturnData cancelBusinessOrders(@PathVariable long id) {
        DoorwayBusinessOrder ko = null;
        ko = travelOrderService.findById(id, CommonUtils.getMyId(), 5);
        if (ko == null) {
            return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE, "您要查看的订单不存在", new JSONObject());
        }
        //商家取消订单
        if (ko.getUserId() == CommonUtils.getMyId()) {
            if (ko.getVerificationType() == 0) {//可以取消用户未验票状态的单子
                ko.setVerificationType(3);
            }
        }
        if (ko.getMyId() == CommonUtils.getMyId()) {
            if (ko.getVerificationType() == 0) {//用户可以取消商家未验票状态的单子
                ko.setVerificationType(4);
            }
        }
        ko.setUpdateCategory(5);
        travelOrderService.updateOrders(ko);//更新订单
        if (ko.getVerificationType() == 3 || ko.getVerificationType() == 4) {
            if (ko.getPaymentStatus() == 1) {
                //更新缓存、钱包、账单
                if (ko.getMoney() > 0) {
                    mqUtils.sendPurseMQ(ko.getMyId(), 43, 0, ko.getMoney());
                }
            }
            //清除缓存中的商家 订单信息
            redisUtils.expire(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + ko.getMyId() + "_" + ko.getNo(), 0);
            //放入缓存
            Map<String, Object> ordersMap = CommonUtils.objectToMap(ko);
            redisUtils.hmset(Constants.REDIS_KEY_DOORWAYBUSINESSORDERS + ko.getMyId() + "_" + ko.getNo(), ordersMap, Constants.USER_TIME_OUT);
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 订单管理条件查询
     * @param userId
     * @param identity     身份区分：1买家 2商家
     * @param ordersType   订单类型: 订单类型:  0未付款（已下单未付款）1未验票(已付款未验票),2已验票,3已完成  4卖家取消订单 5用户取消订单
     * @param page         当前查询数据的页码
     * @param count        每页的显示条数
     * @return
     */
    @Override
    public ReturnData findBusinessOrderList(@PathVariable long userId, @PathVariable int identity, @PathVariable int ordersType, @PathVariable int page, @PathVariable int count) {
        //验证参数
        if (page < 0 || count <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "分页参数有误", new JSONObject());
        }
        //开始查询
        PageBean<DoorwayBusinessOrder> pageBean;
        pageBean = travelOrderService.findOrderList(identity, userId, ordersType, page, count);
        if (pageBean == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, StatusCode.CODE_SUCCESS.CODE_DESC, new JSONArray());
        }
        List list = null;
        list = pageBean.getList();
        DoorwayBusinessOrder t = null;
        UserInfo userCache = null;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                t = (DoorwayBusinessOrder) list.get(i);
                if (t != null) {
                    if (identity == 1) {
                        userCache = userInfoUtils.getUserInfo(t.getUserId());
                    } else {
                        userCache = userInfoUtils.getUserInfo(t.getMyId());
                    }
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
}
