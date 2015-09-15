package me.imnjb.godfrey.redis;

import java.util.concurrent.*;

import lombok.Getter;
import me.imnjb.godfrey.Godfrey;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

public @Getter class Redis {

    private JedisPool pool;
    private PubSubHandler pubSub;

    public Redis(final String host, final int port, final String key) {
        FutureTask<JedisPool> task = new FutureTask<>(new Callable<JedisPool>() {
            public JedisPool call() throws Exception {
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(12);
                config.setJmxEnabled(false);

                if(key == null) {
                    return new JedisPool(config, host, port, 0);
                } else {
                    return new JedisPool(config, host, port, 0, key);
                }
            }
        });

        Godfrey.getExecutor().execute(task);

        try {
            pool = task.get();
        } catch(InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            return;
        }

        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Godfrey.log("[System] PING: " + jedis.ping());
        } catch(JedisConnectionException ex) {
            ex.printStackTrace();

            if(jedis != null) {
                pool.returnBrokenResource(jedis);
            }

            pool.destroy();
            pool = null;
            jedis = null;
            return;
        }

        if(jedis != null && jedis.isConnected()) {
            pubSub = new PubSubHandler(this);
        }
    }

    public void disconnect() {
        if(pool != null && !pool.isClosed()) {
            Jedis jedis = pool.getResource();
            pubSub.close();

            jedis.disconnect();
            jedis.close();

            pool.destroy();
            pool = null;
        }
    }

    public void work(RedisQuery query) {
        Jedis tmpRsc = pool.getResource();

        try {
            query.doAction(tmpRsc, pool);
        } finally {
            pool.returnResource(tmpRsc);
        }
    }

    public void work(RedisQuery query, Jedis jedis) {
        if(jedis == null) {
            work(query);
        } else {
            query.doAction(jedis, pool);
        }
    }

    public static abstract class RedisQuery {

        public abstract void doAction(Jedis jedis, JedisPool pool);

    }

    public boolean isConnected() {
        return pool != null;
    }

}
