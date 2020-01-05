import java.io.*;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class CommandHandler {

    public CommandHandler(){}

    public boolean cmdWithArgs(String command){
        Commands[] commands = Commands.values();
        for (Commands cmd : commands) {
            if (cmd.getCommandName().equals(command) && cmd.isArgs()) {
                return true;
            }
        }
        return false;
    }

    public String processCommand(String command, String args) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        String cmd = this.formatCommand(command, args);
        builder.command("sh", "-c", cmd);
        System.out.println("Do command = " + "sh -c " + cmd);
        Process process = builder.start();
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), this::saveResultIntoFile);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
        Thread.sleep(500);
        return this.getResultOfCommand();
    }

    private String formatCommand(String command, String args) {
        System.out.println("Format command " + command);
        String separator = " ";
        if (Commands.PING.getCommandName().equals(command)){
            System.out.println("Format ping");
            return Commands.PING.getCanonicalName() +
                    separator +
                    Commands.PING.getDefArgs() +
                    separator +
                    args;
        } else if (Commands.UPTIME.getCommandName().equals(command)) {
            return Commands.UPTIME.getCanonicalName() +
                    separator +
                    Commands.UPTIME.getDefArgs();
        } else {
            if (command.startsWith("/")) {
                command = command.replaceFirst("/", "");
            }
            System.out.println("Without Formating");
            return command + " " + args;
        }
    }

    public void saveResultIntoFile(String line){
        System.out.println("Result of command is saved to file = " + line);
        try(FileWriter writer = new FileWriter("src/main/resources/output.txt", true))
        {
            // запись всей строки
            writer.write(line + "\n");
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    private String getResultOfCommand() {
        StringBuilder builder = new StringBuilder();
        try(FileReader reader = new FileReader("src/main/resources/output.txt")){
            Scanner scan = new Scanner(reader);
            while (scan.hasNextLine()) {
                builder.append(scan.nextLine());
                builder.append("\n");
            }
            reader.close();
            FileWriter writer = new FileWriter("src/main/resources/output.txt", false);
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Readed from file = " + builder.toString());
        return builder.toString();
    }
}
