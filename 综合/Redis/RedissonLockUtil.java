package com.gree.ecommerce.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @author A80080
 * @createDate 2021/1/18
 */
public class RedissonLockUtil {

    private static RedissonClient redissonClient;

    public void setRedissonClient(RedissonClient redissonCli) {
        redissonClient = redissonCli;
    }


    public static RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        return lock;
    }

    public static RLock lock(String lockKey, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, TimeUnit.SECONDS);
        return lock;
    }

    public static RLock lock(String lockKey, TimeUnit unit, int timeout) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(timeout, unit);
        return lock;
    }

    /**
     * 尝试获得锁,自定义 时间单位;等待时长;加锁时长
     */
    public static boolean tryLock(String lockKey, int waitTime, int leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }


    /**
     * 尝试获得锁,自定义 等待时长;加锁时长 (默认为秒)
     */
    public static boolean tryLock(String lockKey, int waitTime, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 尝试获得锁,自定义 等待时长 (加锁时长不限制;默认为秒)
     */
    public static boolean tryLock(String lockKey, int waitTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 尝试获得锁,自定义 等待时长 (加锁时长不限制)
     */
    public static boolean tryLock(String lockKey, int waitTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public static void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.unlock();
    }

    public static void unlock(RLock lock) {
        lock.unlock();
    }


}
