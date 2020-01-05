public enum Commands {
    UPTIME("/uptime", false, "-p"),
    UPTIME_STAT("/uptime_stat", false),
    PING("/ping", true, "-c 4"),
    LS("/ls", true),
    SEND_FILE("/send_file", true),
    HISTORY("/history", false);

    private String name;
    private boolean isArgs;
    private String defArgs;

    Commands(String name, boolean isArgs) {
        this.name = name;
        this.isArgs = isArgs;
    }

    Commands(String name, boolean isArgs, String defArgs) {
        this.name = name;
        this.isArgs = isArgs;
        this.defArgs = defArgs;
    }

    public String getCommandName() {
        return this.name;
    }

    public String getCanonicalName() {
        return this.name.replaceFirst("/", "");
    }

    public boolean isArgs() {
        return this.isArgs;
    }

    public String getDefArgs() {
        return this.defArgs;
    }
}
