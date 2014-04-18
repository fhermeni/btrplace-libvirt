package btrplace.actuator.libvirt;

import btrplace.executor.Actuator;
import btrplace.executor.ExecutorException;
import btrplace.plan.event.Action;
import btrplace.plan.event.KillVM;

/**
 * @author Fabien Hermenier
 */
public class Destroy implements Actuator {

    private KillVM a;

    private String vm, host;

    private LibVirt.Protocol proto;

    private int timeout;

    public Destroy(KillVM a, LibVirt.Protocol p, String vmName, String hostAddr) {
        this(a, p, vmName, hostAddr, a.getEnd() - a.getStart());

    }
    public Destroy(KillVM action, LibVirt.Protocol p, String vmName, String hostAddr, int to) {
        vm = vmName;
        host = hostAddr;
        proto = p;
        timeout = to;
        a = action;
    }

    @Override
    public void execute() throws ExecutorException {
        LibVirt.destroy(proto, host, vm);
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
