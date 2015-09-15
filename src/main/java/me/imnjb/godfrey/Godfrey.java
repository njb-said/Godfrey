package me.imnjb.godfrey;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import me.imnjb.godfrey.command.CommandExecutor;
import me.imnjb.godfrey.command.LogListener;
import me.imnjb.godfrey.packet.base.Packet;
import me.imnjb.godfrey.redis.Redis;
import me.imnjb.godfrey.redis.Redis.RedisQuery;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * A command butler
 * <p>A bit cheesy but whatever, that's life.
 *
 * @author njb_said
 */
public class Godfrey {

    @Getter private static Godfrey instance;
    @Getter private static ExecutorService executor;

    public static void main(String[] args) {
        executor = Executors.newFixedThreadPool(2);
        try {
            instance = new Godfrey(args);
        } catch(NumberFormatException ex) {
            log("'" + args[1] + "' is not a valid port");
        }
    }

    @Getter private final Redis redis;
    @Getter private String systemHostname;
    @Getter private static boolean debug, cachingLog;
    private static Date STARTUP = new Date();

    public Godfrey(String[] args) throws IllegalArgumentException, NumberFormatException {
        log("[System] Validating arguments..");

        if(args.length >= 2) {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            if(port < 0 || port > 65536) {
                throw new IllegalArgumentException(port + " is not a valid port. Must be between 0 and 65536");
            }
            String auth = args.length < 3 ? null : (args[2].equals("null") || args[2].equals("none") ? null : args[2]);
            cachingLog = args.length < 4 ? true : Boolean.parseBoolean(args[3]);// Defaults to enabled
            debug = args.length < 5 ? false : Boolean.parseBoolean(args[4]);

            log("[Godfrey] Good day sir!");
            log("[Godfrey] I am godfrey, a server management application that allows you to send commands to multiple servers all at once.");
            log("[Godfrey] Think of me as... the butler of linux servers!");
            log("[Godfrey] I shall get to work immediately!");
            log("[System] Connecting to redis..");

            this.redis = new Redis(host, port, auth);

            log("[System] Obtaining hostname");
            CommandExecutor.run("hostname", new LogListener() {
                public void onLine(String command, String line, boolean error, String hashId) {
                    if(command.equals("hostname") && !error) {
                        systemHostname = line;
                        log("[System] Hostname read as: " + line);
                    }
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    redis.disconnect();
                }
            }));
        } else {
            throw new IllegalArgumentException("You must include at least 2 arguments!");
        }
    }

    public static void log(String message) {
        if(message.startsWith("[ERROR] ")) {
            System.err.println(getTime(false) + message);
        } else {
            System.out.println(getTime(false) + message);
        }
    }

    /**
     * Send a packet
     * <p>Attributes like machine hostname will
     * be appended automatically
     *
     * @param packet packet to send
     */
    public static void sendPacket(final Packet packet) {
        final JSONObject data = new JSONObject();
        packet.write(data);
        if(data.length() <= 0) {
            Godfrey.log("[System] Not sending " + packet.getPacketType().name() + ", no data in object.");
            return;
        }

        getInstance().getRedis().work(new RedisQuery() {
            public void doAction(Jedis jedis, JedisPool pool) {
                sendPacket(data, packet, jedis);
            }
        });
    }

    /**
     * Send a packet using a certain jedis instance
     *
     * @param data data to send
     * @param packet packet to send
     * @param jedis jedis instance to use
     */
    public static void sendPacket(JSONObject data, Packet packet, Jedis jedis) {
        if(data == null) {
            data = new JSONObject();
            packet.write(data);

            if(data.length() <= 0) {
                Godfrey.log("[System] Not sending " + packet.getPacketType().name() + ", no data in object.");
                return;
            }
        }

        JSONObject obj = new JSONObject();
        obj.put("packet", packet.getPacketType().name());
        obj.put("machine", getInstance().getSystemHostname());
        obj.put("data", data);
        if(debug) {
            System.out.println("OUTPUT " + data.toString());//TODO replace with proper logger
        }
        jedis.publish("godfrey-comms", obj.toString());
    }

    public static String getTime(boolean date) {
        return "[" + new SimpleDateFormat((date ? "dd/MM/yyyy " : "") + "HH:mm:ss").format(new Date()) + "] ";
    }

    public static void toFile(String message) {
        try {
            File file = new File("logs", new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(STARTUP) + ".log");
            file.getParentFile().mkdir();
            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(getTime(true) + message + "\n");
            fileWriter.flush();
            fileWriter.close();
        } catch(IOException ex) {
            System.err.println(getTime(false) + "Unable to log to file: " + ex.getLocalizedMessage());
        }
    }

}
