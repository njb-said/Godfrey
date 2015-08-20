package me.imnjb.godfrey.redis;

import me.imnjb.godfrey.Godfrey;
import me.imnjb.godfrey.packet.base.Packet;
import me.imnjb.godfrey.packet.base.PacketType;

import org.json.JSONObject;

import redis.clients.jedis.JedisPubSub;

public class PubSubListener extends JedisPubSub {

    @Override
    public void onMessage(final String channel, final String message) {
        if(!message.trim().isEmpty()) {
            System.out.println("DEBUG:: JSON: " + message + " | Channel: " + channel);

            if(channel.equals("godfrey-comms")) {
                JSONObject json = new JSONObject(message);

                if(json.has("packet") && json.has("data")) {
                    try {
                        PacketType type = PacketType.valueOf(json.getString("packet"));
                        if(json.has("hostname") && (json.getString("hostname").equals(Godfrey.getInstance().getSystemHostname()) || json.getString("hostname").equals("*"))) {
                            Packet packet = PacketType.getPacket(type.name());
                            packet.read(json.getJSONObject("data"));
                            if(Godfrey.isDebug()) {
                                System.out.println("INPUT " + json.getJSONObject("data").toString());//TODO replace with proper logger
                            }
                        }
                    } catch(IllegalArgumentException ex) {
                        Godfrey.log("[ERROR] Invalid packet: " + json.getString("packet"));
                    }
                }
            }
        }
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        if(channel.equalsIgnoreCase("godfrey-comms")) {
            Godfrey.log("[Godfrey] Waiting for commands from you sir. Use the channel godfrey-comms and I will hear you!");
        }
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
    }

}