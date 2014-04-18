package btrplace.actuator.libvirt;

import btrplace.executor.Actuator;
import btrplace.executor.ExecutorException;
import btrplace.plan.event.MigrateVM;

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
public class Migrate implements Actuator {

    private MigrateVM a;

    private boolean live;

    /** The current migration protocol. */
    public static final String PROTOCOL = "qemu+tcp://";

    private String src, dst, vm;

    private int timeout;

    private LibVirt.Protocol proto;
    /**
     *
     * @param action
     * @param vmName
     * @param nodeSrcAddr
     * @param nodeDstAddr
     * @param live
     */
    public Migrate(LibVirt.Protocol p,
                   MigrateVM action,
                   String vmName,
                   String nodeSrcAddr,
                   String nodeDstAddr,
                   boolean live) {
        this(p, action, vmName, nodeSrcAddr, nodeDstAddr, live, action.getEnd() - action.getStart());
    }

    /**
     *
     * @param action
     * @param vmName
     * @param nodeSrcAddr
     * @param nodeDstAddr
     * @param live
     */
    public Migrate(LibVirt.Protocol p,
                   MigrateVM action,
                   String vmName,
                   String nodeSrcAddr,
                   String nodeDstAddr,
                   boolean live,
                   int to) {
        a = action;
        proto = p;
        this.vm = vmName;
        this.src = nodeSrcAddr;
        this.dst = nodeDstAddr;
        this.live = live;
        this.timeout = to;
    }

    @Override
    public void execute() throws ExecutorException {
        LibVirt.migrate(proto, src, vm, dst, live);

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
