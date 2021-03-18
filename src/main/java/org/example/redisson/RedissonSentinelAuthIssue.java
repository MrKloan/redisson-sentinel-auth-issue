package org.example.redisson;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedissonSentinelAuthIssue {

    private static final Logger logger = LoggerFactory.getLogger(RedissonSentinelAuthIssue.class);

    public static void main(final String[] args) {
        logger.info("Creating Redisson client...");
        final RedissonClient redissonClient = Redisson.create(sentinelConfiguration());

        logger.info("Inserting some value in cache...");
        redissonClient.getBucket("some-key").set("some-value");

        logger.info("Getting some value from cache...");
        final String value = (String) redissonClient.getBucket("some-key").get();
        logger.info("Value: {}", value);
    }

    private static Config sentinelConfiguration() {
        final Config config = new Config();

        config.useSentinelServers()
                .setCheckSentinelsList(false)
                .setMasterName("redis-master-set")
                .addSentinelAddress("redis://127.0.0.1:26379")
                .setTimeout(3000)
                .setConnectTimeout(1000)
                .setPassword("pass1234")
                // Switch comments between those 2 lines to enable Sentinel authentication.
                .setSentinelPassword(null); // This line is optional and could be removed to reproduce the issue. Its sole purpose is to ensure that a `null` reference is passed to the Redisson configuration, hence no Sentinel password is provided. Feel free to test it for yourself.
                //.setSentinelPassword("pass1234");

        return config;
    }
}
