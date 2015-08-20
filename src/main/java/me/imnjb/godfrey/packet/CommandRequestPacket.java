package me.imnjb.godfrey.packet;

import me.imnjb.godfrey.Godfrey;
import me.imnjb.godfrey.command.CommandExecutor;
import me.imnjb.godfrey.command.RedisLogListener;
import me.imnjb.godfrey.packet.base.Packet;
import me.imnjb.godfrey.packet.base.PacketType;

import org.json.JSONObject;

/**
 * Packet used to request a command execution
 *
 * @author njb_said
 */
public class CommandRequestPacket extends Packet {

    @Override
    public void write(JSONObject object) {
    }

    @Override
    public void read(JSONObject object) {
        if(object.has("command") && object.has("hashId")) {
            CommandExecutor.run(object.getString("command"), new RedisLogListener(), object.getString("hashId"));
        } else {
            Godfrey.log("[ERROR] [Godfrey] Did not run command: invalid packet");
        }
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.COMMANDREQUEST;
    }

}
