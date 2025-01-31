/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.remoting.redis.support;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.remoting.redis.RedisClient;

import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractRedisClient implements RedisClient {

    private final URL url;

    private final JedisPoolConfig config;

    private GenericObjectPoolConfig<Connection> poolConfig;

    public AbstractRedisClient(URL url) {
        this.url = url;
        config = new JedisPoolConfig();
        poolConfig = new GenericObjectPoolConfig<>();
        config.setTestOnBorrow(url.getParameter("test.on.borrow", true));
        poolConfig.setTestOnBorrow(url.getParameter("test.on.borrow", true));
        config.setTestOnReturn(url.getParameter("test.on.return", false));
        poolConfig.setTestOnReturn(url.getParameter("test.on.return", false));
        config.setTestWhileIdle(url.getParameter("test.while.idle", false));
        poolConfig.setTestOnBorrow(url.getParameter("test.on.borrow", false));
        if (url.getParameter("max.idle", 0) > 0) {
            config.setMaxIdle(url.getParameter("max.idle", 0));
            poolConfig.setMaxIdle(url.getParameter("max.idle", 0));
        }
        if (url.getParameter("min.idle", 0) > 0) {
            config.setMinIdle(url.getParameter("min.idle", 0));
            poolConfig.setMinIdle(url.getParameter("min.idle", 0));
        }
        if (url.getParameter("max.active", 0) > 0) {
            config.setMaxTotal(url.getParameter("max.active", 0));
            poolConfig.setMaxTotal(url.getParameter("max.active", 0));
        }
        if (url.getParameter("max.total", 0) > 0) {
            config.setMaxTotal(url.getParameter("max.total", 0));
            poolConfig.setMaxTotal(url.getParameter("max.total", 0));
        }
        if (url.getParameter("max.wait", url.getParameter("timeout", 0)) > 0) {
            Duration maxWaitMillis = Duration.ofMillis(url.getParameter("timeout", 0));
            config.setMaxWait(maxWaitMillis);
            poolConfig.setMaxWait(maxWaitMillis);
        }
        if (url.getParameter("num.tests.per.eviction.run", 0) > 0) {
            config.setNumTestsPerEvictionRun(url.getParameter("num.tests.per.eviction.run", 0));
            poolConfig.setNumTestsPerEvictionRun(url.getParameter("num.tests.per.eviction.run", 0));
        }
        if (url.getParameter("time.between.eviction.runs.millis", 0) > 0) {
            Duration timeBetweenEvictionRunsMillis = Duration.ofMillis(url.getParameter("time.between.eviction.runs.millis", 0));
            config.setTimeBetweenEvictionRuns(timeBetweenEvictionRunsMillis);
            poolConfig.setTimeBetweenEvictionRuns(timeBetweenEvictionRunsMillis);
        }
        if (url.getParameter("min.evictable.idle.time.millis", 0) > 0) {
            Duration minEvictableIdleTimeMillis = Duration.ofMillis(url.getParameter("min.evictable.idle.time.millis", 0));
            config.setMinEvictableIdleTime(minEvictableIdleTimeMillis);
            poolConfig.setMinEvictableIdleTime(minEvictableIdleTimeMillis);
        }
    }

    protected Set<String> scan(Jedis jedis, String pattern) {
        Set<String> result = new HashSet<>();
        String cursor = ScanParams.SCAN_POINTER_START;
        ScanParams params = new ScanParams();
        params.match(pattern);
        while (true) {
            ScanResult<String> scanResult = jedis.scan(cursor, params);
            List<String> list = scanResult.getResult();
            if (CollectionUtils.isNotEmpty(list)) {
                result.addAll(list);
            }
            if (ScanParams.SCAN_POINTER_START.equals(scanResult.getCursor())) {
                break;
            }
            cursor = scanResult.getCursor();
        }
        return result;
    }

    public URL getUrl() {
        return url;
    }

    public JedisPoolConfig getConfig() {
        return config;
    }

    public GenericObjectPoolConfig<Connection> getPoolConfig() {
        return poolConfig;
    }
}
