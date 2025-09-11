package com.xun.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

//    @Value(value = "${spring.data.redis.host}")
//    private String address;
//
//    @Value(value = "${spring.data.redis.port}")
//    private String port;
//
//    @Value(value = "${spring.data.redis.password}")
//    private String password;
//
//    private String addressValue = "redis://" + address + ":" + port;


    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.92.109:6379").setPassword("123456");
        return Redisson.create(config);
    }

}
