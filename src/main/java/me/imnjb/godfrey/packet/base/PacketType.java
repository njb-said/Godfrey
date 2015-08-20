package me.imnjb.godfrey.packet.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.imnjb.godfrey.packet.CommandCompletePacket;
import me.imnjb.godfrey.packet.CommandRequestPacket;
import me.imnjb.godfrey.packet.ResponseLinePacket;

public @AllArgsConstructor @Getter enum PacketType {

    COMMANDCOMPLETE(CommandCompletePacket.class),
    COMMANDREQUEST(CommandRequestPacket.class),
    RESPONSELINE(ResponseLinePacket.class);

    private Class<? extends Packet> packet;

    public static Packet getPacket(String name) {
        try {
            return valueOf(name.toUpperCase()).getPacket().newInstance();
        } catch(IllegalArgumentException ex) {
        } catch(InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}