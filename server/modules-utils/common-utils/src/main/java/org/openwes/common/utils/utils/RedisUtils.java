package org.openwes.common.utils.utils;

import org.openwes.common.utils.constants.MarkConstant;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.redisson.api.listener.MessageListener;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnClass(RedissonClient.class)
@RequiredArgsConstructor
public class RedisUtils {

    private final RedissonClient redissonClient;

    public void set(String key, String value) {
        redissonClient.getBucket(generateKey(key)).set(value);
    }

    public void set(String key, String value, long expires) {
        set(key, value, expires, TimeUnit.SECONDS);
    }

    public void set(String key, String value, long expires, TimeUnit timeUnit) {
        redissonClient.getBucket(generateKey(key)).set(value, expires, timeUnit);
    }

    public String get(String key) {
        Object object = redissonClient.getBucket(generateKey(key)).get();
        if (object == null) {
            return null;
        }
        return object.toString();
    }

    public void delete(String key) {
        redissonClient.getBucket(generateKey(key)).delete();
    }

    public void close() {
        redissonClient.shutdown();
    }

    public void publish(String topic, Object message) {
        redissonClient.getTopic(topic, new JsonJacksonCodec()).publish(message);
    }

    public <T> void listen(String topic, Class<T> type, MessageListener<T> listener) {
        redissonClient.getTopic(topic, new JsonJacksonCodec()).addListener(type, listener);
    }

    /**
     * lock
     *
     * @param lockKey
     * @return
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(generateKey(lockKey));
    }

    public RLock getLock(List<String> lockKeys) {
        List<RLock> locks = lockKeys.stream().map(this::getLock).toList();
        return redissonClient.getMultiLock(locks.toArray(new RLock[0]));
    }

    /**
     * redis get and increment
     *
     * @param key
     * @param cacheSize
     * @return
     */
    public long getAndIncrement(String key, int cacheSize) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(generateKey(key));
        Duration ofDays = Duration.ofDays(1);
        if (!atomicLong.expireIfSet(ofDays)) {
            atomicLong.expire(ofDays);
        }
        return atomicLong.getAndAdd(cacheSize);
    }

    /**
     * List push
     *
     * @param key
     * @param t
     * @param <T>
     */
    public <T> void push(String key, T t) {
        redissonClient.getList(generateKey(key)).add(t);
    }

    /**
     * Lst getAll
     *
     * @param key
     * @param <T>
     * @return
     */
    public <T> List<T> getList(String key) {
        RList<T> rList = redissonClient.getList(generateKey(key));
        return rList.range(rList.size());
    }

    public <T> List<T> getListByPureKey(String key) {
        RList<T> rList = redissonClient.getList(key);
        return rList.range(rList.size());
    }

    public <T> List<T> getListByPureKey(String key, int size) {
        RList<T> rList = redissonClient.getList(key);
        return rList.range(size);
    }

    /**
     * List remove list
     *
     * @param key
     * @param removeList
     * @param <T>
     */
    public <T> void removeList(String key, List<T> removeList) {
        RList<T> rList = redissonClient.getList(generateKey(key));
        rList.removeAll(removeList);
    }

    public <T> void removeListByPureKey(String key, List<T> removeList) {
        RList<T> rList = redissonClient.getList(key);
        rList.removeAll(removeList);
    }

    /**
     * List add list
     *
     * @param key
     * @param newList
     * @param <T>
     */
    public <T> void pushAll(String key, List<T> newList) {
        redissonClient.getList(generateKey(key)).addAll(newList);
    }


    private String generateKey(String key) {
        return generateKey("", key);
    }

    public static String generateKey(String tenant, String key) {
        return tenant + MarkConstant.COLON_MARK + key;
    }

    public static String generateKeysPatten(String tenant, String key) {
        return generateKey(tenant, key) + "*";
    }

    public List<String> keys(String pattern) {
        return redissonClient.getKeys().getKeysStreamByPattern(pattern).toList();
    }

    public void hSet(String redisKey, String value) {
        RSet<String> rSet = redissonClient.getSet(redisKey);
        rSet.add(value);
    }

    public Set<String> hGet(String redisKey) {
        RSet<String> rSet = redissonClient.getSet(redisKey);
        return rSet == null ? null : rSet.readAll();
    }

    public void hRemove(String redisKey, String value) {
        RSet<String> rSet = redissonClient.getSet(redisKey);
        rSet.remove(value);
    }


    public RMap<String, Object> hGetMap(String redisKey) {
        return redissonClient.getMap(redisKey);
    }

}


