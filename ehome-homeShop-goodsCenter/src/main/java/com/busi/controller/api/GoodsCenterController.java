package com.busi.controller.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.busi.controller.BaseController;
import com.busi.entity.*;
import com.busi.service.GoodsCenterService;
import com.busi.service.HomeShopOtherService;
import com.busi.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品信息相关接口 如：发布商品 管理商品 商品上下架等等
 * author：ZhaoJiaJie
 * create time：2019-7-25 13:46:18
 */
@RestController
public class GoodsCenterController extends BaseController implements GoodsCenterApiController {

    @Autowired
    MqUtils mqUtils;

    @Autowired
    RedisUtils redisUtils;

    @Autowired
    UserInfoUtils userInfoUtils;

    @Autowired
    private GoodsCenterService goodsCenterService;

    @Autowired
    HomeShopOtherService collectService;


    /***
     * 发布商品
     * @param homeShopGoods
     * @return
     */
    @Override
    public ReturnData addShopGoods(@Valid @RequestBody HomeShopGoods homeShopGoods, BindingResult bindingResult) {
        //验证参数格式是否正确
        if (bindingResult.hasErrors()) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, checkParams(bindingResult), new JSONObject());
        }
        //处理特殊字符
        String title = homeShopGoods.getGoodsTitle();
        if (!CommonUtils.checkFull(title)) {
            String filteringTitle = CommonUtils.filteringContent(title);
            if (CommonUtils.checkFull(filteringTitle)) {
                return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "标题不能为空并且不能包含非法字符！", new JSONArray());
            }
            homeShopGoods.setGoodsTitle(filteringTitle);
        }
        //验证地区
        if (!CommonUtils.checkProvince_city_district(0, homeShopGoods.getProvince(), homeShopGoods.getCity(), homeShopGoods.getDistrict())) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "省、市、区参数不匹配", new JSONObject());
        }
        //新增商品对应属性
        GoodsProperty property = new GoodsProperty();
        property.setGoodsId(homeShopGoods.getId());
        property.setName(homeShopGoods.getPropertyName());
        goodsCenterService.addProperty(property);

        //获取属性值
        String propertyName = "";
        if (!CommonUtils.checkFull(homeShopGoods.getPropertyName())) {
            String[] strings = homeShopGoods.getPropertyName().split("_");
            for (int i = 0; i < strings.length; i++) {
                String[] s = strings[i].split(",");
                if (i == 0) {
                    propertyName = "#" + s[2] + "#";
                } else {
                    propertyName += "," + "#" + s[2] + "#";
                }
            }
            homeShopGoods.setPropertyName(propertyName);
        }
        homeShopGoods.setAuditType(1);
//        homeShopGoods.setSellType(1);
        homeShopGoods.setReleaseTime(new Date());
        homeShopGoods.setRefreshTime(new Date());
        goodsCenterService.add(homeShopGoods);

        //更新商品对应特殊属性
//        GoodsOfSpecialProperty ofSpecialProperty = new GoodsOfSpecialProperty();
//        ofSpecialProperty.setGoodsId(homeShopGoods.getId());
//        ofSpecialProperty.setName(homeShopGoods.getSpecialProperty());
//        goodsCenterService.addSpecialProperty(ofSpecialProperty);

        Map<String, Object> map = new HashMap<>();
        map.put("infoId", homeShopGoods.getId());
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", map);
    }

    /***
     * 更新商品
     * @param homeShopGoods
     * @return
     */
    @Override
    public ReturnData changeShopGoods(@Valid @RequestBody HomeShopGoods homeShopGoods, BindingResult bindingResult) {
        //验证参数格式是否正确
        if (bindingResult.hasErrors()) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, checkParams(bindingResult), new JSONObject());
        }
        //处理特殊字符
        String title = homeShopGoods.getGoodsTitle();
        if (!CommonUtils.checkFull(title)) {
            String filteringTitle = CommonUtils.filteringContent(title);
            if (CommonUtils.checkFull(filteringTitle)) {
                return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "标题不能为空并且不能包含非法字符！", new JSONArray());
            }
            homeShopGoods.setGoodsTitle(filteringTitle);
        }
        //验证修改人权限
        if (CommonUtils.getMyId() != homeShopGoods.getUserId()) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "参数有误，当前用户[" + CommonUtils.getMyId() + "]无权限修改用户[" + homeShopGoods.getUserId() + "]的商品信息", new JSONObject());
        }
        // 查询数据库
        HomeShopGoods posts = goodsCenterService.findUserById(homeShopGoods.getId());
        if (posts == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
        }
        //更新商品对应属性
        GoodsProperty property = new GoodsProperty();
        property.setGoodsId(homeShopGoods.getId());
        property.setName(homeShopGoods.getPropertyName());
        goodsCenterService.updateProperty(property);

        //获取属性值
        String propertyName = "";
        if (!CommonUtils.checkFull(homeShopGoods.getPropertyName())) {
            String[] strings = homeShopGoods.getPropertyName().split("_");
            for (int i = 0; i < strings.length; i++) {
                String[] s = strings[i].split(",");
                if (i == 0) {
                    propertyName = "#" + s[2] + "#";
                } else {
                    propertyName += "," + "#" + s[2] + "#";
                }
            }
        }
        homeShopGoods.setPropertyName(propertyName);
        homeShopGoods.setRefreshTime(new Date());
        goodsCenterService.update(homeShopGoods);
        //更新商品对应特殊属性
//        GoodsOfSpecialProperty ofSpecialProperty = new GoodsOfSpecialProperty();
//        ofSpecialProperty.setGoodsId(homeShopGoods.getId());
//        ofSpecialProperty.setName(homeShopGoods.getSpecialProperty());
//        goodsCenterService.updateSpecialProperty(ofSpecialProperty);

        if (!CommonUtils.checkFull(homeShopGoods.getDelImgUrls())) {
            //调用MQ同步 图片到图片删除记录表
            mqUtils.sendDeleteImageMQ(homeShopGoods.getUserId(), homeShopGoods.getDelImgUrls());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("infoId", homeShopGoods.getId());
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", map);
    }

    /***
     * 批量上下架商品
     * @param ids 商品ids(逗号分隔)
     * @param sellType 商品买卖状态 : 0上架，1下架
     * @return
     */
    @Override
    public ReturnData changeShopGoods(@PathVariable String ids, @PathVariable long userId, @PathVariable int sellType) {
        //验证参数
        if (userId <= 0 || CommonUtils.checkFull(ids)) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "参数有误", new JSONObject());
        }
        //验证删除权限
        if (CommonUtils.getMyId() != userId) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "参数有误，当前用户[" + CommonUtils.getMyId() + "]无权限上下架用户[" + userId + "]的商品信息", new JSONObject());
        }
        goodsCenterService.changeShopGoods(ids.split(","), sellType);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * @Description: 批量删除商品
     * @return:
     */
    @Override
    public ReturnData delShopGoods(@PathVariable String ids, @PathVariable long userId) {
        //验证参数
        if (userId <= 0 || CommonUtils.checkFull(ids)) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "参数有误", new JSONObject());
        }
        //验证删除权限
        if (CommonUtils.getMyId() != userId) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "参数有误，当前用户[" + CommonUtils.getMyId() + "]无权限删除用户[" + userId + "]的商品信息", new JSONObject());
        }
        goodsCenterService.updateDels(ids.split(","));
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 查询详情
     * @param id
     * @return
     */
    @Override
    public ReturnData getShopGoods(@PathVariable long id) {
        //验证参数
        if (id <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "参数有误", new JSONObject());
        }
        int num = 0;
        HomeShopGoods posts = null;
        posts = goodsCenterService.findUserById(id);
        if (posts == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
        }
        //商品是下架状态或者查看者不是本人时禁止查看
        if (posts.getSellType() != 0) {
            if (posts.getUserId() != CommonUtils.getMyId()) {
                return returnData(StatusCode.CODE_IPS_AFFICHE_NOT_EXIST.CODE_VALUE, "您要查看的商品已下架或已被主人删除", new JSONObject());
            }
        }
        UserInfo userInfo = null;
        userInfo = userInfoUtils.getUserInfo(posts.getUserId());
        //查询商品对应属性
        GoodsProperty property = goodsCenterService.findProperty(id);
        if (property != null) {
            posts.setPropertyName(property.getName());
        }
        //查询商品对应特殊属性
//            GoodsOfSpecialProperty goodsOfSpecialProperty = goodsCenterService.findSpecialProperty(id);
//            posts.setSpecialProperty(goodsOfSpecialProperty.getName());
        num = goodsCenterService.findNum(userInfo.getUserId(), 1);//已上架
        posts.setSellingNumber(num);
        if (userInfo != null) {
            posts.setName(userInfo.getName());
            posts.setHead(userInfo.getHead());
            posts.setProTypeId(userInfo.getProType());
            posts.setHouseNumber(userInfo.getHouseNumber());
        }
        Map<String, Object> map = CommonUtils.objectToMap(posts);
        //新增浏览记录
        HomeShopGoodsLook look = new HomeShopGoodsLook();
        look.setTime(new Date());
        look.setGoodsId(id);
        look.setUserId(CommonUtils.getMyId());
        look.setGoodsName(posts.getGoodsTitle());
        if (!CommonUtils.checkFull(posts.getImgUrl())) {
            String[] img = posts.getImgUrl().split(",");
            look.setImgUrl(img[0]);//用第一张图做封面
        }
        look.setPrice(posts.getPrice());
        look.setBasicDescribe(posts.getDetails());
        look.setSpecs(posts.getSpecs());
        collectService.addLook(look);
        posts.setSeeNumber(posts.getSeeNumber() + 1);
        goodsCenterService.updateSee(posts);
        int collection = 0;//是否收藏过此商品  0没有  1已收藏
        //验证是否收藏过
        HomeShopGoodsCollection flag = collectService.findUserId(id, CommonUtils.getMyId());
        if (flag != null) {
            collection = 1;//1已收藏
        }
        map.put("collection", collection);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", map);
    }

    /***
     * 分页查询商品（店家）
     * @param shopId  店铺ID
     * @param sort  查询条件:-1全部  0出售中，1仓库中，2已预约
     * @param stock  库存：0倒序 1正序
     * @param time  时间：0倒序 1正序
     * @param goodsSort  分类
     * @param page  页码 第几页 起始值1
     * @param count 每页条数
     * @return
     */
    @Override
    public ReturnData findGoodsList(@PathVariable int sort, @PathVariable long shopId, @PathVariable int stock, @PathVariable int time, @PathVariable long goodsSort, @PathVariable int page, @PathVariable int count) {
        //验证参数
        if (page < 0 || count <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "分页参数有误", new JSONObject());
        }
        //开始查询
        PageBean<HomeShopGoods> pageBean = null;
        pageBean = goodsCenterService.findDishesSortList(sort, shopId, stock, time, goodsSort, page, count);
        if (pageBean == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, StatusCode.CODE_SUCCESS.CODE_DESC, new JSONArray());
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", pageBean);
    }

    /***
     * 分页查询店铺推荐（用户）
     * @param userId  发布者ID
     * @param page  页码 第几页 起始值1
     * @param count 每页条数
     * @return
     */
    @Override
    public ReturnData findRecommendList(@PathVariable long userId, @PathVariable int page, @PathVariable int count) {
        //验证参数
        if (page < 0 || count <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "分页参数有误", new JSONObject());
        }
        //开始查询
        PageBean<HomeShopGoods> pageBean = null;
        pageBean = goodsCenterService.findRecommendList(userId, page, count);
        if (pageBean == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, StatusCode.CODE_SUCCESS.CODE_DESC, new JSONArray());
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", pageBean);
    }

    /***
     * 分页查询商品(用户调用)
     * @param levelOne 商品1级分类  默认为0, -2为不限
     * @param levelTwo 商品2级分类  默认为0, -2为不限
     * @param levelThree 商品3级分类  默认为0, -2为不限
     * @param levelFour 商品4级分类  默认为0, -2为不限
     * @param levelFive 商品5级分类  默认为0, -2为不限
     * @param sort  排序条件:0综合  1销量  2价格最高  3价格最低
     * @param brandId  null不限 多个品牌ID 逗号分隔
     * @param pinkageType  是否包邮:-1不限 0是  1否
     * @param minPrice  最小价格
     * @param maxPrice  最大价格
     * @param province  -1不限 发货地省份
     * @param city  -1不限 发货地城市
     * @param district  -1不限 发货地区域
     * @param propertyName  属性值 多个属性之间","分隔
     * @param letter 搜索商品名字
     * @param page  页码 第几页 起始值1
     * @param count 每页条数
     * @return
     */
    @Override
    public ReturnData findUserGoodsList(@PathVariable int levelOne, @PathVariable int levelTwo, @PathVariable int levelThree, @PathVariable int levelFour, @PathVariable int levelFive, @PathVariable int sort, @PathVariable String brandId, @PathVariable int pinkageType, @PathVariable int minPrice, @PathVariable int maxPrice, @PathVariable int province, @PathVariable int city, @PathVariable int district, @PathVariable String propertyName, @PathVariable String letter, @PathVariable int page, @PathVariable int count) {
        //验证参数
        if (page < 0 || count <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "分页参数有误", new JSONObject());
        }
        //开始查询
        String tagArray = "";
        String[] strings = null;
        PageBean<HomeShopGoods> pageBean = null;
        if (!CommonUtils.checkFull(propertyName)) {
            strings = propertyName.split(",");
            for (int i = 0; i < strings.length; i++) {
                if (i == 0) {
                    tagArray = "#" + strings[i] + "#";
                } else {
                    tagArray += "," + "#" + strings[i] + "#";
                }
            }
        }
        pageBean = goodsCenterService.findUserGoodsList(levelOne, levelTwo, levelThree, levelFour, levelFive, sort, brandId, pinkageType, minPrice, maxPrice, province, city, district, tagArray, letter, page, count);
        if (pageBean == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, StatusCode.CODE_SUCCESS.CODE_DESC, new JSONArray());
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", pageBean);
    }

    /***
     * 二货商城首页分类查询
     * @param sort  分类 0精选 1生活 2电器 3母婴 4时尚
     * @param page  页码 第几页 起始值1
     * @param count 每页条数
     * @return
     */
    @Override
    public ReturnData findHomePageList(@PathVariable int sort, @PathVariable int page, @PathVariable int count) {
        //验证参数
        if (page < 0 || count <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "分页参数有误", new JSONObject());
        }
        //开始查询
        PageBean<HomeShopGoods> pageBean = null;
        pageBean = goodsCenterService.findHomePageList(sort, page, count);
        if (pageBean == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, StatusCode.CODE_SUCCESS.CODE_DESC, new JSONArray());
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", pageBean);
    }

    /***
     * 新增分类
     * @param goodsSort
     * @return
     */
    @Override
    public ReturnData addGoodsSort(@Valid @RequestBody GoodsSort goodsSort, BindingResult bindingResult) {
        //验证参数格式是否正确
        if (bindingResult.hasErrors()) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, checkParams(bindingResult), new JSONObject());
        }
        //判断该用户分类数量 最多20个
        int num = goodsCenterService.findSortNum(goodsSort.getShopId());
        if (num >= 20) {
            return returnData(StatusCode.CODE_DISHESSORT_KITCHEN_ERROR.CODE_VALUE, "分类超过上限,拒绝新增！", new JSONObject());
        }
        goodsCenterService.addGoodsSort(goodsSort);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 修改分类
     * @param goodsSort
     * @return
     */
    @Override
    public ReturnData changeGoodsSort(@Valid @RequestBody GoodsSort goodsSort, BindingResult bindingResult) {
        //验证参数格式是否正确
        if (bindingResult.hasErrors()) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, checkParams(bindingResult), new JSONObject());
        }
        goodsCenterService.changeGoodsSort(goodsSort);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 批量修改商品分类
     * @param ids 商品id
     * @return
     */
    @Override
    public ReturnData editGoodsSort(@PathVariable String ids, @PathVariable long sortId, @PathVariable String sortName) {
        //验证参数
        if (CommonUtils.checkFull(ids)) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "参数有误", new JSONObject());
        }
        goodsCenterService.editGoodsSort(ids.split(","), sortId, sortName);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /**
     * @Description: 删除分类
     * @return:
     */
    @Override
    public ReturnData delGoodsSort(@PathVariable String ids) {
        //验证参数
        if (CommonUtils.checkFull(ids)) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "参数有误", new JSONObject());
        }
        goodsCenterService.delGoodsSort(ids.split(","));
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 查询分类列表
     * @param id  店铺
     * @param find  0默认所有 1一级分类 2二级分类
     * @param sortId  分类ID(仅查询二级分类有效)
     * @param page  页码 第几页 起始值1
     * @param count 每页条数
     * @return
     */
    @Override
    public ReturnData getGoodsSortList(@PathVariable long id, @PathVariable int find, @PathVariable int sortId, @PathVariable int page, @PathVariable int count) {
        //验证参数
        if (page < 0 || count <= 0) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, "分页参数有误", new JSONObject());
        }
        //开始查询
        PageBean<GoodsSort> pageBean = null;
        pageBean = goodsCenterService.getGoodsSortList(id, find, sortId, page, count);
        if (pageBean == null) {
            return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, StatusCode.CODE_SUCCESS.CODE_DESC, new JSONArray());
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", pageBean);
    }

    /***
     * 新增商品描述
     * @param goodsDescribe
     * @return
     */
    @Override
    public ReturnData addGoodsDescribe(@Valid @RequestBody GoodsDescribe goodsDescribe, BindingResult bindingResult) {
        //验证参数格式是否正确
        if (bindingResult.hasErrors()) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, checkParams(bindingResult), new JSONObject());
        }
        goodsCenterService.addGoodsDescribe(goodsDescribe);
        Map<String, Object> map = new HashMap<>();
        map.put("infoId", goodsDescribe.getId());
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", map);
    }

    /***
     * 更新商品描述
     * @param goodsDescribe
     * @return
     */
    @Override
    public ReturnData changeGoodsDescribe(@Valid @RequestBody GoodsDescribe goodsDescribe, BindingResult bindingResult) {
        //验证参数格式是否正确
        if (bindingResult.hasErrors()) {
            return returnData(StatusCode.CODE_PARAMETER_ERROR.CODE_VALUE, checkParams(bindingResult), new JSONObject());
        }
        goodsCenterService.changeGoodsDescribe(goodsDescribe);
        if (!CommonUtils.checkFull(goodsDescribe.getDelImgUrls())) {
            //调用MQ同步 图片到图片删除记录表
            mqUtils.sendDeleteImageMQ(goodsDescribe.getUserId(), goodsDescribe.getDelImgUrls());
        }
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /**
     * @Description: 删除商品描述
     * @return:
     */
    @Override
    public ReturnData delGoodsDescribe(@PathVariable long id, @PathVariable long userId) {
        //数据库删除
        goodsCenterService.delGoodsDescribe(id, userId);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", new JSONObject());
    }

    /***
     * 查询商品描述
     * @param id
     * @return
     */
    @Override
    public ReturnData getGoodsDescribe(@PathVariable long id) {
        GoodsDescribe dishes = goodsCenterService.disheSdetails(id);
        return returnData(StatusCode.CODE_SUCCESS.CODE_VALUE, "success", dishes);
    }
}
