package com.jy.sharework.service;

import com.jy.sharework.entity.SysConfig;
import com.jy.sharework.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysConfigService {
    private final SysConfigMapper sysConfigMapper;
    private volatile Map<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        List<SysConfig> list = sysConfigMapper.selectList(null);
        cache = list.stream().collect(Collectors.toConcurrentMap(SysConfig::getConfigKey, SysConfig::getConfigValue, (a, b) -> b));
    }

    public String get(String key, String defaultVal) {
        return cache.getOrDefault(key, defaultVal);
    }

    public int getInt(String key, int defaultVal) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultVal)));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public void upsert(String key, String value, String desc) {
        SysConfig c = sysConfigMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, key));
        if (c == null) {
            c = new SysConfig();
            c.setConfigKey(key);
            c.setConfigValue(value);
            c.setDescription(desc);
            sysConfigMapper.insert(c);
        } else {
            c.setConfigValue(value);
            sysConfigMapper.updateById(c);
        }
        reload();
    }
}
