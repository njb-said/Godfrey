package me.imnjb.godfrey.command;

public interface LogListener {

    /**
     * Called whenever a line is read
     * <p>hashId parameter will be null if not running over redis
     * else will be the equivelant to the command and time begun, hashed.
     *
     * @param command the command this line is from
     * @param line the line
     * @param error if the line was in the error stream or not
     * @param hashId command id if the command is being run over redis
     */
    public void onLine(String command, String line, boolean error, String hashId);

}
