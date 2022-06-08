package tech.gordonlee.PacketProcessorBenchmarks;

import tech.gordonlee.PacketProcessor.DisruptorPacketProcessor.MultipleProducerDisruptorProcessor;
import tech.gordonlee.PacketProcessor.PacketProcessor;
import tech.gordonlee.PacketProcessor.QueuePacketProcessor.MultipleProducerQueueProcessor;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 1, time = 5)
@Fork(value = 1)
public class BenchmarkMultipleProducerProcessor {

    @State(Scope.Benchmark)
    public static class DisruptorImplementationState {

        public PacketProcessor processor;

        @Param({"1", "4", "16", "64", "256", "1024", "4096"})
        public int size;

        @Param({"1", "10", "100", "1000", "10000", "100000"})
        public int numPackets;

        @Setup(Level.Invocation)
        public void setup() throws IOException {
            processor = new MultipleProducerDisruptorProcessor(
                    size,
                    "src/main/resources/inputs/copies/input_1_" + numPackets + ".pcap",
                    "src/main/resources/inputs/copies/input_2_" + numPackets + ".pcap",
                    "src/main/resources/inputs/copies/input_3_" + numPackets + ".pcap",
                    numPackets * 3
            );
            processor.initialize();
        }

        @TearDown(Level.Invocation)
        public void teardown() {
            processor.shutdown();
        }
    }

    @Benchmark
    public void benchmarkDisruptorImplementation(DisruptorImplementationState state) throws InterruptedException {
        state.processor.start();
    }

    @State(Scope.Benchmark)
    public static class QueueImplementationState {

        public PacketProcessor processor;

        @Param({"1", "4", "16", "64", "256", "1024", "4096"})
        public int size;

        @Param({"1", "10", "100", "1000", "10000", "100000"})
        public int numPackets;

        @Setup(Level.Invocation)
        public void setup() throws IOException {
            processor = new MultipleProducerQueueProcessor(
                    size,
                    "src/main/resources/inputs/copies/input_1_" + numPackets + ".pcap",
                    "src/main/resources/inputs/copies/input_2_" + numPackets + ".pcap",
                    "src/main/resources/inputs/copies/input_3_" + numPackets + ".pcap",
                    numPackets * 3
            );
            processor.initialize();
        }

        @TearDown(Level.Invocation)
        public void teardown() {
            processor.shutdown();
        }
    }

    @Benchmark
    public void benchmarkQueueImplementation(QueueImplementationState state) throws InterruptedException {
        state.processor.start();
    }

}
