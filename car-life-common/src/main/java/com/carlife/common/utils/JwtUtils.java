package com.carlife.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtils {
	//token有效时间
    public static final long EXPIRE = 1000 * 60 * 60 * 24 * 7;
    //只有服务器知道的key
    public static final String APP_SECRET = "xxxxxx0000ooooosecuret";
    public static String getJwtToken(Map<String,Object> claim){//claim载荷
        String JwtToken = Jwts.builder()
            	//头部信息
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                //当前时间
                .setIssuedAt(new Date())
                //有效时间截止
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                //设置载荷
                .addClaims(claim)
            	//认证
                .signWith(SignatureAlgorithm.HS256, APP_SECRET)
                .compact();
        return JwtToken;
    }
    /**
     * 判断token是否存在与有效
     * @param jwtToken
     * @return
     */
    public static boolean checkToken(String jwtToken) {
        if(jwtToken == null || "".equals(jwtToken)) return false;
        try {
            Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * 根据给定的key获取token体里面的数据，
     */
    public static String getClaimByJwtToken(String token,String key) {
        if(token == null || "".equals(token)) return "";
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String)claims.get(key);
    }
}
