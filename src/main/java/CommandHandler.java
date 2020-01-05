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
        if (command.startsWith("/")) {
            command = command.replaceFirst("/", "");
        }
        builder.command("sh", "-c", command + " " + args);
        System.out.println("Do command = " + "sh -c " + command + " " + args);
        Process process = builder.start();
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), this::saveResultIntoFile);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
        Thread.sleep(500);
        return this.getResultOfCommand();
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
