package com.busi.dao;

import com.busi.entity.ShopFloorMasterOrders;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: ehome
 * @description: 楼店店主订单
 * @author: ZHaoJiaJie
 * @create: 2020-1-9 15:52:17
 */
@Mapper
@Repository
public interface ShopFloorMasterOrdersDao {

    /***
     * 新增厨房订座订单
     * @param kitchenBookedOrders
     * @return
     */
    @Insert("insert into ShopFloorMasterOrders(buyerId,shopId,goods,money,addTime,paymentTime,deliveryTime,receivingTime,no,ordersState,ordersType," +
            "addressId,distributioMode,shopName,remarks,address,addressName,addressPhone,addressProvince,addressCity,addressDistrict)" +
            "values (#{buyerId},#{shopId},#{goods},#{money},#{addTime},#{paymentTime},#{deliveryTime},#{receivingTime},#{no},#{ordersState},#{ordersType}" +
            ",#{addressId},#{distributioMode},#{shopName},#{remarks},#{address},#{addressName},#{addressPhone},#{addressProvince},#{addressCity},#{addressDistrict})")
    @Options(useGeneratedKeys = true)
    int addOrders(ShopFloorMasterOrders kitchenBookedOrders);

    /***
     * 根据用户ID查询订单
     * @param id
     * @param type  查询场景 0删除 1由未发货改为已发货 2由未收货改为已收货 3取消订单
     * @return
     */
    @Select("<script>" +
            "select * from ShopFloorMasterOrders" +
            " where id = #{id}" +
            "<if test=\"type == 0\">" +
            " and ordersType >2 and ordersState=0" +
            "</if>" +
            "<if test=\"type == 1\">" +
            " and ordersState=0  and ordersType=1" +
            "</if>" +
            "<if test=\"type == 2\">" +
            " and ordersState=0  and ordersType=2 and buyerId=#{userId}" +
            "</if>" +
            "<if test=\"type == 3\">" +
            " and ordersState=0 and ordersType &lt; 3 and buyerId=#{userId}" +
            "</if>" +
            "</script>")
    ShopFloorMasterOrders findById(@Param("id") long id, @Param("userId") long userId, @Param("type") int type);

    /***
     * 根据订单编号查询订单
     * @param no  订单编号
     * @return
     */
    @Select("<script>" +
            "select * from ShopFloorMasterOrders" +
            " where no = #{no}" +
            " and ordersState = 0" +
            "</script>")
    ShopFloorMasterOrders findByNo(@Param("no") String no);

    /***
     *  更新楼店订单状态
     *  updateCategory 更新类别  0删除状态  1由未发货改为已发货 2由未收货改为已收货 3取消订单 4更新支付状态
     * @param orders
     * @return
     */
    @Update("<script>" +
            "update ShopFloorMasterOrders set" +
            "<if test=\"updateCategory == 0\">" +
            " ordersState =1," +
            "</if>" +
            "<if test=\"updateCategory == 1\">" +
            " deliveryTime =#{deliveryTime}," +
            " ordersType =#{ordersType}," +
            "</if>" +
            "<if test=\"updateCategory == 2\">" +
            " ordersType =#{ordersType}," +
            " receivingTime =#{receivingTime}," +
            "</if>" +
            "<if test=\"updateCategory == 3\">" +
            " ordersType =#{ordersType}," +
            "</if>" +
            "<if test=\"updateCategory == 4\">" +
            " paymentTime =#{paymentTime}," +
            " ordersType =#{ordersType}," +
            "</if>" +
            " id=#{id} " +
            " where id=#{id} and ordersState=0" +
            "</script>")
    int updateOrders(ShopFloorMasterOrders orders);

    /***
     * 分页查询订单列表
     * @param ordersType 订单类型: -1全部 0待付款,1待发货(已付款),2已发货（待收货）, 3已收货（待评价）  4已评价  5付款超时、发货超时、买家取消订单、卖家取消订单
     * @return
     */
    @Select("<script>" +
            "select * from ShopFloorMasterOrders" +
            " where ordersState=0" +
            "<if test=\"userId > 0 \">" +
            " and buyerId = #{userId}" +
            "</if>" +
            "<if test=\"ordersType == -2\">" +
            " and ordersType > 0" +
//            " and ordersType in (1,2,3,4) " +
            "</if>" +
            "<if test=\"ordersType > 0 and ordersType &lt; 5\">" +
            " and ordersType = #{ordersType}" +
            "</if>" +
            "<if test=\"ordersType >= 5\">" +
            " and ordersType > 5" +
            "</if>" +
            " order by addTime desc" +
            "</script>")
    List<ShopFloorMasterOrders> findOrderList(@Param("userId") long userId, @Param("ordersType") int ordersType);

    /***
     * 统计各类订单数量
     * @return
     */
    @Select("<script>" +
            "select * from ShopFloorMasterOrders" +
            " where 1=1 " +
            " and buyerId = #{userId}" +
            " and ordersState = 0" +
            "</script>")
    List<ShopFloorMasterOrders> findIdentity(@Param("userId") long userId);


}
