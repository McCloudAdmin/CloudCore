package systems.mythical.cloudcore.console;

public class ConsoleTask {
    private final int id;
    private final String command;
    private final String executeOnServer;
    private final long date;

    public ConsoleTask(int id, String command, String executeOnServer, long date) {
        this.id = id;
        this.command = command;
        this.executeOnServer = executeOnServer;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public String getExecuteOnServer() {
        return executeOnServer;
    }

    public long getDate() {
        return date;
    }
} 