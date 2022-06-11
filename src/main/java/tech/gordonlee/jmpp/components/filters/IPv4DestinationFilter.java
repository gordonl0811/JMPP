package tech.gordonlee.jmpp.components.filters;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.IPv4Packet;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

/**
 * Only forwards IPv4 packets that have addresses between the specified range (inclusive).
 */
public class IPv4DestinationFilter extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;
    private final Disruptor<PacketEvent> outputDisruptor;

    private final int addrLowerBound;
    private final int addrUpperBound;

    /**
     * Default constructor.
     * @param inputDisruptor Receives packets from this Disruptor
     * @param outputDisruptor Sends packets to this Disruptor
     * @param addrLower Lower bound of the address range
     * @param addrUpper Upper bound of the address range
     * @throws UnknownHostException Invalid address given/read
     */
    public IPv4DestinationFilter(Disruptor<PacketEvent> inputDisruptor, Disruptor<PacketEvent> outputDisruptor, String addrLower, String addrUpper) throws UnknownHostException {

        this.addrLowerBound = encodeIPv4Address(addrLower);
        this.addrUpperBound = encodeIPv4Address(addrUpper);

        this.inputDisruptor = inputDisruptor;
        this.outputDisruptor = outputDisruptor;

        inputDisruptor.handleEventsWith(this);
    }

    @Override
    public void initialize() {
        startDisruptor(inputDisruptor);
        startDisruptor(outputDisruptor);
    }

    @Override
    public void shutdown() {
        inputDisruptor.shutdown();
        outputDisruptor.shutdown();
    }

    @Override
    public void process(Packet packet) throws IOException {
        int addrValue = encodeIPv4Address(((IPv4Packet) packet.getPacket(Protocol.IPv4)).getDestinationIP());
        if (addrValue >= addrLowerBound && addrValue <= addrUpperBound) {
            outputDisruptor.publishEvent((event, sequence) -> event.setValue(packet));
        }
    }

    /**
     * Parse the address using the InetAddress class and convert it to an integer.
     * @param address IPv4 address as a string
     * @return Address as a unique integer representation
     * @throws UnknownHostException Invalid address given/read
     */
    private int encodeIPv4Address(String address) throws UnknownHostException {

        int result = 0;

        // Encode each octet
        for (byte b : InetAddress.getByName(address).getAddress()) {
            result = result << 8 | (b & 0xFF);
        }
        return result;

    }
}
