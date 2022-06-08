package tech.gordonlee.jmpp;

import tech.gordonlee.jmpp.components.Component;
import tech.gordonlee.jmpp.sources.PcapReader;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPacketProcessor implements PacketProcessor {

    private List<PcapReader> readers;
    private List<Component> components;

    protected abstract List<PcapReader> setReaders();

    protected abstract List<Component> setComponents();


    @Override
    public final void initialize() {
        readers = setReaders();
        components = setComponents();
        for (PcapReader reader : readers) {
            reader.initialize();
        }
        for (Component component : components) {
            component.initialize();
        }
    }

    @Override
    public void start() throws InterruptedException {

        releasePackets();

        while (!shouldTerminate()) {
            // Spin to reduce CPU pressure
            TimeUnit.MILLISECONDS.sleep(1);
        }

    }

    @Override
    public final void shutdown() {
        for (PcapReader reader : readers) {
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
        for (PcapReader reader : readers) {
            reader.start();
        }
    }

    /**
     * The start() function ends when a condition is met. This requirement will change for each Processor, so
     * implementing classes will need to define this condition.
     *
     * @return true when a condition is met
     */
    protected abstract boolean shouldTerminate();
}
