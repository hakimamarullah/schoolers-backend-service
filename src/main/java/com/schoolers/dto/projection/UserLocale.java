package com.schoolers.dto.projection;

import java.util.Locale;
import java.util.Optional;

public interface UserLocale {

    String getLocale();
    Long getUserId();
    String getLoginId();
    default Locale toLocale() {
        return Optional.ofNullable(getLocale())
                .map(Locale::of)
                .orElse(Locale.US);
    }
}
