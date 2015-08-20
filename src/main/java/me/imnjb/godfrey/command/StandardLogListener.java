package me.imnjb.godfrey.command;

import me.imnjb.godfrey.Godfrey;

public class StandardLogListener implements LogListener {

    @Override
    public void onLine(String command, String line, boolean error, String hashId) {
        String cmd = command.split("\\s+")[1];
        if(error) {
            Godfrey.log("[ERROR] [RESPONSE] (" + cmd + ") : " + line);
        } else {
            Godfrey.log("[RESPONSE] (" + cmd + ") : " + line);
        }
    }

}
