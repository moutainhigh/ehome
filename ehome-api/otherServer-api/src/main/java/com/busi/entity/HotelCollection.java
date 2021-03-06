package com.busi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import java.util.Date;

/**
 * @program: 酒店民宿收藏
 * @author: ZHaoJiaJie
 * @create: 2020-08-03 14:47:33
 */
@Setter
@Getter
public class HotelCollection {
    private long id;        //主键ID

    private long hotelId;        //酒店ID

    @Min(value = 1, message = "myId参数有误")
    private long myId;        //用户ID

    private long userId;                //酒店民宿创建者ID

    private String name;                //酒店民宿名字

    private String picture;        //图片

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;        //收藏时间

    private String type;            // 类型

    private int levels;            // 级别

    private int hotelType;          // 0酒店 1民宿

    //与数据库无关字段
    private int distance;            //距离
}
