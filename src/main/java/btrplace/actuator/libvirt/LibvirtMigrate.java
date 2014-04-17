package btrplace.actuator.libvirt;

import btrplace.executor.Actuator;
import btrplace.executor.ExecutorException;
import btrplace.plan.event.MigrateVM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Actuator to perform the migration of a VM between two nodes.
 * The migration process can be done in live or not.
 *
 * <ul>
 * <li>The VM name is retrieved from its attributes (key 'name')</li>
 * <li>The node address are retrieved from their attributes (key 'ip')</li>
 * <li>By default, the timeout value equals the estimated actio duration</li>
 * </ul>
 * For the moment, the underlying qemu protocol that is used is `qemu+tcp`
 * @author Fabien Hermenier
 */
public class LibvirtMigrate implements Actuator {

    private MigrateVM a;

    private boolean live;

    /** The current migration protocol. */
    public static final String protocol = "qemu+tcp://";

    private String nodeSrcAddress, nodeDstAddress, vmName;

    private int timeout;

    /**
     *
     * @param action
     * @param vmName
     * @param nodeSrcAddress
     * @param nodeDstAddress
     * @param live
     */
    public LibvirtMigrate(MigrateVM action,
                          String vmName,
                          String nodeSrcAddress,
                          String nodeDstAddress,
                          boolean live,
                          int to) {
        a = action;
        this.vmName = vmName;
        this.nodeSrcAddress = nodeSrcAddress;
        this.nodeDstAddress = nodeDstAddress;
        this.live = live;
        this.timeout = to;
    }
    /**
     *
     * @param action
     * @param vmName
     * @param nodeSrcAddress
     * @param nodeDstAddress
     * @param live
     */
    public LibvirtMigrate(MigrateVM action,
                          String vmName,
                          String nodeSrcAddress,
                          String nodeDstAddress,
                          boolean live) {
        this(action, vmName, nodeSrcAddress, nodeDstAddress, live, action.getEnd() - action.getStart());
    }

    @Override
    public void execute() throws ExecutorException {
        String liveFlag = live ? "--live" : "";
        String command = "virsh --connect " + protocol + "/" + nodeSrcAddress + "/system"
                        + " migrate " + liveFlag
                        + " " + vmName + " "
                        + protocol + "/" + nodeDstAddress + "/system";
        try {
            Process p = Runtime.getRuntime().exec(command);
            int ret = p.waitFor();
            if (ret != 0) {
                throw new ExecutorException("Error while running " + a + ". Exit code=" + ret + "\n" + getOutput(p), null);
            }
        } catch (InterruptedException | IOException e) {
            throw new ExecutorException("Error while running " + a, e);
        }
    }

    private String getOutput(Process p) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder buf = new StringBuilder();
        String line = "";
        while ((line = reader.readLine())!= null) {
            buf.append(line).append("\n");
        }
        return buf.toString();
    }
    @Override
    public MigrateVM getAction() {
        return a;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }
}
