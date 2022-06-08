package tech.gordonlee.jmpp.utils;

import io.pkts.packet.Packet;

public class PacketEvent {

    private Packet value;

    public Packet getValue() {
        return value;
    }

    public void setValue(Packet value) {
        this.value = value;
    }

}
