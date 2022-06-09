package tech.gordonlee.jmpp;

import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.readers.Reader;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPacketProcessor implements PacketProcessor {

    private List<Reader> readers;
    private List<Component> components;

    /**
     * Implementing classes have the responsibility of
     * declaring the sources used within the Processor.
     * @return a list of Readers (recommend using List.of())
     */
    protected abstract List<Reader> setReaders();

    /**
     * Implementing classes have the responsibility of
     * declaring the components used within the Processor.
     * @return a list of Components (recommend using List.of())
     */
    protected abstract List<Component> setComponents();


    /**
     * Initialises the Readers and Components that the
     * Processor uses, dependent on their implementations.
     */
    @Override
    public final void initialize() {
        readers = setReaders();
        components = setComponents();
        for (Reader reader : readers) {
            reader.initialize();
        }
        for (Component component : components) {
            component.initialize();
        }
    }

    /**
     * Releases the packets from the Readers,
     * spinning until it is told to terminate.
     * @throws InterruptedException from sleep() when spinning
     */
    @Override
    public void start() throws InterruptedException {

        releasePackets();

        while (!shouldTerminate()) {
            // Spin to reduce CPU pressure
            TimeUnit.MILLISECONDS.sleep(1);
        }

    }

    /**
     * Shuts down each reader/component
     * specific to their implementation.
     */
    @Override
    public final void shutdown() {
        for (Reader reader : readers) {
            reader.shutdown();
        }
        for (Component component : components) {
            component.shutdown();
        }
    }

    /**
     * Signal to the readers to begin producing packets.
     */
    private void releasePackets() {
        for (Reader reader : readers) {
            reader.start();
        }
    }

    /**
     * The start() function ends when a condition is met. This
     * requirement will change for each Processor, so
     * implementing classes will need to define this condition.
     * @return true when a condition is met
     */
    protected abstract boolean shouldTerminate();
}
