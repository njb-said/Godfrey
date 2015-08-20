package me.imnjb.godfrey.packet;

import me.imnjb.godfrey.packet.base.Packet;
import me.imnjb.godfrey.packet.base.PacketType;

import org.json.JSONObject;

/**
 * Packet used to signal that a command has been executed
 *
 * @author njb_said
 */
public class CommandCompletePacket extends Packet {

    // use unique token which is time + command to keep things in sync
    private final String hashId;
    private final String command;
    private final int statusId;

    public CommandCompletePacket() {
        this.command = null;
        this.hashId = null;
        this.statusId = -1;
    }

    public CommandCompletePacket(String command, String hashId, int statusId) {
        this.command = command;
        this.hashId = hashId;
        this.statusId = statusId;
    }

    @Override
    public void write(JSONObject object) {
        if(hashId != null) {
            object.put("command", command);
            object.put("hashId", hashId);
            object.put("statusId", statusId);
            object.put("time", System.currentTimeMillis());
        }
    }

    @Override
    public void read(JSONObject object) {
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.COMMANDCOMPLETE;
    }

}
