package com.bridge.console.utils;

import com.bridge.console.utils.result.BaseErrorEnum;
import com.bridge.exception.BridgeProcessFailException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * @author Jay
 * @version v1.0
 * @description MD5加密
 * @date 2018-01-15 18:05
 */
@Slf4j
public class EncryptUtil {


    /**
     * MD5 加密
     *
     * @param str 需要md5的字符串
     * @return MD5
     */
    public static String getMd5Str(String str) {
        if (StringUtils.isEmpty(str)) {
            throw new BridgeProcessFailException(BaseErrorEnum.PARAM_ERROR.getCode(), "入参不能为空");
        }
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException caught!", e);
            return null;
        }
        byte[] byteArray = messageDigest.digest();
        StringBuilder md5StrBuff = new StringBuilder();
        for (byte b : byteArray) {
            if (Integer.toHexString(0xFF & b).length() == 1) {
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & b));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & b));
            }
        }
        String md5 = md5StrBuff.toString();
        if (StringUtils.isEmpty(md5)) {
            throw new BridgeProcessFailException(BaseErrorEnum.PARAM_ERROR.getCode(), "md5错误");
        }
        return md5;
    }

    /**
     * 获取token
     *
     * @param str 字符串
     * @return token
     */
    public static String getToken(String str) {
        return new StringBuilder(getMd5Str(str))
                .insert(8, "-")
                .insert(17, "-")
                .insert(26, "-")
                .toString();
    }


    /**
     * 生成appCode
     *
     * @return 系统编码
     */
    public static String getAppCode() {
        String str = getMd5Str(String.valueOf(System.currentTimeMillis())).substring(0, 16);
        return new StringBuilder(str)
                .insert(4, "-")
                .insert(9, "-")
                .insert(14, "-")
                .toString();
    }
}
