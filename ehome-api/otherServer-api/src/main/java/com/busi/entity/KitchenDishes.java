package com.busi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import java.util.Date;

/**
 * @program: ehome
 * @description: 厨房菜品实体
 * @author: ZHaoJiaJie
 * @create: 2019-03-01 10:20
 */
@Setter
@Getter
public class KitchenDishes {

    private long id;                    // 主键

    @Min(value = 1, message = "userId参数有误")
    private long userId;				// 用户

    private long kitchenId;				// 厨房ID

    private long sales;					//销量

    private long pointNumber;			//点赞数

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;				// 添加时间

    private int deleteType;				 // 删除标志:0未删除,1用户删除,2管理员删除

    private int cuisine; 					//菜系：鲁菜、川菜、粤菜、苏菜、闽菜、浙菜、湘菜、徽菜、其他

    private double cost; 				// 价格

    private String dishame; 			// 菜名

    private String ingredients; 		// 配料

    private String imgUrl;			// 图片

    private long sortId;			// 分类ID

    private int bookedState;            //   0厨房  1订座

    //与数据库无关字段
    private String delImgUrls;//将要删除的图片地址组合 “,”分隔

}
