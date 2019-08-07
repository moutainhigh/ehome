package com.busi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

/**
 * @program: ehome
 * @description: 厨房订座订单实体
 * @author: ZHaoJiaJie
 * @create: 2019-06-26 16:10
 */
@Setter
@Getter
public class KitchenBookedOrders {

    private long id;                    // 主键

    private long myId;                // 买家ID

    private long userId;                // 卖家ID

    private long kitchenId;                //厨房ID

    private String no;                    //订单编号

    private String dishameCost;        //菜品ID,菜名,数量,价格【格式：12,回锅肉,5,24;木须肉,4,32】

    private int ordersState;        // 订单状态:0正常 1买家删除 2商家删除 3全部删除

    private int ordersType;            //订单类型: 0未付款（已下单未付款),1未接单(已付款未接单),2已接单,3已完成,4接单超时,5付款超时,6卖家取消订单,7用户取消订单 ,8已评价,9确认完成超时

    private String kitchenName;                //厨房名称

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;            // 添加时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paymentTime;            // 付款时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderTime;            // 接单时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date completeTime;            // 完成时间

    private double money;            // 商品金额

    private String smallMap;            //厨房头像【厨房封面】

    private int eatNumber;        //就餐人数

    private String eatDate;        //就餐日期

    private Date eatTime;        //就餐时间

    @Max(value = 1, message = "position参数有误，超出指定范围")
    @Min(value= 0 ,message= "position参数有误，超出指定范围")
    private int position;        //就餐位置  0大厅  1包间

    private String address_Name;            // 联系人姓名

    private String address_Phone;                // 联系人电话

    private String remarks;        //备注

    @Max(value = 1, message = "dispensing参数有误，超出指定范围")
    @Min(value= 0 ,message= "dispensing参数有误，超出指定范围")
    private int dispensing;        //是否接受位置调剂 0是 1否

    @Max(value = 1, message = "sex参数有误，超出指定范围")
    @Min(value= 0 ,message= "sex参数有误，超出指定范围")
    private int sex;        //性别 0男 1女

    //与数据库无关字段
    private String goodsIds;             //菜品ID

    private String foodNumber;           //菜品数量

    private String name;                //用户名

    private String head;                 //头像

    private int proTypeId;              //	省简称ID

    private long houseNumber;        // 门牌号

    private long updateCategory;        // 更新类别  默认0删除状态  1由未接单改为已接单  2已接单改为已完成  3取消订单、评价状态  4更新支付状态

}