package tech.gordonlee.PacketProcessor.DisruptorPacketProcessor.components;

import tech.gordonlee.PacketProcessor.DisruptorPacketProcessor.utils.PacketEvent;
import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.Packet;

import static tech.gordonlee.PacketProcessor.DisruptorPacketProcessor.utils.Utils.startDisruptor;

public class Dropper extends ProcessorComponent {

    private final Disruptor<PacketEvent> inputDisruptor;

    public Dropper(Disruptor<PacketEvent> inputDisruptor) {
        this.inputDisruptor = inputDisruptor;
        inputDisruptor.handleEventsWith(this);
    }

    @Override
    public void initialize() {
        startDisruptor(inputDisruptor);
    }

    @Override
    public void shutdown() {
        inputDisruptor.shutdown();
    }


    @Override
    public void process(Packet packet) {
//        System.out.println("Processed " + getPacketCount());
    }
}
