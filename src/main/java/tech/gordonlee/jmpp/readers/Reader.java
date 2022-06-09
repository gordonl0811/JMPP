package tech.gordonlee.jmpp.readers;

/**
 * A source of Packets to send to output buffers.
 */
public interface Reader {

    /**
     * Prepare any Disruptors and packet sources.
     */
    void initialize();

    /**
     * Release Packets from the Reader's source.
     */
    void start();

    /**
     * Shut down any Disruptors and close any
     * sources associated with the Reader.
     */
    void shutdown();

}
