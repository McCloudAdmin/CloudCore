package systems.mythical.cloudcore.console;

public class ConsoleTask {
    private final int id;
    private final String command;
    private final String executeOn;
    private final String executeOnServer;
    private final long date;

    public ConsoleTask(int id, String command, String executeOn, String executeOnServer, long date) {
        this.id = id;
        this.command = command;
        this.executeOn = executeOn;
        this.executeOnServer = executeOnServer;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public String getExecuteOn() {
        return executeOn;
    }

    public String getExecuteOnServer() {
        return executeOnServer;
    }

    public long getDate() {
        return date;
    }
} 