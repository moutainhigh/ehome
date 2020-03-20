package com.busi.service;

import com.busi.dao.CommunityNewsDao;
import com.busi.entity.CommunityNews;
import com.busi.entity.PageBean;
import com.busi.utils.PageUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @program: ehome
 * @description: 资讯
 * @author: ZHaoJiaJie
 * @create: 2020-03-20 11:48:31
 */
@Service
public class CommunityNewsService {
    @Autowired
    private CommunityNewsDao todayNewsDao;

    /***
     * 新增
     * @param todayNews
     * @return
     */
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public int add(CommunityNews todayNews) {
        return todayNewsDao.add(todayNews);
    }

    /***
     * 更新
     * @param todayNews
     * @return
     */
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public int editNews(CommunityNews todayNews) {
        return todayNewsDao.editNews(todayNews);
    }

    /***
     * 删除
     * @param id
     * @return
     */
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public int del(long id) {
        return todayNewsDao.del(id);
    }

    /***
     * 查询详情
     */
    public CommunityNews findInfo(long infoId) {
        return todayNewsDao.findInfo(infoId);
    }

    /***
     * 查询列表
     */
    public PageBean<CommunityNews> findList(long communityId, int newsType, int page, int count) {
        List<CommunityNews> list;
        Page p = PageHelper.startPage(page, count);//为此行代码下面的第一行sql查询结果进行分页
        list = todayNewsDao.findList(communityId, newsType);

        return PageUtils.getPageBean(p, list);
    }

}
