package com.busi.utils;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 支付宝工具类
 * authorsuntj
 * Create time 2020/7/13 14:56
 */
@Slf4j
public class AlipayUtils {

    /***
     * 提现到支付宝
     * @param ordernumber  订单编号
     * @param identity     支付宝会员ID
     * @param money        提现金额
     */
    public static int cashOutToAli(String ordernumber,String identity,double money){
        AlipayClient alipayClient = new DefaultAlipayClient(
                Constants.ALIPAY_URL,
                Constants.ALIPAY_APPID,
                Constants.ALIPAY_PRIVATE_KEY,
                "json",
                "GBK",
                Constants.ALIPAY_PUBLIC_KEY,
                "RSA2");
        AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
        request.setBizContent("{" +
                "out_biz_no:"+ordernumber+"," +//商户端的唯一订单号，对于同一笔转账请求，商户需保证该订单号唯一。
                "trans_amount:"+money+"," +//订单总金额，单位为元，精确到小数点后两位，
                "product_code:TRANS_ACCOUNT_NO_PWD," +//业务产品码，单笔无密转账到支付宝账户固定为:TRANS_ACCOUNT_NO_PWD；单笔无密转账到银行卡固定为:TRANS_BANKCARD_NO_PWD;收发现金红包固定为:STD_RED_PACKET；
                "biz_scene:DIRECT_TRANSFER," +//描述特定的业务场景，可传的参数如下：DIRECT_TRANSFER：单笔无密转账到支付宝/银行卡, B2C现金红包; PERSONAL_COLLECTION：C2C现金红包-领红包
                "order_title:提现," +//转账业务的标题，用于在支付宝用户的账单里显示
                "original_order_id:," +//原支付宝业务单号。C2C现金红包-红包领取时，传红包支付时返回的支付宝单号；B2C现金红包、单笔无密转账到支付宝/银行卡不需要该参数。
                "payee_info:{" +//收款方信息
                "identity:"+identity+"," +//参与方的唯一标识
                "identity_type:ALIPAY_USER_ID," +//与方的标识类型，目前支持如下类型：1、ALIPAY_USER_ID 支付宝的会员ID 2、ALIPAY_LOGON_ID：支付宝登录号，支持邮箱和手机号格式
                "name:" +//参与方真实姓名，如果非空，将校验收款支付宝账号姓名一致性。当identity_type=ALIPAY_LOGON_ID时，本字段必填。
                "    }," +
                "remark:," +//业务备注
                "business_params:{sub_biz_scene:REDPACKET}" +
                "  }");
        AlipayFundTransUniTransferResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            log.info("调用支付宝提现失败!");
        }
        if(response.isSuccess()){
            log.info("调用支付宝提现成功!");
            return 0;
        } else {
            log.info("调用支付宝提现失败："+response.toString());
            return 1;
        }
    }
}