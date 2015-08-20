package me.imnjb.godfrey.redis;

import me.imnjb.godfrey.Godfrey;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class PubSubHandler implements Runnable {

    private Redis redis;
    private Jedis jedis;
    private PubSubListener listener;

    public PubSubHandler(Redis instance) {
        this.redis = instance;
        new Thread(this).start();
    }

    @Override
    public void run() {
        jedis = redis.getPool().getResource();
        listener = new PubSubListener();
        try {
            jedis.subscribe(listener, "godfrey-comms");
        } catch(JedisConnectionException ex) {
            if(Godfrey.isDebug()) {
                ex.printStackTrace();
            }
            Godfrey.log("[System] " + ex.getMessage());
            Godfrey.log("[Godfrey] Looks like your redis server went away");
            Godfrey.log("[Godfrey] I am going to shutdown now");
            System.exit(-1);
        }
    }

    public void close() {
        listener.unsubscribe();
    }

}
