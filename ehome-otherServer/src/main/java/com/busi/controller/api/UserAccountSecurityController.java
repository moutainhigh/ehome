package com.busi.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.busi.controller.BaseController;
import com.busi.entity.ReturnData;
import com.busi.entity.UserAccountSecurity;
import com.busi.service.UserAccountSecurityService;
import com.busi.utils.CommonUtils;
import com.busi.utils.Constants;
import com.busi.utils.RedisUtils;
import com.busi.utils.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户账户安全接口
 * author：SunTianJie
 * create time：2018/9/17 15:07
 */
@RestController
public class UserAccountSecurityController extends BaseController implements UserAccountSecurityApiController {

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    UserAccountSecurityService userAccountSecurityService;

    /***
     * 查询安全中心数据接口
     * @param userId
     * @return
     */
    @Override
    public ReturnData findUserAccountSecurity(@PathVariable long userId) {
        if(userId<=0){//参数有误
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE,"userId参数有误",new JSONObject());
        }
        Map<String,Object> map = redisUtils.hmget(Constants.REDIS_KEY_USER_ACCOUNT_SECURITY+userId);
        if(map==null||map.size()<=0){
            UserAccountSecurity userAccountSecurity = userAccountSecurityService.findUserAccountSecurityByUserId(userId);
            if(userAccountSecurity==null){
                //之前该用户未设置过安全中心数据
                userAccountSecurity = new UserAccountSecurity();
                userAccountSecurity.setUserId(userId);
            }else{
                userAccountSecurity.setRedisStatus(1);//数据库中已有记录
            }
            //放到缓存中
            map = CommonUtils.objectToMap(userAccountSecurity);
            redisUtils.hmset(Constants.REDIS_KEY_USER_ACCOUNT_SECURITY+userId,map,Constants.USER_TIME_OUT);
        }
        UserAccountSecurity userAccountSecurity = (UserAccountSecurity) CommonUtils.mapToObject(map,UserAccountSecurity.class);
        if(userAccountSecurity==null){
            return returnData(StatusCode.CODE_SERVER_ERROR.CODE_VALUE,"账号有误，请重新登录后，再进行此操作",new JSONObject());
        }
        String bindPhone = "";// 绑定的手机号码
        String bindEmail = "";// 绑定的邮箱
        int securityQuestionStatus = 0;// 0密保问题未设置 1已设置
        int realNameStatus = 0;// 0实名未认证 1已认证
        int otherPlatformType = 0;//是否绑定第三方平台账号，0：未绑定, 1：绑定QQ账号，2：绑定微信账号，3：绑定新浪微博账号
        String otherPlatformAccount = "";//第三方平台账号名称
        int grade = 1;//安全等级 1为危险  2为一般 3为比较安全 4为安全 5非常安全
        int score = 0;//安全分数 安全等级根据分数计算获得 0分为危险 30分以下为一般  40-50分为比较安全 60以上为安全 100分为非常安全
        if(userAccountSecurity!=null){
            if(!CommonUtils.checkFull(userAccountSecurity.getPhone())){
                bindPhone = userAccountSecurity.getPhone();
                score += 30;
            }
            if(!CommonUtils.checkFull(userAccountSecurity.getEmail())){
                bindEmail = userAccountSecurity.getEmail();
                score += 15;
            }
            if(!CommonUtils.checkFull(userAccountSecurity.getSecurityQuestion())){
                securityQuestionStatus = 1;//已设置密保问题
                score += 15;
            }
            if(!CommonUtils.checkFull(userAccountSecurity.getIdCard())
                    &&!CommonUtils.checkFull(userAccountSecurity.getRealName())){
                realNameStatus = 1;//已认证
                score += 30;
            }
            if(userAccountSecurity.getOtherPlatformType()>0){//已绑定第三方平台账号
                otherPlatformType = userAccountSecurity.getOtherPlatformType();
                otherPlatformAccount = userAccountSecurity.getOtherPlatformAccount();
                score += 10;
            }
        }
        //开始计算安全等级
        if(score<=0){
            grade = 1;
        }else if(score>0&&score<=30){
            grade = 2;
        }else if(score>30&&score<60){
            grade = 3;
        }else if(score>=60&&score<100){
            grade = 4;
        }else if(score>=100){
            grade = 5;
        }
        Map<String, Object> userAccountSecurityMap = new HashMap<>();
        userAccountSecurityMap.put("grade", grade);
        userAccountSecurityMap.put("bindPhone", bindPhone);
        userAccountSecurityMap.put("bindEmail", bindEmail);
        userAccountSecurityMap.put("securityQuestionStatus", securityQuestionStatus);
        userAccountSecurityMap.put("realNameStatus", realNameStatus);
        userAccountSecurityMap.put("otherPlatformType", otherPlatformType);
        userAccountSecurityMap.put("otherPlatformAccount", otherPlatformAccount);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", userAccountSecurityMap);
    }

    /***
     * 绑定手机前，验证新手机号是否被占用接口
     * @param phone
     * @return
     */
    @Override
    public ReturnData checkNewPhone(@PathVariable String phone) {
        //验证参数
        if(!CommonUtils.checkPhone(phone)){
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE,"phone参数有误",new JSONObject());
        }
        UserAccountSecurity userAccountSecurity = userAccountSecurityService.findUserAccountSecurityByPhone(phone);
        int isTrue = 0;//是否正确，0表示占用，1表示可用
        if(userAccountSecurity==null){
            isTrue = 1;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("isTrue", isTrue);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", map);
    }

    /***
     * 绑定手机号接口
     * @param userAccountSecurity
     * @return
     */
    @Override
    public ReturnData bindNewPhone(@Valid @RequestBody UserAccountSecurity userAccountSecurity, BindingResult bindingResult) {
        //验证参数格式
        if(bindingResult.hasErrors()){
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE,checkParams(bindingResult),new JSONObject());
        }
        //验证修改人权限
        if(CommonUtils.getMyId()!=userAccountSecurity.getUserId()){
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE,"参数有误，当前用户["+CommonUtils.getMyId()+"]无权限操作用户["+userAccountSecurity.getUserId()+"]的安全中心信息",new JSONObject());
        }
        //验证验证码是否正确
        Object serverCode = redisUtils.getKey(Constants.REDIS_KEY_USER_ACCOUNT_SECURITY_BIND_CODE+userAccountSecurity.getUserId()+"_"+userAccountSecurity.getPhone());
        if(serverCode==null){
            return returnData(StatusCode.CODE_ACCOUNTSECURITY_CHECK_ERROR.CODE_VALUE,"该验证码已过期,请重新获取",new JSONObject());
        }
        if(!serverCode.toString().equals(userAccountSecurity.getCode())){//不相等
            return returnData(StatusCode.CODE_ACCOUNTSECURITY_CHECK_ERROR.CODE_VALUE,"您输入的验证码有误,请重新输入",new JSONObject());
        }
        //验证该手机是否被绑定过
        UserAccountSecurity uas = userAccountSecurityService.findUserAccountSecurityByPhone(userAccountSecurity.getPhone());
        if(uas!=null){//已存在
            return returnData(StatusCode.CODE_ACCOUNTSECURITY_CHECK_ERROR.CODE_VALUE,"该手机号已被其他账户绑定，请更换其他的手机号再进行绑定",new JSONObject());
        }
        //判断该账户是否未绑定手机号
        Map<String,Object> userAccountSecurityMap = redisUtils.hmget(Constants.REDIS_KEY_USER_ACCOUNT_SECURITY+userAccountSecurity.getUserId());
        if(userAccountSecurityMap==null||userAccountSecurityMap.size()<=0){
            UserAccountSecurity uass = userAccountSecurityService.findUserAccountSecurityByUserId(userAccountSecurity.getUserId());
            if(uass==null){
                //之前该用户未设置过安全中心数据 新增
                userAccountSecurityService.addUserAccountSecurity(userAccountSecurity);
            }else{//更新
                userAccountSecurityService.updateUserAccountSecurity(userAccountSecurity);
            }
        }else{
            if(Integer.parseInt(userAccountSecurityMap.get("redisStatus").toString())==0){//redisStatus==0 说明数据中无此记录
                //之前该用户未设置过权限信息 新增
                userAccountSecurityService.addUserAccountSecurity(userAccountSecurity);
            }else{//更新
                userAccountSecurityService.updateUserAccountSecurity(userAccountSecurity);
            }
        }
        //清除安全中心缓存
        redisUtils.expire(Constants.REDIS_KEY_USER_ACCOUNT_SECURITY+userAccountSecurity.getUserId(),0);
        //清除短信验证码
        redisUtils.expire(Constants.REDIS_KEY_USER_ACCOUNT_SECURITY_BIND_CODE+userAccountSecurity.getUserId()+"_"+userAccountSecurity.getPhone(),0);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE,"success",new JSONObject());
    }

    /***
     * 解绑手机号
     * @param userAccountSecurity
     * @return
     */
    @Override
    public ReturnData unBindPhone(@Valid @RequestBody UserAccountSecurity userAccountSecurity, BindingResult bindingResult) {
        //验证参数格式
        if(bindingResult.hasErrors()){
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE,checkParams(bindingResult),new JSONObject());
        }
        //验证修改人权限
        if(CommonUtils.getMyId()!=userAccountSecurity.getUserId()){
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE,"参数有误，当前用户["+CommonUtils.getMyId()+"]无权限操作用户["+userAccountSecurity.getUserId()+"]的安全中心信息",new JSONObject());
        }
        //验证验证码是否正确
        Object serverCode = redisUtils.getKey(Constants.REDIS_KEY_USER_ACCOUNT_SECURITY_UNBIND_CODE+userAccountSecurity.getUserId()+"_"+userAccountSecurity.getPhone());
        if(serverCode==null){
            return returnData(StatusCode.CODE_ACCOUNTSECURITY_CHECK_ERROR.CODE_VALUE,"该验证码已过期,请重新获取",new JSONObject());
        }
        if(!serverCode.toString().equals(userAccountSecurity.getCode())){//不相等
            return returnData(StatusCode.CODE_ACCOUNTSECURITY_CHECK_ERROR.CODE_VALUE,"您输入的验证码有误,请重新输入",new JSONObject());
        }
        //判断该账户绑定手机号情况
        Map<String,Object> userAccountSecurityMap = redisUtils.hmget(Constants.REDIS_KEY_USER_ACCOUNT_SECURITY+userAccountSecurity.getUserId());
        if(userAccountSecurityMap==null||userAccountSecurityMap.size()<=0){
            UserAccountSecurity uass = userAccountSecurityService.findUserAccountSecurityByUserId(userAccountSecurity.getUserId());
            if(uass==null){
                return returnData(StatusCode.CODE_ACCOUNTSECURITY_CHECK_ERROR.CODE_VALUE,"该账号未绑定过手机，无法解绑",new JSONObject());
            }else{
                if(!uass.getPhone().equals(userAccountSecurity.getPhone())){
                    return returnData(StatusCode.CODE_ACCOUNTSECURITY_CHECK_ERROR.CODE_VALUE,"解绑手机号不正确，解绑失败",new JSONObject());
                }
            }
        }else{
            if(Integer.parseInt(userAccountSecurityMap.get("redisStatus").toString())==0){//redisStatus==0 说明数据中无此记录
                return returnData(StatusCode.CODE_ACCOUNTSECURITY_CHECK_ERROR.CODE_VALUE,"该账号未绑定过手机，无法解绑",new JSONObject());
            }else{
                if(!userAccountSecurityMap.get("phone").toString().equals(userAccountSecurity.getPhone())){
                    return returnData(StatusCode.CODE_ACCOUNTSECURITY_CHECK_ERROR.CODE_VALUE,"解绑手机号不正确，解绑失败",new JSONObject());
                }
            }
        }
        //开始解绑 更新数据库
        userAccountSecurity.setPhone("");
        userAccountSecurityService.updateUserAccountSecurity(userAccountSecurity);
        //清除安全中心缓存
        redisUtils.expire(Constants.REDIS_KEY_USER_ACCOUNT_SECURITY+userAccountSecurity.getUserId(),0);
        //清除短信验证码
        redisUtils.expire(Constants.REDIS_KEY_USER_ACCOUNT_SECURITY_UNBIND_CODE+userAccountSecurity.getUserId()+"_"+userAccountSecurity.getPhone(),0);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE,"success",new JSONObject());
    }

}
