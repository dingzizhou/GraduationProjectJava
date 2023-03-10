package com.example.back.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.back.model.dto.CurrentUser;
import com.example.back.model.pojo.User;

public class TokenUtil {

    private static final String TOKEN_SECRET = "ZXCVBNM";

    private static final Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);

    public static String sign(User user){
        try {
            return JWT.create().withClaim("username",user.getUsername()).withClaim("id",user.getUuid()).sign(algorithm);
        }
        catch (JWTCreationException exception){
            return exception.getMessage();
        }
    }

    public static CurrentUser verify(String token){
        if(token == null){
            return new CurrentUser();
        }
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            String username = decodedJWT.getClaim("username").asString();
            String uuid = decodedJWT.getClaim("uuid").asString();
            return new CurrentUser(uuid,username);
        }
        catch (JWTVerificationException exception){
            return new CurrentUser();
        }
    }

}
