package com.gree.ecommerce.config;

import com.gree.ecommerce.utils.RedissonLockUtil;
import com.gree.ecommerce.utils.StringUtils;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author A80080
 * @createDate 2021/1/18
 */
@Configuration
@ConfigurationProperties(prefix = "redisson")
@Data
@AutoConfigureOrder
public class RedissonConfig {

    private int timeout = 3000;

    private String address;

    private String password;

    private int database = 0;

    private int connectionPoolSize = 64;

    private int connectionMinimumIdleSize = 10;

    private int slaveConnectionPoolSize = 250;

    private int masterConnectionPoolSize = 250;

    private String[] sentinelAddresses;

    private String masterName;

    public void setSentinelAddresses(String sentinelAddresses) {
        this.sentinelAddresses = Arrays.stream(sentinelAddresses.split(",")).map(s -> "redis://" + s).toArray(String[]::new);
    }

    public void setAddress(String address) {
        this.address = "redis://" + address;
    }


    /**
     * 哨兵模式自动装配
     */
    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    @ConditionalOnProperty(name = "redisson.master-name")
    public RedissonLockUtil redissonSentinel() {
        Config config = new Config();
        SentinelServersConfig serverConfig = config.useSentinelServers().addSentinelAddress(this.getSentinelAddresses())
                .setMasterName(this.getMasterName())
                .setTimeout(this.getTimeout())
                .setMasterConnectionPoolSize(this.getMasterConnectionPoolSize())
                .setSlaveConnectionPoolSize(this.getSlaveConnectionPoolSize());

        if (StringUtils.isNotBlank(this.getPassword())) {
            serverConfig.setPassword(this.getPassword());
        }
        RedissonClient redissonClient = Redisson.create(config);
        RedissonLockUtil locker = new RedissonLockUtil();
        locker.setRedissonClient(redissonClient);
        return locker;
    }

    /**
     * 单机模式自动装配
     */
    @Bean
    @ConditionalOnProperty(name = "redisson.address")
    public RedissonLockUtil redissonSingle() {
        Config config = new Config();

        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(this.getAddress())
                .setTimeout(this.getTimeout())
                .setConnectionPoolSize(this.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(this.getConnectionMinimumIdleSize());

        if (StringUtils.isNotBlank(this.getPassword())) {
            serverConfig.setPassword(this.getPassword());
        }
        RedissonClient redissonClient = Redisson.create(config);
        RedissonLockUtil locker = new RedissonLockUtil();
        locker.setRedissonClient(redissonClient);
        return locker;
    }

}
