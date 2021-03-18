# Redisson 3.15.1 Sentinel authentication issue
> This repository aims to help reproduce and analyse an issue with Redis Sentinel authentication
> in [Redisson](https://github.com/redisson/redisson/) 3.15.1. 

## Steps to reproduce

### The issue

* Start the Docker Compose stack using `docker-compose up -d`;
* Compile and execute the project using `mvn compile exec:java`, the expected output is:
```text
$ mvn compile exec:java
// Redacted Maven logs...
18-03-2021 11:08:46.855 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  o.e.r.RedissonSentinelAuthIssue.main - Creating Redisson client...
18-03-2021 11:08:47.793 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  org.redisson.Version.logVersion - Redisson 3.15.1
18-03-2021 11:08:49.859 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  o.r.c.SentinelConnectionManager.<init> - master: redis://127.0.0.1:6379 added
18-03-2021 11:08:49.883 [redisson-netty-2-8] INFO  o.r.c.SentinelConnectionManager.lambda$null$15 - sentinel: redis://127.0.0.1:26379 added
18-03-2021 11:08:49.891 [org.example.redisson.RedissonSentinelAuthIssue.main()] WARN  o.r.c.SentinelConnectionManager.<init> - ReadMode = SLAVE, but slave nodes are not found!
18-03-2021 11:08:50.010 [redisson-netty-2-12] INFO  o.r.c.p.MasterPubSubConnectionPool.lambda$run$0 - 1 connections initialized for /127.0.0.1:6379
18-03-2021 11:08:50.048 [redisson-netty-2-28] INFO  o.r.c.pool.MasterConnectionPool.lambda$run$0 - 24 connections initialized for /127.0.0.1:6379
18-03-2021 11:08:50.091 [redisson-netty-2-29] INFO  o.r.c.pool.PubSubConnectionPool.lambda$run$0 - 1 connections initialized for /127.0.0.1:6379
18-03-2021 11:08:50.117 [redisson-netty-2-15] INFO  o.r.c.pool.SlaveConnectionPool.lambda$run$0 - 24 connections initialized for /127.0.0.1:6379
18-03-2021 11:08:50.156 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  o.e.r.RedissonSentinelAuthIssue.main - Inserting some value in cache...
18-03-2021 11:08:50.191 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  o.e.r.RedissonSentinelAuthIssue.main - Getting some value from cache...
18-03-2021 11:08:50.206 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  o.e.r.RedissonSentinelAuthIssue.main - Value: some-value
18-03-2021 11:08:51.146 [redisson-netty-2-19] ERROR o.r.c.SentinelConnectionManager.checkState - Can't update cluster state
org.redisson.client.RedisException: ERR Client sent AUTH, but no password is set. channel: [id: 0xf21be32b, L:/127.0.0.1:59370 - R:/127.0.0.1:26379] command: (AUTH), params: (password masked)
        at org.redisson.client.handler.CommandDecoder.decode(CommandDecoder.java:345)
        at org.redisson.client.handler.CommandDecoder.decodeCommand(CommandDecoder.java:177)
        at org.redisson.client.handler.CommandDecoder.decode(CommandDecoder.java:116)
        at org.redisson.client.handler.CommandDecoder.decode(CommandDecoder.java:101)
        at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:508)
        at io.netty.handler.codec.ReplayingDecoder.callDecode(ReplayingDecoder.java:366)
        at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:276)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
        at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357)
        at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
        at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919)
        at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166)
        at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:719)
        at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:655)
        at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:581)
        at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:493)
        at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989)
        at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
        at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
        at java.base/java.lang.Thread.run(Thread.java:834)
// The stacktrace keeps repeating itself forever...
```
Just hit `Ctrl+C` to stop the program.

The Redisson Client seems to perform operations just fine in the Redis instance through the Sentinel (read & write), 
but the stacktrace keeps repeating itself. 

`ERR Client sent AUTH, but no password is set.` It seems that Redisson tries to authenticate itself on the Sentinel, 
which at this point does not require any authentication.


### Enable Sentinel authentication

* Destroy the Docker Compose stack using `docker-compose down`
* Go to the [docker-compose.yml](docker-compose.yml) file and uncomment line 21;
* Go to the [RedissonSentinelAuthIssue.java](src/main/java/org/example/redisson/RedissonSentinelAuthIssue.java) file and
switch comment between lines 36 and 37;
* Restart the Docker Compose stack using `docker-compose up -d`;
* Compile and execute the project using `mvn compile exec:java`, the expected output is:
```text
$ mvn compile exec:java
// Redacted Maven logs...
18-03-2021 11:21:18.390 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  o.e.r.RedissonSentinelAuthIssue.main - Creating Redisson client...
18-03-2021 11:21:19.379 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  org.redisson.Version.logVersion - Redisson 3.15.1
18-03-2021 11:21:21.699 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  o.r.c.SentinelConnectionManager.<init> - master: redis://127.0.0.1:6379 added
18-03-2021 11:21:21.723 [redisson-netty-2-7] INFO  o.r.c.SentinelConnectionManager.lambda$null$15 - sentinel: redis://127.0.0.1:26379 added
18-03-2021 11:21:21.728 [org.example.redisson.RedissonSentinelAuthIssue.main()] WARN  o.r.c.SentinelConnectionManager.<init> - ReadMode = SLAVE, but slave nodes are not found!
18-03-2021 11:21:21.849 [redisson-netty-2-8] INFO  o.r.c.p.MasterPubSubConnectionPool.lambda$run$0 - 1 connections initialized for /127.0.0.1:6379
18-03-2021 11:21:21.883 [redisson-netty-2-27] INFO  o.r.c.pool.MasterConnectionPool.lambda$run$0 - 24 connections initialized for /127.0.0.1:6379
18-03-2021 11:21:21.938 [redisson-netty-2-1] INFO  o.r.c.pool.PubSubConnectionPool.lambda$run$0 - 1 connections initialized for /127.0.0.1:6379
18-03-2021 11:21:21.969 [redisson-netty-2-14] INFO  o.r.c.pool.SlaveConnectionPool.lambda$run$0 - 24 connections initialized for /127.0.0.1:6379
18-03-2021 11:21:22.015 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  o.e.r.RedissonSentinelAuthIssue.main - Inserting some value in cache...
18-03-2021 11:21:22.045 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  o.e.r.RedissonSentinelAuthIssue.main - Getting some value from cache...
18-03-2021 11:21:22.062 [org.example.redisson.RedissonSentinelAuthIssue.main()] INFO  o.e.r.RedissonSentinelAuthIssue.main - Value: some-value
// The program keeps running but there is no more stacktrace.
```


### Conclusion

By enabling the authentication on the Sentinel instance, the `ERR Client sent AUTH, but no password is set.` error is no
more to be seen.

But should it really be thrown in the first place? If no Sentinel password is provided to the Redisson configuration,
why does the client seems to send the `AUTH` instruction to the Sentinel anymay?

Looking at Redisson 3.15.1 source it seems unlikely that this is what is happening, but we could not comprehend it
another way:
* Redisson seems to use the provided `password` (not `sentinelPassword`) to check for authentication: https://github.com/redisson/redisson/blob/redisson-3.15.1/redisson/src/main/java/org/redisson/connection/SentinelConnectionManager.java#L208
* If the authentication is indeed required, it toggles the ùsePassword` flag: https://github.com/redisson/redisson/blob/redisson-3.15.1/redisson/src/main/java/org/redisson/connection/SentinelConnectionManager.java#L224
* But the authentication should not be sent if the `sentinelPassword` is `null`: https://github.com/redisson/redisson/blob/redisson-3.15.1/redisson/src/main/java/org/redisson/connection/SentinelConnectionManager.java#L261

Maybe we did not analyse the issue correctly, but we are curious to finally have an explenation on what is going on 
under the hood. :)  