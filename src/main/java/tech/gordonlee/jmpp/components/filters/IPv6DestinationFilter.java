package tech.gordonlee.jmpp.components.filters;

import com.lmax.disruptor.dsl.Disruptor;
import io.pkts.packet.IPv4Packet;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;
import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.utils.PacketEvent;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static tech.gordonlee.jmpp.utils.Utils.startDisruptor;

public class IPv6DestinationFilter extends Component {

    private final Disruptor<PacketEvent> inputDisruptor;
    private final Disruptor<PacketEvent> outputDisruptor;

    private final BigInteger addrLowerBound;
    private final BigInteger addrUpperBound;

    public IPv6DestinationFilter(Disruptor<PacketEvent> inputDisruptor, Disruptor<PacketEvent> outputDisruptor, String addrLower, String addrUpper) throws UnknownHostException {

        this.addrLowerBound = encodeIPv6Address(addrLower);
        this.addrUpperBound = encodeIPv6Address(addrUpper);

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
        BigInteger addrValue = encodeIPv6Address(((IPv4Packet) packet.getPacket(Protocol.IPv6)).getDestinationIP());
        if (addrValue.compareTo(addrLowerBound) >= 0 && addrValue.compareTo(addrUpperBound) <= 0) {
            outputDisruptor.publishEvent((event, sequence) -> event.setValue(packet));
        }
    }

    /**
     * Parse the address using the InetAddress class and convert it to a BigInteger.
     * BigInteger is necessary because of the large size of IPv6 addresses.
     *
     * @param address the IPv6 address as a string
     * @return the address as a unique integer representation
     * @throws UnknownHostException invalid IPv6 address
     */
    private BigInteger encodeIPv6Address(String address) throws UnknownHostException {
        return new BigInteger(1, InetAddress.getByName(address).getAddress());
    }
}
