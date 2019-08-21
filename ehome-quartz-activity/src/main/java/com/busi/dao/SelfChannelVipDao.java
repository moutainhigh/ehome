package com.busi.dao;

import com.busi.entity.SelfChannelVip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 自频道会员DAO
 *
 * @author: ZHaoJiaJie
 * @create: 2019-03-22 13:57
 */
@Mapper
@Repository
public interface SelfChannelVipDao {

    /***
     * 查询会员信息
     * @return
     */
    @Select("select * from SelfChannelVip where memberShipStatus = 0 and TO_DAYS(expiretTime)>TO_DAYS(NOW())")
    List<SelfChannelVip> findMembershipList();

    /***
     * 更新
     * @param userMembership
     * @return
     */
    @Update("<script>" +
            "update SelfChannelVip set" +
            " memberShipStatus=#{memberShipStatus}" +
            " where userId=#{userId}" +
            "</script>")
    int update(SelfChannelVip userMembership);
}
