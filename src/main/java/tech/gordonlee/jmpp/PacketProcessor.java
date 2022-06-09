package tech.gordonlee.jmpp;

/**
 * Packet Processors tie together the JMPP components
 */
public interface PacketProcessor {

    /**
     * Initialise the processor by preparing the components to receive packets.
     */
    void initialize();

    /**
     * Start the processor, allowing the readers to produce packets from their sources.
     *
     * @throws InterruptedException can be thrown when waiting for termination
     */
    void start() throws InterruptedException;

    /**
     * Clean up any objects that need manual shutdown, as well as any files that need closing.
     */
    void shutdown();
}
