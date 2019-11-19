package com.busi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

/**
 * @program: ehome
 * @description: 楼店实体
 * @author: ZHaoJiaJie
 * @create: 2019-11-12 16:14
 */
@Setter
@Getter
public class ShopFloor {

    private long id;                    //主键

    private long userId;                //用户

    @Length(max = 15, message = "店铺名称最多可输入15个字")
    private String shopName;                //店铺名称

    private String shopHead;            //店铺封面

    private String videoUrl;        //视频地址

    private String videoCoverUrl;     //视频封面地址

    @Length(max = 140, message = "店铺简介最多可输入140字")
    private String content;                //店铺简介

    private int deleteType;                //删除标志：0未删除，1用户删除，2管理人员删除

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;            //新增时间

    private int shopState;        //店铺状态  0未开店  1已开店

    private int payState;        //缴费状态  0未缴费  1已缴费

    private double lat;                    //纬度

    private double lon;                    //经度

    private String address;                // 详细地址

    private String villageName;                // 小区名称

    private String villageOnly;                // 小区唯一标识

    // 与数据库无关字段
//    private String name;                //用户名
//
//    private String head;                //头像
//
//    private int proTypeId;                //省简称ID
//
//    private long houseNumber;            //门牌号

    private String delImgUrls;      //将要删除的图片地址组合 “,”分隔

//    private int villageIs;            //是否存在  0没有  1有
}