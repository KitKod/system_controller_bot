public enum Commands {
    UPTIME("/uptime", false),
    PING("/ping", true);

    private String name;
    private boolean isArgs;

    Commands(String name, boolean isArgs) {
        this.name = name;
        this.isArgs = isArgs;
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
}
