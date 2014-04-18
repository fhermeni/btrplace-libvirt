package btrplace.actuator.libvirt;

import btrplace.executor.Actuator;
import btrplace.executor.ExecutorException;
import btrplace.plan.event.Action;
import btrplace.plan.event.BootVM;

/**
 * @author Fabien Hermenier
 */
public class Boot implements Actuator {

    private BootVM a;

    private String vm, host;

    private LibVirt.Protocol proto;

    private int timeout;

    public Boot(BootVM action, LibVirt.Protocol p, String vmName, String hostAddr, int to) {
        a = action;
        vm = vmName;
        host = hostAddr;
        proto = p;
        timeout = to;
    }

    public Boot(BootVM a, LibVirt.Protocol p, String vmName, String hostAddr) {
        this(a, p, vmName, hostAddr, a.getEnd() - a.getStart());
    }

    @Override
    public void execute() throws ExecutorException {
        LibVirt.boot(proto, host, vm);
    }

    @Override
    public Action getAction() {
        return a;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }
}
