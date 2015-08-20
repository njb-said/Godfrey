package me.imnjb.godfrey.packet;

import me.imnjb.godfrey.packet.base.Packet;
import me.imnjb.godfrey.packet.base.PacketType;

import org.json.JSONObject;

/**
 * Packet used to send a line of command response
 *
 * @author njb_said
 */
public class ResponseLinePacket extends Packet {

    // use unique token which is time + command to keep things in sync
    private final String hashId;
    private final String line, command;
    private final boolean error;

    public ResponseLinePacket() {
        this.line = null;
        this.command = null;
        this.error = false;
        this.hashId = null;
    }

    public ResponseLinePacket(String line, String command, boolean error, String hashId) {
        this.line = line;
        this.command = command;
        this.error = error;
        this.hashId = hashId;
    }

    @Override
    public void write(JSONObject object) {
        if(line != null) {
            object.put("line", line);
            object.put("command", command);
            object.put("error", error);
            object.put("hashId", hashId);
        }
    }

    @Override
    public void read(JSONObject object) {
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.RESPONSELINE;
    }

}
