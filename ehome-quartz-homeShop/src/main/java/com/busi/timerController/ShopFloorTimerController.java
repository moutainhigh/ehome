package com.busi.timerController;

import com.busi.entity.ShopFloorOrders;
import com.busi.servive.ShopFloorOrdersService;
import com.busi.utils.Constants;
import com.busi.utils.MqUtils;
import com.busi.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * quartz定时器:搂店、礼尚往来、合伙购订单
 * author：ZHJJ
 * create time：2020-05-07 11:05:50
 */
@Slf4j
@Component
public class ShopFloorTimerController {

    @Autowired
    private MqUtils mqUtils;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    ShopFloorOrdersService shopFloorOrdersService;

    /**
     * Cron表达式的格式：秒 分 时 日 月 周 年(可选)。
     * <p>
     * “*”可用在所有字段中，表示对应时间域的每一个时刻，例如，*在分钟字段时，表示“每分钟”；
     * <p>
     * “?”字符：表示不确定的值 该字符只在日期和星期字段中使用，它通常指定为“无意义的值”，相当于点位符；
     * <p>
     * “,”字符：指定数个值 表达一个列表值，如在星期字段中使用“MON,WED,FRI”，则表示星期一，星期三和星期五；
     * <p>
     * “-”字符：指定一个值的范围 如在小时字段中使用“10-12”，则表示从10到12点，即10,11,12；
     * <p>
     * “/”字符：指定一个值的增加幅度。n/m表示从n开始，每次增加m
     * <p>
     * “L”字符：用在日表示一个月中的最后一天，用在周表示该月最后一个星期X
     * <p>
     * “W”字符：指定离给定日期最近的工作日(周一到周五)
     * <p>
     * “#”字符：表示该月第几个周X。6#3表示该月第3个周五
     *
     * @throws Exception
     */
    @Scheduled(cron = "0 11 11 * * ?") //
    public void shopFloorTimer() throws Exception {
        log.info("开始查询数据库中待处理的黑店超时订单，并加装到内存中...");
        while (true) {
            ShopFloorOrders order = null;
            List ipsOrderList = null;
            int countTime = Constants.TIME_OUT_MINUTE_45 * 1000;// 45分钟
            int countTime1 = Constants.TIME_OUT_MINUTE_60_24_1 * 7 * 1000;// 一周
            int countTime2 = Constants.TIME_OUT_MINUTE_60_24_1 * 14 * 1000;// 两周
            long nowTime = new Date().getTime();// 系统时间
            ipsOrderList = shopFloorOrdersService.findOrderList2();
            if (ipsOrderList != null && ipsOrderList.size() > 0) {
                for (int i = 0; i < ipsOrderList.size(); i++) {
                    order = (ShopFloorOrders) ipsOrderList.get(i);
                    if (order != null) {
                        long sendTime = order.getAddTime().getTime();// 下单时间
                        int ordersType = order.getOrdersType();
                        if (ordersType == 0) {
                            if (sendTime <= nowTime - countTime) {
                                order.setOrdersType(5);// 付款超时【未付款】
                                shopFloorOrdersService.cancelOrders(order);
                                //清除缓存中的黑店订单信息
                                redisUtils.expire(Constants.REDIS_KEY_SHOPFLOORORDERS + order.getBuyerId() + "_" + order.getNo(), 0);
                                log.info("更新了黑店订单[" + order.getId() + "]操作成功,状态为：付款超时！");
                            } else {
                                continue;
                            }
                        } else if (ordersType == 1) {
                            long paymentTime = order.getPaymentTime().getTime();// 付款时间
                            if (paymentTime <= nowTime - countTime1) {
                                order.setOrdersType(6);// 发货超时
                                shopFloorOrdersService.cancelOrders(order);
                                //清除缓存中的黑店订单信息
                                redisUtils.expire(Constants.REDIS_KEY_SHOPFLOORORDERS + order.getBuyerId() + "_" + order.getNo(), 0);
                                //更新买家缓存、钱包、账单
                                mqUtils.sendPurseMQ(order.getBuyerId(), 14, 0, order.getMoney());

                                log.info("更新了黑店订单[" + order.getId() + "]操作成功,状态为：发货超时！");
                            } else {
                                continue;
                            }
                        } else if (ordersType == 2) {
                            long deliverTime = order.getDeliveryTime().getTime();// 发货时间

                            if (deliverTime <= nowTime - countTime2) {
                                order.setOrdersType(3);// //更新订单为已收货
                                order.setReceivingTime(new Date());
                                shopFloorOrdersService.updateCollect(order);
                                //清除缓存中的黑店订单信息
                                redisUtils.expire(Constants.REDIS_KEY_SHOPFLOORORDERS + order.getBuyerId() + "_" + order.getNo(), 0);
                                //更新商家缓存、钱包、账单
//                                mqUtils.sendPurseMQ(order.getUserId(), 14, 0, order.getMoney());
                                log.info("更新了黑店订单[" + order.getId() + "]操作成功,状态为：收货超时！");
                            } else {
                                continue;
                            }
                        } else if (ordersType == 3) {//用户自己确认收货
                            order.setOrdersType(3);// //更新订单为已收货
                            order.setReceivingTime(new Date());
                            shopFloorOrdersService.updateCollect(order);
                            //清除缓存中的黑店订单信息
                            redisUtils.expire(Constants.REDIS_KEY_SHOPFLOORORDERS + order.getBuyerId() + "_" + order.getNo(), 0);
                            //更新商家缓存、钱包、账单
//                            mqUtils.sendPurseMQ(order.getUserId(), 14, 0, order.getMoney());
                            log.info("更新了黑店订单[" + order.getId() + "]操作成功,状态为：用户确认收货！");
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
    }
}
