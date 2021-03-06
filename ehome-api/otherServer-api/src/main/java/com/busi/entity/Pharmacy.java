package com.busi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import java.util.Date;

/**
 * @program: ehome
 * @description: 药房实体
 * @author: ZhaoJiaJie
 * @create: 2020-08-10 13:58:03
 */
@Setter
@Getter
public class Pharmacy {

    private long id;                    // 药房ID

    @Min(value = 1, message = "userId参数有误")
    private long userId;                // 商家ID

    private int businessStatus;        // 营业状态:0正常 1打烊中

    private int deleteType;            // 删除标志:0未删除,1用户删除,2管理员删除

    private int auditType;            // 审核标志:0审核中,1通过,2未通过 3已被其他用户入驻

    @Length(max = 14, message = "药房名称不能超过30字")
    private String pharmacyName;                //药房名称

    private int openType;                // 药房开放类型:0全天 1时间段

    private String openTime;                // 药房开放时间 openType=1时有效

    private String closeTime;                // 药房关闭时间 openType=1时有效

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;            // 入驻时间

    private String licence;        //药房证照

    private String picture;        //药房图片

    private String videoUrl;        //视频地址

    private String videoCoverUrl;     //视频封面地址

    @Length(max = 140, message = "药房贴士不能超过140字")
    private String tips;                //药房贴士

    @Length(max = 2000, message = "药房简介不能超过2000字")
    private String content;                //药房简介

    private long totalEvaluate;        // 总评价

    private long totalScore;        // 总评分

    private int averageScore;        // 平均评分

    @Min(value = 0, message = "province参数有误，超出指定范围")
    private int province; // 省

    @Min(value = 0, message = "city参数有误，超出指定范围")
    private int city; // 城市

    @Min(value = 0, message = "district参数有误，超出指定范围")
    private int district; // 地区或县

    @Digits(integer = 3, fraction = 6, message = "lat参数格式有误")
    private double lat;                    //纬度

    @Digits(integer = 3, fraction = 6, message = "lon参数格式有误")
    private double lon;                    //经度

    private String type;            // 药房类型

    private int levels;            // 药房级别

    //    @Pattern(regexp = "^\\s*$|^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$", message = "手机号格式有误，请输入正确的手机号")
    private String phone;//药房电话

    private double cost;                    //药品最低价格

    private int distributionMode;            // 配送方式 0免费配送  1满额配送

    private double initialCost;                    //起送价格

    private String address;            // 详细地址

    private String claimId;        // 认领数据唯一标识符

    private int claimStatus;        // 入驻状态:0待入驻 1已入驻

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date claimTime;        // 入驻时间

    private String invitationCode;//邀请码

    //与数据库无关字段
    private String delImgUrls;//将要删除的图片地址组合 “,”分隔

    private int distance;            //距离

    private String name;                //用户名

    private String head;                    //头像

    private int proTypeId;                //	省简称ID

    private long houseNumber;        // 门牌号
}
