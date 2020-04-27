package com.busi.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.busi.controller.BaseController;
import com.busi.entity.*;
import com.busi.service.*;
import com.busi.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @program: 公告首页
 * @author: ZHaoJiaJie
 * @create: 2018-08-10 10:01
 */
@RestController
public class IPS_HomeController extends BaseController implements IPS_HomeApiController {

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    OtherPostsService otherPostsService;

    @Autowired
    LoveAndFriendsService loveAndFriendsService;

    @Autowired
    SearchGoodsService searchGoodsService;

    @Autowired
    private UserMembershipUtils userMembershipUtils;

    @Autowired
    UsedDealService usedDealService;

    @Autowired
    WorkResumeService workResumeService;

    @Autowired
    WorkRecruitService workRecruitService;

    @Autowired
    RentAhouseService rentAhouseService;

    /***
     * 查询接口
     * @param userId   用户ID
     * @return
     */
    @Override
    public ReturnData findHomeList(@PathVariable long userId) {
        //验证参数
        if (userId < 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "userId参数有误", new JSONObject());
        }
        int page = 1;
        int count = 50;
        UsedDeal usedDeal = null;
        PageBean<UsedDeal> dealPage = null;
        OtherPosts posts = null;
        PageBean<OtherPosts> otherPage = null;
        SearchGoods searchGoods = null;
        PageBean<SearchGoods> goodsPage = null;
        LoveAndFriends loveAndFriends = null;
        PageBean<LoveAndFriends> lovePage = null;
        RentAhouse rentAhouse = null;
        PageBean<RentAhouse> rentPage = null;
        List homeList = null;
        List<IPS_Home> ips = new ArrayList<>();
        homeList = redisUtils.getList(Constants.REDIS_KEY_IPS_HOMELIST, 0, 100);
        if (userId > 0 || homeList == null || homeList.size() <= 0) {
            //开始查询
            if (userId <= 0) {
                dealPage = usedDealService.findList(0, page, count);
                goodsPage = searchGoodsService.findList(0, page, count);
                lovePage = loveAndFriendsService.findHList(0, page, count);
                rentPage = rentAhouseService.findHList(0, page, count);
            } else {
                dealPage = usedDealService.findList(userId, page, count);
                otherPage = otherPostsService.findList(userId, page, count);
                goodsPage = searchGoodsService.findList(userId, page, count);
                lovePage = loveAndFriendsService.findHList(userId, page, count);
                rentPage = rentAhouseService.findHList(userId, page, count);
            }
            List loveList = lovePage.getList();
            List dealList = dealPage.getList();
            List otherList = null;
            if (otherPage != null) {
                otherList = otherPage.getList();
            }
            List goodsList = goodsPage.getList();
            List rentList = rentPage.getList();
            if (dealList != null && dealList.size() > 0) {
                for (int j = 0; j < dealList.size(); j++) {
                    usedDeal = (UsedDeal) dealList.get(j);
                    if (usedDeal != null) {
                        IPS_Home ipsHome = new IPS_Home();
                        ipsHome.setInfoId(usedDeal.getId());
                        ipsHome.setTitle(usedDeal.getTitle());
                        ipsHome.setUserId(usedDeal.getUserId());
                        ipsHome.setContent(usedDeal.getContent());
                        ipsHome.setMediumImgUrl(usedDeal.getImgUrl());
                        ipsHome.setReleaseTime(usedDeal.getReleaseTime());
                        ipsHome.setRefreshTime(usedDeal.getRefreshTime());
                        ipsHome.setAuditType(2);
                        ipsHome.setDeleteType(1);
                        ipsHome.setAfficheType(2);
                        ipsHome.setFraction(usedDeal.getFraction());
                        if (userId > 0 && j < 4) {
                            ips.add(ipsHome);
                        } else {
                            if (usedDeal.getFraction() >= 70) {
                                ips.add(ipsHome);
                            }
                        }
                    }
                }
            }
            if (otherList != null && otherList.size() > 0) {
                for (int j = 0; j < otherList.size(); j++) {
                    if (userId > 0 && j < 4) {
                        posts = (OtherPosts) otherList.get(j);
                        if (posts != null) {
                            IPS_Home ipsHome = new IPS_Home();
                            ipsHome.setInfoId(posts.getId());
                            ipsHome.setTitle(posts.getTitle());
                            ipsHome.setUserId(posts.getUserId());
                            ipsHome.setContent(posts.getContent());
                            ipsHome.setReleaseTime(posts.getAddTime());
                            ipsHome.setRefreshTime(posts.getRefreshTime());
                            ipsHome.setAuditType(2);
                            ipsHome.setDeleteType(1);
                            ipsHome.setAfficheType(6);
                            ipsHome.setFraction(posts.getFraction());
                            ips.add(ipsHome);
                        }
                    }
                }
            }
            if (goodsList != null && goodsList.size() > 0) {
                for (int j = 0; j < goodsList.size(); j++) {
                    searchGoods = (SearchGoods) goodsList.get(j);
                    if (searchGoods != null) {
                        IPS_Home ipsHome = new IPS_Home();
                        ipsHome.setInfoId(searchGoods.getId());
                        ipsHome.setTitle(searchGoods.getTitle());
                        ipsHome.setUserId(searchGoods.getUserId());
                        ipsHome.setContent(searchGoods.getContent());
                        ipsHome.setReleaseTime(searchGoods.getAddTime());
                        ipsHome.setMediumImgUrl(searchGoods.getImgUrl());
                        ipsHome.setRefreshTime(searchGoods.getRefreshTime());
                        ipsHome.setAuditType(2);
                        ipsHome.setDeleteType(1);
                        ipsHome.setAfficheType(searchGoods.getSearchType() + 2);
                        ipsHome.setFraction(searchGoods.getFraction());
                        if (userId > 0 && j < 4) {
                            ips.add(ipsHome);
                        } else {
                            if (searchGoods.getFraction() >= 70) {
                                ips.add(ipsHome);
                            }
                        }
                    }
                }
            }
            if (loveList != null && loveList.size() > 0) {
                for (int j = 0; j < loveList.size(); j++) {
                    loveAndFriends = (LoveAndFriends) loveList.get(j);
                    if (loveAndFriends != null) {
                        IPS_Home ipsHome = new IPS_Home();
                        ipsHome.setInfoId(loveAndFriends.getId());
                        ipsHome.setTitle(loveAndFriends.getTitle());
                        ipsHome.setUserId(loveAndFriends.getUserId());
                        ipsHome.setContent(loveAndFriends.getContent());
                        ipsHome.setMediumImgUrl(loveAndFriends.getImgUrl());
                        ipsHome.setRefreshTime(loveAndFriends.getRefreshTime());
                        ipsHome.setReleaseTime(loveAndFriends.getReleaseTime());
                        ipsHome.setAuditType(2);
                        ipsHome.setDeleteType(1);
                        ipsHome.setAfficheType(1);
                        ipsHome.setFraction(loveAndFriends.getFraction());
                        if (userId > 0 && j < 4) {
                            ips.add(ipsHome);
                        } else {
                            if (loveAndFriends.getFraction() >= 70) {
                                ips.add(ipsHome);
                            }
                        }
                    }
                }
            }
            if (rentList != null && rentList.size() > 0) {
                for (int j = 0; j < rentList.size(); j++) {
                    rentAhouse = (RentAhouse) dealList.get(j);
                    if (usedDeal != null) {
                        IPS_Home ipsHome = new IPS_Home();
                        ipsHome.setInfoId(rentAhouse.getId());
                        ipsHome.setTitle(rentAhouse.getTitle());
                        ipsHome.setUserId(rentAhouse.getUserId());
                        ipsHome.setContent(rentAhouse.getFormulation());
                        ipsHome.setMediumImgUrl(rentAhouse.getPicture());
                        ipsHome.setReleaseTime(rentAhouse.getAddTime());
                        ipsHome.setRefreshTime(rentAhouse.getRefreshTime());
                        ipsHome.setAuditType(2);
                        ipsHome.setDeleteType(1);
                        ipsHome.setAfficheType(rentAhouse.getRoomState() + 9);
                        ipsHome.setFraction(rentAhouse.getRentalType());
                        if (userId > 0 && j < 4) {
                            ips.add(ipsHome);
                        } else {
                            if (rentAhouse.getRentalType() >= 70) {
                                ips.add(ipsHome);
                            }
                        }
                    }
                }
            }
            Collections.sort(ips, new Comparator<IPS_Home>() {
                @Override
                public int compare(IPS_Home o1, IPS_Home o2) {
                    // 按照刷新时间进行降序排列
                    if (o1.getRefreshTime().getTime() > o2.getRefreshTime().getTime()) {
                        return -1;
                    }
                    if (o1.getRefreshTime().getTime() == o2.getRefreshTime().getTime()) {
                        return 0;
                    }
                    return 1;
                }
            });
            //更新到缓存
            if (userId <= 0) {
                if (ips != null && ips.size() > 0) {
                    redisUtils.pushList(Constants.REDIS_KEY_IPS_HOMELIST, ips);
                }
            }
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, StatusCode.CODE_SUCCESS.CODE_DESC, ips);
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, StatusCode.CODE_SUCCESS.CODE_DESC, homeList);
    }

    /***
     * 刷新公告时间
     * @param infoId      公告ID
     * @param userId      用户ID
     * @param afficheType 公告类别标志：1婚恋交友,2二手手机,3寻人,4寻物,5失物招领,6其他 7发简历找工作 8发布招聘（注：后续添加）
     * @return
     */
    @Override
    public ReturnData refreshTime(@PathVariable long infoId, @PathVariable long userId, @PathVariable int afficheType) {
        //验证参数
        if (infoId <= 0 || userId <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "ID参数有误", new JSONObject());
        }
        //验证操作人员权限
        if (CommonUtils.getMyId() != userId) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "参数有误，当前用户[" + CommonUtils.getMyId() + "]无权限刷新用户[" + userId + "]的公告信息", new JSONObject());
        }
        List list = null;
        OtherPosts posts = null;
        UsedDeal usedDeal = null;
        SearchGoods searchGoods = null;
        IPS_Home ipsHome = null;
        WorkResume resume = null;
        WorkRecruit recruit = null;
        LoveAndFriends loveAndFriends = null;
        list = redisUtils.getList(Constants.REDIS_KEY_IPS_HOMELIST, 0, 101);
        if (afficheType == 1) { //婚恋交友
            ipsHome = new IPS_Home();
            loveAndFriends = loveAndFriendsService.findByIdUser(userId);
            if (loveAndFriends == null) {
                return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
            }
            loveAndFriends.setRefreshTime(new Date());
            loveAndFriendsService.updateTime(loveAndFriends);

            //更新home
            ipsHome.setAuditType(2);
            ipsHome.setDeleteType(1);
            ipsHome.setInfoId(infoId);
            ipsHome.setUserId(userId);
            ipsHome.setAfficheType(1);
            ipsHome.setTitle(loveAndFriends.getTitle());
            ipsHome.setContent(loveAndFriends.getContent());
            ipsHome.setReleaseTime(loveAndFriends.getReleaseTime());
            ipsHome.setRefreshTime(loveAndFriends.getRefreshTime());
            ipsHome.setFraction(loveAndFriends.getFraction());
            ipsHome.setMediumImgUrl(loveAndFriends.getImgUrl());
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    IPS_Home home = (IPS_Home) list.get(i);
                    if (home.getAfficheType() == 1 && home.getInfoId() == loveAndFriends.getId()) {
                        redisUtils.removeList(Constants.REDIS_KEY_IPS_HOMELIST, 1, home);
                    }
                }
            }
            //清除缓存中的信息
            redisUtils.expire(Constants.REDIS_KEY_IPS_LOVEANDFRIEND + infoId, 0);
        } else if (afficheType == 2) {//二手手机
            ipsHome = new IPS_Home();
            usedDeal = usedDealService.findUserById(infoId);
            if (usedDeal == null) {
                return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
            }
            usedDeal.setRefreshTime(new Date());
            usedDealService.updateTime(usedDeal);

            //更新home
            ipsHome.setAuditType(2);
            ipsHome.setDeleteType(1);
            ipsHome.setInfoId(infoId);
            ipsHome.setUserId(userId);
            ipsHome.setAfficheType(2);
            ipsHome.setTitle(usedDeal.getTitle());
            ipsHome.setContent(usedDeal.getContent());
            ipsHome.setReleaseTime(usedDeal.getReleaseTime());
            ipsHome.setRefreshTime(usedDeal.getRefreshTime());
            ipsHome.setFraction(usedDeal.getFraction());
            ipsHome.setMediumImgUrl(usedDeal.getImgUrl());
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    IPS_Home home = (IPS_Home) list.get(i);
                    if (home.getAfficheType() == 2 && home.getInfoId() == usedDeal.getId()) {
                        redisUtils.removeList(Constants.REDIS_KEY_IPS_HOMELIST, 1, home);
                    }
                }
            }
            //清除缓存中的信息
            redisUtils.expire(Constants.REDIS_KEY_IPS_USEDDEAL + infoId, 0);
        } else if (afficheType == 3 || afficheType == 4 || afficheType == 5) {//寻人,寻物，失物招领
            ipsHome = new IPS_Home();
            searchGoods = searchGoodsService.findUserById(infoId);
            if (searchGoods == null) {
                return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
            }
            searchGoods.setRefreshTime(new Date());
            searchGoodsService.updateTime(searchGoods);

            //更新home
            ipsHome.setAuditType(2);
            ipsHome.setDeleteType(1);
            ipsHome.setInfoId(infoId);
            ipsHome.setUserId(userId);
            ipsHome.setTitle(searchGoods.getTitle());
            ipsHome.setContent(searchGoods.getContent());
            ipsHome.setReleaseTime(searchGoods.getAddTime());
            ipsHome.setRefreshTime(searchGoods.getRefreshTime());
            ipsHome.setFraction(searchGoods.getFraction());
            ipsHome.setMediumImgUrl(searchGoods.getImgUrl());
            ipsHome.setAfficheType(searchGoods.getSearchType() + 2);
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    IPS_Home home = (IPS_Home) list.get(i);
                    if ((home.getAfficheType() == 3 || home.getAfficheType() == 4 || home.getAfficheType() == 5) && home.getInfoId() == searchGoods.getId()) {
                        redisUtils.removeList(Constants.REDIS_KEY_IPS_HOMELIST, 1, home);
                    }
                }
            }
            //清除缓存中的信息
            redisUtils.expire(Constants.REDIS_KEY_IPS_SEARCHGOODS + infoId, 0);
        } else if (afficheType == 6) { //其他
            ipsHome = new IPS_Home();
            posts = otherPostsService.findUserById(infoId);
            if (posts == null) {
                return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
            }
            posts.setRefreshTime(new Date());
            otherPostsService.updateTime(posts);

            //更新home
            ipsHome.setAuditType(2);
            ipsHome.setDeleteType(1);
            ipsHome.setInfoId(infoId);
            ipsHome.setUserId(userId);
            ipsHome.setAfficheType(6);
            ipsHome.setTitle(posts.getTitle());
            ipsHome.setContent(posts.getContent());
            ipsHome.setReleaseTime(posts.getAddTime());
            ipsHome.setRefreshTime(posts.getRefreshTime());
            ipsHome.setFraction(posts.getFraction());
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    IPS_Home home = (IPS_Home) list.get(i);
                    if (home.getAfficheType() == 6 && home.getInfoId() == posts.getId()) {
                        redisUtils.removeList(Constants.REDIS_KEY_IPS_HOMELIST, 1, home);
                    }
                }
            }
            //清除缓存中的信息
            redisUtils.expire(Constants.REDIS_KEY_IPS_OTHERPOSTS + infoId, 0);
        } else if (afficheType == 7) {//发布招聘
            resume = workResumeService.findById(infoId);
            if (resume == null) {
                return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
            }
            resume.setRefreshTime(new Date());
            workResumeService.updateTime(resume);

            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
        } else if (afficheType == 8) {//发布招聘
            recruit = workRecruitService.findRecruit(infoId);
            if (recruit == null) {
                return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
            }
            recruit.setRefreshTime(new Date());
            workRecruitService.refreshRecruit(recruit);

            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
        } else {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
        }
        if (ipsHome != null) {//防止脏数据进入缓存
            //放入缓存
            redisUtils.addListLeft(Constants.REDIS_KEY_IPS_HOMELIST, ipsHome, 0);
            if (list.size() == 101) {
                //清除缓存中的信息
                redisUtils.expire(Constants.REDIS_KEY_IPS_HOMELIST, 0);
                redisUtils.pushList(Constants.REDIS_KEY_IPS_HOMELIST, list, 0);
            }
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 置顶公告
     * @param infoId
     * @param userId
     * @param frontPlaceType 0未置顶  1当前分类置顶  2推荐列表置顶
     * @return
     */
    @Override
    public ReturnData setTop(@PathVariable long infoId, @PathVariable long userId, @PathVariable int frontPlaceType, @PathVariable int afficheType) {
        //验证参数
        if (infoId <= 0 || userId <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "ID参数有误", new JSONObject());
        }
        //验证操作人员权限
        if (CommonUtils.getMyId() != userId) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "参数有误，当前用户[" + CommonUtils.getMyId() + "]无权限置顶用户[" + userId + "]的公告", new JSONObject());
        }
        //获取会员等级 根据用户会员等级 获取最大次数 后续添加
        UserMembership memberMap = userMembershipUtils.getUserMemberInfo(CommonUtils.getMyId());
        int numLimit = Constants.SET_TOP_COUNT_USER;
        int memberShipStatus = 0;
        if (memberMap != null) {
            memberShipStatus = memberMap.getMemberShipStatus();
        }
        if (memberShipStatus == 1) {//普通会员
            numLimit = Constants.SET_TOP_COUNT_MEMBER;
        } else if (memberShipStatus > 1) {//高级以上
            numLimit = Constants.SET_TOP_COUNT_SENIOR_MEMBER;
        } else {
            return returnData(StatusCode.CODE_SETTOP_UNQUALIFIED.CODE_VALUE, "很抱歉，您没有置顶资格,成为会员可开启置顶功能!", new JSONObject());
        }
        //获取当前时间毫秒数
        long now = new Date().getTime();
        // 获取当月最后一天(首先要获取前月的最后一天)
        Calendar cale = null;
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 1);
        cale.set(Calendar.DAY_OF_MONTH, 0);
        Date lastday = cale.getTime();
        //计算当前时间到月底的秒数差
        long second = (lastday.getTime() - now) / 1000;

        //和缓存中的记录比较是否到达上限
        Object obj = redisUtils.hget(Constants.REDIS_KEY_USER_SET_TOP, userId + "");
        if (obj == null || CommonUtils.checkFull(obj.toString())) {
            //查询用户当月置顶次数
            int num = 0;
            num = loveAndFriendsService.statistics(userId);
            num += searchGoodsService.statistics(userId);
            num += otherPostsService.statistics(userId);
            if (num >= numLimit) {
                //此处需要判断会员级别
                if (memberShipStatus == 0) {//普通用户
                    return returnData(StatusCode.CODE_SETTOP_UNQUALIFIED.CODE_VALUE, "很抱歉，您还没有置顶资格,成为会员可开启刷新功能!", new JSONObject());
                } else if (memberShipStatus == 1) {//普通会员
                    return returnData(StatusCode.CODE_SETTOP_ORDINARY_TOPLIMIT.CODE_VALUE, "很抱歉，您本月的置顶次数已用尽,成为高级会员可获得更多次数!", new JSONObject());
                } else {
                    return returnData(StatusCode.CODE_SETTOP_SENIOR_TOPLIMIT.CODE_VALUE, "很抱歉，您本月的置顶次数已用尽,下个月再来吧!", new JSONObject());
                }
            }
            redisUtils.hset(Constants.REDIS_KEY_USER_SET_TOP, userId + "", num + 1, second);
        } else {//已有记录 比较是否达到上限
            int count = Integer.parseInt(obj.toString());
            if (count >= numLimit) {
                //此处需要判断会员级别
                if (memberShipStatus == 0) {//普通用户
                    return returnData(StatusCode.CODE_SETTOP_UNQUALIFIED.CODE_VALUE, "很抱歉，您没有置顶资格,成为会员可开启刷新功能!", new JSONObject());
                } else if (memberShipStatus == 1) {//普通会员
                    return returnData(StatusCode.CODE_SETTOP_ORDINARY_TOPLIMIT.CODE_VALUE, "很抱歉，您本月的置顶次数已用尽,成为高级会员可获得更多次数!", new JSONObject());
                } else {
                    return returnData(StatusCode.CODE_SETTOP_SENIOR_TOPLIMIT.CODE_VALUE, "很抱歉，您本月的置顶次数已用尽,下个月再来吧!", new JSONObject());
                }
            }
            count++;
            redisUtils.hset(Constants.REDIS_KEY_USER_SET_TOP, userId + "", count, second);
        }
        OtherPosts posts = null;
        UsedDeal usedDeal = null;
        SearchGoods searchGoods = null;
        LoveAndFriends loveAndFriends = null;
        if (afficheType == 1) { //婚恋交友
            loveAndFriends = loveAndFriendsService.findByIdUser(userId);
            if (loveAndFriends == null) {
                return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "当前公告不存在！", new JSONObject());
            }
            loveAndFriends.setFrontPlaceType(frontPlaceType);
            loveAndFriendsService.setTop(loveAndFriends);
            //清除缓存中的信息
            redisUtils.expire(Constants.REDIS_KEY_IPS_LOVEANDFRIEND + infoId, 0);
        }
        if (afficheType == 2) {//二手手机
            usedDeal = usedDealService.findUserById(userId);
            if (usedDeal == null) {
                return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "当前公告不存在！", new JSONObject());
            }
            usedDeal.setFrontPlaceType(frontPlaceType);
            usedDealService.setTop(usedDeal);
            //清除缓存中的信息
            redisUtils.expire(Constants.REDIS_KEY_IPS_USEDDEAL + infoId, 0);
        }
        if (afficheType == 3 || afficheType == 4 || afficheType == 5) {//寻人,寻物，失物招领
            searchGoods = searchGoodsService.findUserById(infoId);
            if (searchGoods == null) {
                return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "当前公告不存在！", new JSONObject());
            }
            searchGoods.setFrontPlaceType(frontPlaceType);
            searchGoodsService.setTop(searchGoods);
            //清除缓存中的信息
            redisUtils.expire(Constants.REDIS_KEY_IPS_SEARCHGOODS + infoId, 0);
        }
        if (afficheType == 6) { //其他
            posts = otherPostsService.findUserById(infoId);
            if (posts == null) {
                return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "当前公告不存在！", new JSONObject());
            }
            posts.setFrontPlaceType(frontPlaceType);
            otherPostsService.setTop(posts);
            //清除缓存中的信息
            redisUtils.expire(Constants.REDIS_KEY_IPS_OTHERPOSTS + infoId, 0);
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }
}
