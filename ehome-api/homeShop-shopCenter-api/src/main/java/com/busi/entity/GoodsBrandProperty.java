package com.busi.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @program: ehome
 * @description: 商品属性实体类
 * @author: ZHaoJiaJie
 * @create: 2019-10-31 10:55
 */
@Setter
@Getter
public class GoodsBrandProperty {

    private long id;//商品属性id

    private String name;//商品属性名称

    private long goodCategoryId;//商品分类id

    private long goodsBrandParamId;//商品品牌概括属性id

    private long goodsBrandId;//品牌id
}
