package btrplace.actuator.libvirt;

import btrplace.executor.ExecutorException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utility to send command through libvirt and the shell.
 * @author Fabien Hermenier
 */
public final class LibVirt {

    public static void boot(Protocol proto, String host, String vm) {
        throw new UnsupportedOperationException();
    }

    public static enum Protocol {QEMU_TCP}

    public static final String VIRSH_CONNECT = "virsh --connect ";
    private LibVirt() {}

    public static void shutdown(Protocol p, String node, String vm) throws ExecutorException {
        //TODO: blocking or not ?
        String cmd = VIRSH_CONNECT + uri(p, node) + " shutdown " + vm;
        exec(cmd, 0);
    }

    public static void destroy(Protocol p, String node, String vm) throws ExecutorException {
        String cmd = VIRSH_CONNECT + uri(p, node) + " destroy " + vm;
        exec(cmd, 0);
    }

    public static void migrate(Protocol p, String src, String vm, String dst, boolean live) throws ExecutorException {
        StringBuilder cmd = new StringBuilder(VIRSH_CONNECT);
        cmd.append(uri(p, src)).append("migrate ");
        if (live) {
            cmd.append("--live ");
        }
        cmd.append(uri(p, dst));
        exec(cmd.toString(), 0);
    }

    private static String uri(Protocol p, String ip){
        switch (p) {
            case QEMU_TCP: return "qemu+tcp://" + ip + "/system";
        }
        throw new UnsupportedOperationException();
    }

    private static void exec(String cmd, int ret) throws ExecutorException {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            int r = p.waitFor();
            if (ret != 0) {
                throw new ExecutorException("'" + cmd + "' failed. Exit code=" + r + "\n" + getOutput(p), null);
            }
        } catch (InterruptedException | IOException e) {
            throw new ExecutorException("'" + cmd + "' failed", e);
        }
    }

    private static String getOutput(Process p) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder buf = new StringBuilder();
        String line = "";
        while ((line = reader.readLine())!= null) {
            buf.append(line).append("\n");
        }
        return buf.toString();
    }
}
