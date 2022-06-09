package tech.gordonlee.jmpp.utils;

import com.lmax.disruptor.dsl.Disruptor;

/**
 * A set of utility functions that may be used by Components.
 */
public class Utils {

    /**
     * Function that safely starts a shared Disruptor,
     * even if they were started by other components.
     * @param disruptor the Disruptor being started
     */
    static public void startDisruptor(Disruptor<PacketEvent> disruptor) {
        if (!disruptor.hasStarted()) {
            disruptor.start();
        }
    }
}
