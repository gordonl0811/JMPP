package tech.gordonlee.jmpp.components.utils;

import java.io.File;

public class TestUtils {

    public static final File SINGLE_PACKET_PCAP = new File("src/test/resources/inputs/input_1.pcap");
    public static final int SINGLE_PACKET_PCAP_COUNT = 1;

    public static final File MULTIPLE_PACKET_PCAP = new File("src/test/resources/inputs/input_10.pcap");
    public static final int MULTIPLE_PACKET_PCAP_COUNT = 10;
    public static final int TCP_PACKET_COUNT = 5;
    public static final int UDP_PACKET_COUNT = 5;

    public static final int BUFFER_SIZE = 1024;

    public static final File HUNDRED_PACKET_PCAP = new File("src/test/resources/inputs/input_100.pcap");
    public static final File HUNDRED_PACKET_PCAP_COPY_1 = new File("src/test/resources/inputs/copies/input_1_100.pcap");
    public static final File HUNDRED_PACKET_PCAP_COPY_2 = new File("src/test/resources/inputs/copies/input_2_100.pcap");
    public static final File HUNDRED_PACKET_PCAP_COPY_3 = new File("src/test/resources/inputs/copies/input_3_100.pcap");
    public static final int HUNDRED_PACKET_PCAP_COUNT = 100;


}
