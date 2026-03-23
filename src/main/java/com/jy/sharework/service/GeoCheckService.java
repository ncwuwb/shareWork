package com.jy.sharework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeoCheckService {
    private final StringRedisTemplate redisTemplate;

    /**
     * 使用 Redis GEO 计算两点直线距离（米），与文档要求一致。
     */
    public double distanceMeters(double lon1, double lat1, double lon2, double lat2) {
        String key = "geo:once:" + Thread.currentThread().getId() + ":" + System.nanoTime();
        try {
            redisTemplate.opsForGeo().add(key, new Point(lon1, lat1), "p1");
            redisTemplate.opsForGeo().add(key, new Point(lon2, lat2), "p2");
            Distance d = redisTemplate.opsForGeo().distance(key, "p1", "p2", RedisGeoCommands.DistanceUnit.METERS);
            if (d == null) {
                return Double.MAX_VALUE;
            }
            return d.getValue();
        } finally {
            redisTemplate.delete(key);
        }
    }
}
