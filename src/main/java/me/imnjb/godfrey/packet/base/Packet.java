package me.imnjb.godfrey.packet.base;

import org.json.JSONObject;

/**
 * Represents a packet of data sent or received
 */
public abstract class Packet {

    public abstract void write(JSONObject object);
    public abstract void read(JSONObject object);

    public abstract PacketType getPacketType();

}