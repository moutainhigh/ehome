package com.busi.entity;

import com.busi.validator.IdCardConstraint;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.util.Date;

/** 
 * 用户实名信息实体类
 * 
 * 一个身份证可以帮到多个账号，目前暂不能修改已绑定的身份证
 * 
 * @author SunTianJie 
 *
 * @version create time：2017-6-5 下午1:35:45 
 * 
 */
@Setter
@Getter
public class RealNameInfo {
	
	private long id;//主键ID
	
	private long userId;//用户ID

	@NotNull
	@Pattern(regexp="[\\d\\w\\u4e00-\\u9fa5,\\.;\\:\"'?!\\-]{2,30}",message = "名字格式有误，长度为2-30，并且不能包含非法字符")
	private String realName;//用户真实姓名

	@IdCardConstraint(message = "身份证格式有误")
	private String cardNo;//用户身份证号
	
	private String addrCode;//用户区号
	
	private String birth;//用户生日
	
	private int sex;//用户性别 1男2女
	
	private int length;//身份证长度
	
	private String checkBit;//身份证最后一位
	
	private String addr;//详细地址
	
	private String province;//省
	
	private String city;//市
	
	private String area;//区县

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	private Date time;//入库时间 实名认证时间


}
