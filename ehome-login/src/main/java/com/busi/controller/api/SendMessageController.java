package com.busi.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.busi.controller.BaseController;
import com.busi.entity.ReturnData;
import com.busi.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发送信息接口 例如发送短信验证码  发送邮件 发送消息等
 * author：SunTianJie
 * create time：2018/8/30 8:46
 */
@RestController
public class SendMessageController extends BaseController implements SendMessageApiController {

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    MqUtils mqUtils;

    /**
     * 发送手机短信
     * @param phone     将要发送短信的手机号
     * @param phoneType 短信类型 0注册验证码  1找回支付密码验证码 2安全中心绑定手机验证码 3安全中心解绑手机验证码
     *                            4手机短信找回登录密码验证码  5手机短信修改密码验证码 6短信邀请新用户注册 7...
     * 规则：每个账号每天最多发送短信100次 每小时最多30次
     *       每个设备每天最多发送短信200次 每小时最多60次
     * @return
     */
    @Override
    public ReturnData SendPhoneMessage(@PathVariable String phone,@PathVariable int phoneType) {
        //验证参数
        if(CommonUtils.checkFull(phone)||!CommonUtils.checkPhone(phone)){
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE,"phone参数有误",new JSONObject());
        }
        if(phoneType<1||phoneType>6){
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE,"phoneType参数有误，超出合法范围",new JSONObject());
        }
        //同一账号每小时限制
        String accountHourTotal = String.valueOf(redisUtils.hget(Constants.REDIS_KEY_ACCOUNT_HOUR_TOTAL,CommonUtils.getMyId()+""));
        if(!CommonUtils.checkFull(accountHourTotal)&&Integer.parseInt(accountHourTotal)>Constants.ACCOUNT_HOUR_TOTAL){
            return returnData(StatusCode.CODE_SMS_USEROVER_ERROR.CODE_VALUE,"您当前账号发送的短信次数过多,请一个小时后再试，如有疑问请联系官方客服",new JSONObject());
        }
        //同一账号每天时限制
        String accountDayTotal = String.valueOf(redisUtils.hget(Constants.REDIS_KEY_ACCOUNT_DAY_TOTAL,CommonUtils.getMyId()+""));
        if(!CommonUtils.checkFull(accountDayTotal)&&Integer.parseInt(accountDayTotal)>Constants.ACCOUNT_DAY_TOTAL){
            return returnData(StatusCode.CODE_SMS_USEROVER_ERROR.CODE_VALUE,"您当前账号发送的短信次数过多,系统已自动停用当前账号使用短信功能一天，如有疑问请联系官方客服",new JSONObject());
        }
        //同一客户端设备每小时限制
        String clientHourTotal = String.valueOf(redisUtils.hget(Constants.REDIS_KEY_CLIENT_HOUR_TOTAL,CommonUtils.getMyId()+""));
        if(!CommonUtils.checkFull(clientHourTotal)&&Integer.parseInt(clientHourTotal)>Constants.CLIENT_HOUR_TOTAL){
            return returnData(StatusCode.CODE_SMS_PHONEOVER_ERROR.CODE_VALUE,"您当前设备发送的短信次数过多,请一个小时后再试，如有疑问请联系官方客服",new JSONObject());
        }
        //同一客户端设备每天时限制
        String clientDayTotal = String.valueOf(redisUtils.hget(Constants.REDIS_KEY_CLIENT_DAY_TOTAL,CommonUtils.getMyId()+""));
        if(!CommonUtils.checkFull(clientDayTotal)&&Integer.parseInt(clientDayTotal)>Constants.ACCOUNT_DAY_TOTAL){
            return returnData(StatusCode.CODE_SMS_PHONEOVER_ERROR.CODE_VALUE,"您当前设备发送的短信次数过多,系统已自动停用当前账号使用短信功能一天，如有疑问请联系官方客服",new JSONObject());
        }
        //生成验证码
        String code = CommonUtils.getRandom(4,1);
        //根据业务不同将验证码存入缓存中
        switch (phoneType) {

            case 0://注册验证码（暂不使用，使用注册接口中的获取验证码）

                break;
            case 1://找回支付密码验证码
                redisUtils.set(Constants.REDIS_KEY_PAY_FIND_PAYPASSWORD_CODE+CommonUtils.getMyId(),code,60*10);//验证码10分钟内有效
                break;
            case 2://安全中心绑定手机验证码

                break;
            case 3://安全中心解绑手机验证码

                break;
            case 4://手机短信找回登录密码验证码

                break;
            case 5://手机短信修改密码验证码

                break;
            case 6://短信邀请新用户注册

                break;

            default:
                break;
        }
        //调用MQ进行发送短信
        mqUtils.sendPhoneMessage(phone,code,phoneType);
        //更新同一账号每小时限制
        if(CommonUtils.checkFull(accountHourTotal)){//第一次
            redisUtils.hset(Constants.REDIS_KEY_ACCOUNT_HOUR_TOTAL,CommonUtils.getMyId()+"",1,24*60*60);//设置1天后失效
        }else{
            redisUtils.hashIncr(Constants.REDIS_KEY_ACCOUNT_HOUR_TOTAL,CommonUtils.getMyId()+"",1);
        }
        //更新同一账号每天时限制
        if(CommonUtils.checkFull(accountDayTotal)){//第一次
            redisUtils.hset(Constants.REDIS_KEY_ACCOUNT_DAY_TOTAL,CommonUtils.getMyId()+"",1,24*60*60);//设置1天后失效
        }else{
            redisUtils.hashIncr(Constants.REDIS_KEY_ACCOUNT_DAY_TOTAL,CommonUtils.getMyId()+"",1);
        }
        //更新同一客户端设备每小时限制
        if(CommonUtils.checkFull(clientHourTotal)){//第一次
            redisUtils.hset(Constants.REDIS_KEY_CLIENT_HOUR_TOTAL,CommonUtils.getMyId()+"",1,24*60*60);//设置1天后失效
        }else{
            redisUtils.hashIncr(Constants.REDIS_KEY_CLIENT_HOUR_TOTAL,CommonUtils.getMyId()+"",1);
        }
        //更新同一客户端设备每天时限制
        if(CommonUtils.checkFull(clientDayTotal)){//第一次
            redisUtils.hset(Constants.REDIS_KEY_CLIENT_DAY_TOTAL,CommonUtils.getMyId()+"",1,24*60*60);//设置1天后失效
        }else{
            redisUtils.hashIncr(Constants.REDIS_KEY_CLIENT_DAY_TOTAL,CommonUtils.getMyId()+"",1);
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE,"success",new JSONObject());
    }
}
