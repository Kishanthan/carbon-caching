/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.caching;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;


/**
 * Base application
 */
public class AbstractApp {
    private static Duration TEN_SEC = new Duration(TimeUnit.SECONDS, 10);

    static {
        final String logging = "hazelcast.logging.type";
        if (System.getProperty(logging) == null) {
            System.setProperty(logging, "jdk");
        }

        System.setProperty("hazelcast.version.check.enabled", "false");
        System.setProperty("hazelcast.mancenter.enabled", "false");
        System.setProperty("hazelcast.wait.seconds.before.join", "1");
        System.setProperty("hazelcast.local.localAddress", "127.0.0.1");
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("hazelcast.jmx", "true");

        // randomize multicast group...
        Random rand = new Random();
        int g1 = rand.nextInt(255);
        int g2 = rand.nextInt(255);
        int g3 = rand.nextInt(255);
        System.setProperty("hazelcast.multicast.group", "224." + g1 + "." + g2 + "." + g3);
        System.setProperty("hazelcast.jcache.provider.type", "server");
    }

    protected final URI uri1 = new File("jcache/src/main/resources/hazelcast-client-c1.xml").toURI();
    protected final URI uri2 = new File("jcache/src/main/resources/hazelcast-client-c2.xml").toURI();

    protected CachingProvider cachingProvider;

    /**
     * initialize the JCache Manager that we will use for creating and getting a cache object
     */
    public CacheManager initCacheManager(URI uri) {
        //resolve a cache manager
        cachingProvider = Caching.getCachingProvider();
        return cachingProvider.getCacheManager(uri, null);
    }

    public CacheManager initCacheManager() {
        return initCacheManager(null);
    }

    /**
     * we initialize a cache with name
     *
     * @param name
     */
    public Cache<String, Integer> initCache(String name, CacheManager cacheManager) {

        //configure the cache
        MutableConfiguration<String, Integer> config = new MutableConfiguration<String, Integer>();
        config.setStoreByValue(true)
                .setTypes(String.class, Integer.class)
                .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(TEN_SEC))
                .setStatisticsEnabled(false);

        //create the cache
        return cacheManager.createCache(name, config);
    }

    /**
     * we initialize a cache with name
     *
     * @param duration
     * @param name
     */
    public Cache<String, Integer> initCache(String name, CacheManager cacheManager, Duration duration) {

        //configure the cache
        MutableConfiguration<String, Integer> config = new MutableConfiguration<String, Integer>();
        config.setStoreByValue(true)
                .setTypes(String.class, Integer.class)
                .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(duration))
                .setStatisticsEnabled(false);
        if (cacheManager.getCache(name, String.class, Integer.class) != null) {
            //cache should not exist so I will destroy it
            cacheManager.destroyCache(name);
        }
        //create the cache
        return cacheManager.createCache(name, config);
    }

    /**
     * we populate cache with (theKey-i, i )
     */
    public void populateCache(Cache<String, Integer> cache) {
        if (cache != null) {
            for (int i = 0; i < 10; i++) {
                cache.put("theKey-" + i, i);
            }
        }
    }

    /**
     * print all of the content of the cache, if expires or not exist you will see a null value
     */
    public void printContent(Cache<String, Integer> cache) {
        System.out.println("==============>  " + cache.getName() + "@ URI:" + cache.getCacheManager().getURI() + "  <=====================");
        for (int i = 0; i < 10; i++) {
            final String key = "theKey-" + i;
            System.out.println("Key: " + key + ", Value: " + cache.get(key));
        }
        System.out.println("============================================================");
    }

    /**
     * print all of the content of the cache using Iterator, if expires or not exist you will see no value
     */
    public void printContentWithIterator(Cache<String, Integer> cache) {
        System.out.println("==============>  " + cache.getName() + "  <=====================");
        final Iterator<Cache.Entry<String, Integer>> iterator = cache.iterator();
        while (iterator.hasNext()) {
            final Cache.Entry<String, Integer> next = iterator.next();
            System.out.println("Key: " + next.getKey() + ", Value: " + next.getValue());
        }
        System.out.println("============================================================");
    }

    public void sleepFor(long duration)
            throws InterruptedException {
        Thread.sleep(duration);
    }

    public void clientSetup() {
        System.setProperty("hazelcast.jcache.provider.type", "client");
    }

    /**
     * closing the cache manager we started
     */
    public void shutdown() {
        cachingProvider.close();

    }
}
