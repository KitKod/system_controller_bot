import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DefaultCommand {
    public static String CD = "/cd";
    public static String PWD = "/pwd";
    public List<String> command_list;

    public DefaultCommand(){
        this.command_list = new ArrayList<String>();
        this.command_list.add(CD);
        this.command_list.add(PWD);
    }

    public boolean isBelong(String command){
        return this.command_list.contains(command);
    }

    public String processCommand(String command) throws IOException, InterruptedException {
        if (command.equals(CD)) {
            return "cd was processed";
        } else if (command.equals(PWD)) {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh", "-c", "pwd");
            Process process = builder.start();
            StreamGobbler streamGobbler =
//                    new StreamGobbler(process.getInputStream(), System.out::println);
                    new StreamGobbler(process.getInputStream(), this::saveline);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = process.waitFor();
            assert exitCode == 0;
            return "pwd was processed";
        }
        return null;
    }

    public void saveline(String line){
        System.out.println("wrom my " + line);
    }


}
