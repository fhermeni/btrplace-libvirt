package btrplace.actuator.libvirt;

import btrplace.executor.ExecutorException;
import btrplace.plan.event.Action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Fabien Hermenier
 */
public final class Utils {

    public static String getOutput(Process p) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder buf = new StringBuilder();
        String line = "";
        while ((line = reader.readLine())!= null) {
            buf.append(line).append("\n");
        }
        return buf.toString();
    }

    public static void execute(Action a, String cmd, int ret) throws ExecutorException {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            int r = p.waitFor();
            if (ret != r) {
                throw new ExecutorException("Error while running " + a + ". Exit code=" + r + "\n" + getOutput(p), null);
            }
        } catch (InterruptedException | IOException e) {
            throw new ExecutorException("Error while running " + a, e);
        }
    }
}
