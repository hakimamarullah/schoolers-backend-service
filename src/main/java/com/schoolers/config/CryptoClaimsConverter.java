package com.schoolers.config;

import com.schoolers.utils.CryptoUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
public class CryptoClaimsConverter implements Converter<Map<String, Object>, Map<String, Object>> {

    private final CryptoUtils cryptoUtils;



    // === Builder API ===

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Map<String, Object> convert(@NonNull Map<String, Object> source) {
        return decryptClaims(source);
    }

    public static class Builder {
        private CryptoUtils cryptoUtils;


        public Builder withCryptoUtils(CryptoUtils cryptoUtils) {
            this.cryptoUtils = cryptoUtils;
            return this;
        }

        public CryptoClaimsConverter build() {
            if (cryptoUtils == null) {
                throw new IllegalArgumentException("CryptoUtils must not be null");
            }
            return new CryptoClaimsConverter(cryptoUtils);
        }
    }

    private Map<String, Object> decryptClaims(Map<String, Object> claims) {
        Map<String, Object> decryptedClaims = new HashMap<>(claims);
        String[] targetClaims = {"profileId", "role", "sub", "classroomId"};
        for (String claim : targetClaims) {
            if (claims.containsKey(claim)) {
                decryptedClaims.put(claim, cryptoUtils.decrypt((String) claims.get(claim)));
            }
        }
        Instant iat = ((Date) claims.get("iat")).toInstant();
        Instant exp = ((Date) claims.get("exp")).toInstant();
        decryptedClaims.put("iat", iat);
        decryptedClaims.put("exp", exp);
        return decryptedClaims;
    }
}
