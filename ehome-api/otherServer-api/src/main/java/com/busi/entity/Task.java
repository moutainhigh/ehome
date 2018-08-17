package com.busi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import java.util.Date;

/**
 * @program: 任务实体类
 * @description:
 * @author: ZHaoJiaJie
 * @create: 2018-08-15 15:00
 */
@Setter
@Getter
public class Task {

    private long id;        //主键

    @Min(value = 1, message = "userId参数有误")
    private long userId;    //用户ID

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;        //完成时间

    private int taskStatus; //任务状态  0未完成 、 1未领奖、2已领奖

    private int taskType;    //任务类型：0、一次性任务   1 、每日任务

    private int sortTask;

	/*任务id：

	（每日做1次的）

	0、签到一次。
	1、发生活圈一次。
	2、转发一次其他用户的生活圈。
	3、发布需求汇一次。
	4、发布求助一次。
	5、打开活动页面，并为一个参加活动的用户投票一次。
	6、参与一次分享抢红包活动，并成功分享到微信朋友圈、微信好友或QQ好友。
	7、串门到一个用户家喂一次鹦鹉。
	8、串门到一个用户家后，成功玩一局互动游戏。
	9、打开记事本，添加一条记事或日程。
	10、打开钱包系统并任意充值一次。
	11、发送添加好友请求或成功添加一个好友。
	12、打开设置功能，上传或提交一次意见及反馈。
	13、创建新简历或编辑自己的简历一次。


	（只能做1次的）

	0、设置头像
	1、点击头像进入个人资料后编辑完善资料。
	2、任意到一个用户家串门。
	3、打开足迹功能，并自己的浏览足迹。
	4、到客厅点击脚印功能，看谁来过自己家串门。
	5、到客厅查看当前访客，并任意到一个访客家串门。
	6、到储藏室上传一张或多张照片。
	7、打开会员功能，看是否要成为元老级会员。
	8、进入资讯页面并切换查看今日人物、今日企业、今日新闻。
	9、打开电视功能并随意打开一个电视台链接。
	10、打开设置功能并任意查看设置中的一些功能。
	11、进入家园管家，或和家园管家进行一次对话。
	12、打开会客厅右上角陌生人按钮，查看陌生人打招呼内容。
	13、选择一个用户聊天。
	14、进入聊天广场后，任意进入一个地方聊天广场，并发布一次文字、图片或视频。
	15、打开云邻居功能，成功创建一个家人圈。
	16、进入自己的家人圈，给家人们发一条群聊信息，或问候其中一个家人。
	*/


}
