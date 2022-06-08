package tech.gordonlee.PacketProcessor;

import java.util.concurrent.TimeUnit;

public abstract class AbstractPacketProcessor implements PacketProcessor {

    @Override
    public abstract void initialize();

    @Override
    public void start() throws InterruptedException {

        releasePackets();

        while (!shouldTerminate()) {
            // Spin to reduce CPU pressure
            TimeUnit.MILLISECONDS.sleep(1);
        }

    }

    @Override
    public abstract void shutdown();

    /**
     * Signal to the readers to begin producing packets.
     */
    protected abstract void releasePackets();

    /**
     * The start() function ends when a condition is met.
     * This requirement will change for each Processor, so implementing
     * classes will need to define this condition.
     * @return true when a condition is met
     */
    protected abstract boolean shouldTerminate();
}
