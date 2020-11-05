package com.example.demo.util;

import io.jsonwebtoken.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class AuthHelper {

    private static String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4/Ipw/yzV3OB1fS4ngnnH2cRDy7dZTBP8TEaqJiILHve3P2Z6NSgTr9dbLCUXjO+pwt6t/dMs2oVPDfpM8I+10g9cO+gcPA5QTzTKcHoned0B9p8jzEEvSDBlej1qH0+SgJMooQrbJXHjatF4TiAOTCFT+yRwPbcar0QYhvUWNV52xjEvj4yDnmK42y819LY7Hy+Gkzky4iV9mjf6qEFmlxTjSdqxuQo0Y68YHJZLGSx3rQmNzYt0XY8So3aGKXz/v4mMHkZl62mQGx5U/80LmB+3j6WjIJXilJmj1pbMU6Cp6sWjA9pTgAxF5LDzxplXpjQas33vsJ5n1xsmGxpowIDAQAB";

    private static String issuer = "https://accounts.google.com";

    private static String audience = "914528319640-a5n4p54gi49pt3eib8pftsgdoh7e1pb1.apps.googleusercontent.com";

    public static boolean verifyToken(String idToken) {



        if(idToken == null){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "missing token");
        }
        try {
            Jws<Claims> jws = Jwts.parserBuilder().requireIssuer(issuer)
                    .setSigningKey(getParsedPublicKey()) // <---- publicKey, not privateKey
                    .build()
                    .parseClaimsJws(idToken);
            System.out.println(jws);
        } catch (ExpiredJwtException e){
            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }  catch (JwtException ex) {
            System.out.println(ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
        return true;
    }

    public static RSAPublicKey getParsedPublicKey() {
        // public key content...excluding '---PUBLIC KEY---' and '---END PUBLIC KEY---'
        String PUB_KEY = publicKey;

        // removes white spaces or char 20
        String PUBLIC_KEY = "";
        if (!PUB_KEY.isEmpty()) {
            PUBLIC_KEY = PUB_KEY.replace(" ", "");
        }

        try {
            byte[] decode = com.google.api.client.util.Base64.decodeBase64(PUBLIC_KEY);
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(decode);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
            return pubKey;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            System.out.println("Exception block | Public key parsing error ");
        }
        return null;
    }
}
