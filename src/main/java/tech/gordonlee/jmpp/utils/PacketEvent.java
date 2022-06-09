package tech.gordonlee.jmpp.utils;

import io.pkts.packet.Packet;

/**
 * Packet wrapper used for transactions
 * within the Disruptors used within JMPP.
 */
public class PacketEvent {

    private Packet value;

    public Packet getValue() {
        return value;
    }

    public void setValue(Packet value) {
        this.value = value;
    }

}
