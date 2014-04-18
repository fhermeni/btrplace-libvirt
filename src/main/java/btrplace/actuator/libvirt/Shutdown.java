package btrplace.actuator.libvirt;

import btrplace.executor.Actuator;
import btrplace.executor.ExecutorException;
import btrplace.plan.event.Action;
import btrplace.plan.event.ShutdownVM;

/**
 * @author Fabien Hermenier
 */
public class Shutdown implements Actuator {

    private ShutdownVM a;

    private String vm, host;

    private LibVirt.Protocol proto;

    private int timeout;

    public Shutdown(ShutdownVM action, LibVirt.Protocol p, String vmName, String hostAddr, int to) {
        a = action;
        vm = vmName;
        host = hostAddr;
        proto = p;
        timeout = to;
    }

    public Shutdown(ShutdownVM a, LibVirt.Protocol p, String vmName, String hostAddr) {
        this(a, p, vmName, hostAddr, a.getEnd() - a.getStart());
    }

    @Override
    public void execute() throws ExecutorException {
        LibVirt.shutdown(proto, host, vm);
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
