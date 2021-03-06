package com.busi.controller.api;

import com.busi.entity.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @program: ehome
 * @description: 酒店民宿相关接口
 * @author: ZhaoJiaJie
 * @create: 2020-08-03 15:06:11
 */
public interface HotelApiController {

    /***
     * 新增酒店民宿
     * @param scenicSpot
     * @return
     */
    @PostMapping("addHotel")
    ReturnData addHotel(@Valid @RequestBody Hotel scenicSpot, BindingResult bindingResult);

    /***
     * 更新酒店民宿
     * @param scenicSpot
     * @return
     */
    @PutMapping("changeHotel")
    ReturnData changeHotel(@Valid @RequestBody Hotel scenicSpot, BindingResult bindingResult);

    /**
     * @Description: 删除酒店民宿
     * @return:
     */
    @DeleteMapping("delHotel/{userId}/{id}")
    ReturnData delHotel(@PathVariable long userId, @PathVariable long id);

    /***
     * 更新酒店民宿营业状态
     * @param scenicSpot
     * @return
     */
    @PutMapping("updHotelStatus")
    ReturnData updHotelStatus(@Valid @RequestBody Hotel scenicSpot, BindingResult bindingResult);

    /***
     * 查询酒店民宿信息
     * @param userId
     * @return
     */
    @GetMapping("findHotel/{userId}")
    ReturnData findHotel(@PathVariable long userId);

    /***
     * 查询酒店民宿信息
     * @param id
     * @return
     */
    @GetMapping("findHotelId/{id}")
    ReturnData findHotelId(@PathVariable long id);

    /***
     * 条件查询酒店民宿
     * @param watchVideos 筛选视频：0否 1是
     * @param hotelType 筛选：-1全部 0酒店 1民宿
     * @param name    模糊搜索
     * @param province     省
     * @param city      市
     * @param district    区
     * @param lat      纬度
     * @param lon      经度
     * @param page     页码
     * @param count    条数
     * @return
     */
    @GetMapping("findHotelList/{watchVideos}/{hotelType}/{name}/{province}/{city}/{district}/{lat}/{lon}/{page}/{count}")
    ReturnData findHotelList(@PathVariable int watchVideos, @PathVariable int hotelType, @PathVariable String name, @PathVariable int province, @PathVariable int city, @PathVariable int district, @PathVariable double lat, @PathVariable double lon, @PathVariable int page, @PathVariable int count);

    /***
     * 新增房间
     * @param tickets
     * @return
     */
    @PostMapping("addHotelRoom")
    ReturnData addHotelRoom(@Valid @RequestBody HotelRoom tickets, BindingResult bindingResult);

    /***
     * 更新房间
     * @param tickets
     * @return
     */
    @PutMapping("updateHotelRoom")
    ReturnData updateHotelRoom(@Valid @RequestBody HotelRoom tickets, BindingResult bindingResult);

    /**
     * @Description: 删除房间
     * @return:
     */
    @DeleteMapping("delHotelRoom/{ids}")
    ReturnData delHotelRoom(@PathVariable String ids);

    /***
     * 查询房间详情
     * @param id
     * @return
     */
    @GetMapping("findHotelRoom/{id}")
    ReturnData findHotelRoom(@PathVariable long id);


    /***
     * 分页查询房间列表
     * @param type   0酒店 1景区
     * @param id   酒店或景区ID
     * @param page     页码
     * @param count    条数
     * @return
     */
    @GetMapping("findHotelRoomList/{type}/{id}/{page}/{count}")
    ReturnData findHotelRoomList(@PathVariable int type, @PathVariable long id, @PathVariable int page, @PathVariable int count);

    /***
     * 新增收藏
     * @param hourlyWorkerCollection
     * @return
     */
    @PostMapping("addHotelCollect")
    ReturnData addHotelCollect(@Valid @RequestBody HotelCollection hourlyWorkerCollection, BindingResult bindingResult);

    /***
     * 分页查询收藏列表
     * @param userId   用户ID
     * @param lat      纬度
     * @param lon      经度
     * @param page     页码
     * @param count    条数
     * @return
     */
    @GetMapping("findHotelCollectList/{userId}/{lat}/{lon}/{page}/{count}")
    ReturnData findHotelCollectList(@PathVariable long userId, @PathVariable double lat, @PathVariable double lon, @PathVariable int page, @PathVariable int count);

    /**
     * @Description: 删除收藏
     * @return:
     */
    @DeleteMapping("delHotelCollect/{ids}")
    ReturnData delHotelCollect(@PathVariable String ids);

    /***
     * 新增酒店数据
     * @param hotelData
     * @return
     */
    @PostMapping("addHotelData")
    ReturnData addHotelData(@Valid @RequestBody HotelData hotelData, BindingResult bindingResult);

    /***
     * 查询酒店数据详情
     * @param id
     * @return
     */
    @GetMapping("findHotelData/{id}")
    ReturnData findHotelData(@PathVariable long id);

    /***
     * 认领酒店
     * @param hotel
     * @return
     */
    @PutMapping("claimHotel")
    ReturnData claimHotel(@Valid @RequestBody Hotel hotel, BindingResult bindingResult);

    /***
     * 查询酒店数据列表
     * @param hotelType    0酒店 1民宿
     * @param name    酒店名称
     * @param lat      纬度
     * @param lon      经度
     * @param page     页码
     * @param count    条数
     * @return
     */
    @GetMapping("findHotelDataList/{hotelType}/{name}/{lat}/{lon}/{page}/{count}")
    ReturnData findHotelDataList(@PathVariable int hotelType, @PathVariable String name, @PathVariable double lat, @PathVariable double lon, @PathVariable int page, @PathVariable int count);

}
