package com.busi.controller.api;

import com.busi.entity.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @program: ehome
 * @description: 酒店景区设置相关接口
 * @author: ZHaoJiaJie
 * @create: 2020-08-13 12:57:13
 */
public interface HotelTourismApiController {

    /***
     * 新增订座信息
     * @param kitchenBooked
     * @return
     */
    @PostMapping("addHotelTourismBooked")
    ReturnData addHotelTourismBooked(@Valid @RequestBody KitchenReserveBooked kitchenBooked, BindingResult bindingResult);

    /***
     * 查看订座设置详情
     * @param userId  商家ID
     * @param type  0酒店 1景区
     * @return
     */
    @GetMapping("findHotelTourismBooked/{userId}/{type}")
    ReturnData findHotelTourismBooked(@PathVariable long userId, @PathVariable int type);

    /***
     * 编辑订座设置
     * @param kitchenBooked
     * @return
     */
    @PutMapping("changeHotelTourismBooked")
    ReturnData changeHotelTourismBooked(@Valid @RequestBody KitchenReserveBooked kitchenBooked, BindingResult bindingResult);

    /***
     * 新增包间or大厅信息
     * @param kitchenHotelTourismRoom
     * @return
     */
    @PostMapping("addHotelTourismRoom")
    ReturnData addHotelTourismRoom(@Valid @RequestBody KitchenReserveRoom kitchenHotelTourismRoom, BindingResult bindingResult);

    /***
     * 查询包间or大厅信息
     * @param id
     * @return
     */
    @GetMapping("findHotelTourismRoom/{id}")
    ReturnData findHotelTourismRoom(@PathVariable long id);

    /***
     * 查看包间or大厅列表
     * @param type  0酒店 1景区
     * @param eatTime  就餐时间
     * @param userId  商家ID
     * @param bookedType  包间0  散桌1
     * @param page     页码
     * @param count    条数
     * @return
     */
    @GetMapping("findHotelTourismRoomList/{type}/{eatTime}/{userId}/{bookedType}/{page}/{count}")
    ReturnData findHotelTourismRoomList(@PathVariable int type, @PathVariable String eatTime, @PathVariable long userId, @PathVariable int bookedType, @PathVariable int page, @PathVariable int count);

    /***
     * 编辑包间or大厅信息
     * @param kitchenHotelTourismRoom
     * @return
     */
    @PutMapping("changeHotelTourismRoom")
    ReturnData changeHotelTourismRoom(@Valid @RequestBody KitchenReserveRoom kitchenHotelTourismRoom, BindingResult bindingResult);

    /**
     * @Description: 删除包间or大厅
     * @return:
     */
    @DeleteMapping("delHotelTourismRoom/{ids}")
    ReturnData delHotelTourismRoom(@PathVariable String ids);

    /***
     * 新增上菜时间
     * @param kitchenServingTime
     * @return
     */
    @PostMapping("addHotelTourismTime")
    ReturnData addHotelTourismTime(@Valid @RequestBody KitchenReserveServingTime kitchenServingTime, BindingResult bindingResult);

    /***
     * 更新上菜时间
     * @param kitchenServingTime
     * @return
     */
    @PutMapping("updateHotelTourismTime")
    ReturnData updateHotelTourismTime(@Valid @RequestBody KitchenReserveServingTime kitchenServingTime, BindingResult bindingResult);

    /***
     * 查询上菜时间列表
     * @param type  0酒店 1景区
     * @param kitchenId   酒店景区ID
     * @return
     */
    @GetMapping("findHotelTourismTime/{type}/{kitchenId}")
    ReturnData findHotelTourismTime(@PathVariable int type, @PathVariable long kitchenId);

}