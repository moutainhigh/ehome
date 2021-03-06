package com.busi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * @program: ehome
 * @description: 订座实体
 * @author: ZHaoJiaJie
 * @create: 2019-07-31 14:06
 */
@Setter
@Getter
public class KitchenReserve {

    private long id;                    // 厨房ID

    @Min(value = 1, message = "userId参数有误")
    private long userId;                // 商家ID

    private int businessStatus;        // 营业状态:0正常 1暂停

    private String claimId;        // 认领数据唯一标识符

    private int claimStatus;        // 认领状态:0待认领 1已认领

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date claimTime;        // 认领时间

    private int deleteType;                 // 删除标志:0未删除,1用户删除,2管理员删除

    private int auditType;            // 审核标志:0审核中,1通过,2未通过 3已被其他用户入驻

    private String cuisine;            //菜系 [格式：川菜，粤菜（逗号分隔）]  菜系内容：鲁菜、川菜、粤菜、苏菜、闽菜、浙菜、湘菜、徽菜、其他

    @Length(max = 13, message = "拿手菜不能超过13字")
    private String goodFood;            //拿手菜

    @Length(max = 14, message = "厨房名称不能超过14字")
    private String kitchenName;                //厨房名称

    private int startingTime;                // 平均等候上菜时长

    private int merchantsType;                // 商户类型:0餐馆、1酒吧、2KTV、3茶馆、4咖啡厅、5其他，默认餐馆

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;            // 添加时间

    private String healthyCard;        //营业执照（原健康证）

    private String kitchenCover;        //封面

    private String videoUrl;        //视频地址

    private String videoCoverUrl;     //视频封面地址

    @Length(max = 300, message = "简介不能超过300字")
    private String content;                //简介

    private long totalSales;        // 总销量

    private long totalScore;        // 总评分

    private int averageScore;        // 平均评分

    private double lat;                    //纬度

    private double lon;                    //经度

    @Length(max = 46, message = "详细地址不能超过46字")
    private String address;            // 详细地址

    @Pattern(regexp = "[\\d\\w\\u4e00-\\u9fa5,\\.;\\:\"'?!\\-]{2,30}", message = "名字格式有误，长度为2-10，并且不能包含非法字符")
    private String realName;//店主姓名

    @Pattern(regexp = "^\\s*$|^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$", message = "手机号格式有误，请输入正确的手机号")
    private String phone;//店主电话

    private String orderingPhone;//订餐电话

    private String invitationCode;//邀请码

    //与数据库无关字段
    private String delImgUrls;//将要删除的图片地址组合 “,”分隔

    private int age;         //年龄

    private int sex;        // 性别:1男,2女

    private int distance;            //距离

    private String name;                //用户名

    private String head;                    //头像

    private int proTypeId;                //	省简称ID

    private long houseNumber;        // 门牌号

}
