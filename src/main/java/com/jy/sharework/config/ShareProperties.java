package com.jy.sharework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Component
@ConfigurationProperties(prefix = "share")
public class ShareProperties {
    private Jwt jwt = new Jwt();
    private Upload upload = new Upload();
    private Cors cors = new Cors();
    private Bootstrap bootstrap = new Bootstrap();
    private Captcha captcha = new Captcha();

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:5173";
    }

    @Data
    public static class Jwt {
        private String secret = "changeMe";
        private int appExpireDays = 7;
        private int adminExpireHours = 12;
    }

    @Data
    public static class Upload {
        private String dir = "uploads";
    }

    @Data
    public static class Bootstrap {
        private Admin admin = new Admin();
    }

    @Data
    public static class Admin {
        private boolean enabled;
        private String phone;
        private String password;
        private String realName = "Super Admin";
    }

    @Data
    public static class Captcha {
        private boolean debugLogCode = true;
    }

    public List<String> corsOriginsList() {
        return Arrays.stream(cors.getAllowedOrigins().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}
