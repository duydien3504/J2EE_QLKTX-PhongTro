package com.group10.API_ManageDormitory.utils.momo;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

public class MoMoEncoder {
    public static String signHmacSHA256(String data, String secretKey) {
        try {
            return new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secretKey).hmacHex(data);
        } catch (Exception e) {
            throw new RuntimeException("Error signing MoMo request", e);
        }
    }
}
