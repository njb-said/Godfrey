package me.imnjb.godfrey.command;

import me.imnjb.godfrey.Godfrey;
import me.imnjb.godfrey.packet.ResponseLinePacket;

public class RedisLogListener implements LogListener {

    @Override
    public void onLine(String command, String line, boolean error, String hashId) {
        Godfrey.sendPacket(new ResponseLinePacket(line, command, error, hashId));
    }

}
