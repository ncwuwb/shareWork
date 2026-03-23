package com.jy.sharework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jy.sharework.entity.SysUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SysUserJsonTest {

    @Test
    void passwordIsWriteOnly() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SysUser user = new SysUser();
        user.setPhone("13800000000");
        user.setPassword("secret");

        String json = objectMapper.writeValueAsString(user);

        assertFalse(json.contains("password"));

        SysUser deserialized = objectMapper.readValue(
                "{\"phone\":\"13800000000\",\"password\":\"secret\"}",
                SysUser.class
        );
        assertEquals("secret", deserialized.getPassword());
    }
}
