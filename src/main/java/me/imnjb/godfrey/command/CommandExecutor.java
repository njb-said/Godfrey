package me.imnjb.godfrey.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import me.imnjb.godfrey.Godfrey;
import me.imnjb.godfrey.ValContainer;
import me.imnjb.godfrey.packet.CommandCompletePacket;
import me.imnjb.godfrey.redis.Redis.RedisQuery;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Class used to execute commands or scripts
 *
 * @see StreamReader
 */
public class CommandExecutor {

    /**
     * Class that command responses are read within
     */
    public static class StreamReader implements Runnable {

        private final String command, hashId;
        private final InputStream input;
        private final LogListener output;
        private final boolean error;

        public StreamReader(String command, String hashId, InputStream stream, LogListener output, boolean error) {
            this.command = command;
            this.hashId = hashId;
            this.input = stream;
            this.output = output;
            this.error = error;
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            try {
                while((line = reader.readLine()) != null) {
                    if(output != null) {
                        output.onLine(command, line, error, hashId);
                    }
                }
                reader.close();
            } catch(IOException ex) {
                output.onLine(command, "Command encountered an error", true, hashId);
                Godfrey.log("[ERROR] Command encountered an error: " + command);
                ex.printStackTrace();
            }
        }

    }

    public static int run(final String command, final LogListener logListener, final String hash) {
        final ValContainer<Integer> status = new ValContainer<Integer>(0);

        new Thread() {
            public void run() {
                try {
                    Godfrey.log("[Godfrey] (Executing) " + command);

                    Process process = Runtime.getRuntime().exec(command);

                    if(logListener != null) {
                        new Thread(new StreamReader(command, hash, process.getInputStream(), logListener, false)).start();
                        new Thread(new StreamReader(command, hash, process.getErrorStream(), logListener, true)).start();
                    }

                    status.setValue(process.waitFor());

                    if(hash != null && !hash.isEmpty()) {
                        Godfrey.getInstance().getRedis().work(new RedisQuery() {
                            public void doAction(Jedis jedis, JedisPool pool) {
                                jedis.srem("godfrey:running", hash);// the web ui will use this TODO
                                Godfrey.sendPacket(null, new CommandCompletePacket(command, hash, status.getValue()), jedis);
                                Godfrey.log("[System] Removed " + command + " (" + hash + ") from godfrey:running");
                            }
                        });
                    }
                } catch(InterruptedException | IOException ex) {
                    ex.printStackTrace();
                    status.setValue(-1);
                }
            }
        }.start();

        return status.getValue();
    }

    public static int run(String command) {
        return run(command, new StandardLogListener(), null);
    }

    public static int run(String command, LogListener logListener) {
        return run(command, logListener, null);
    }

    public static int run(String command, String hash) {
        return run(command, new StandardLogListener(), hash);
    }

    public static int runScript(String script, String args, LogListener logListener, String hash) {
        return run("sh " + script + (script.endsWith(".sh") ? "" : ".sh") + " " + args, logListener, hash);
    }

    public static int runScript(String script, String args, LogListener logListener) {
        return runScript(script, args, logListener, null);
    }

    public static int runScript(String script, String args) {
        return runScript(script, args, null, null);
    }

}
