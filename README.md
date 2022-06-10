# Java Modular Packet Processor

A lightweight Java library for processing network packets with a user-defined graph of individual processing operations.

## Documentation

- [Overview](#overview)
- [User Guide](#user-guide)
- [Benchmarks](#benchmarks)
- [Future Work](#future-work)

## Overview

The Java Modular Packet Processor (JMPP) library is part of a Final Year Project (Programming Strategies for Low-Latency Applications) for the Computing BEng at Imperial College London.

The JMPP library allows users to create variations of `Processors`, consisting of `Components` that perform different operations on packets. These components are interchangeable, allowing users to specify a network of components to route packets through with a graph-like structure.

The project is not built for production, but instead has a broader objective that aligns with the purpose of the university project. Packets are passed between components using the [LMAX Disruptor](https://lmax-exchange.github.io/disruptor/), a sophisticated data structure designed to replace queues used in typical producer-consumer design patterns. JMPP contains reproducible benchmarks (collected using the Java Microbenchmark Harness) that demonstrate the advantages of the Disruptor over the high-performing `java.util.concurrent.ArrayBlockingQueue`.

The JMPP library is also built on the lightweight [pkts.io Java library developed by aboutsip](https://github.com/aboutsip/pkts).

## User Guide

Processors consist of `Readers`, `Components`, and `Outputters`. `Disruptors` are used to pass `Packets` between the different objects, which serve as alternatives to classic `Queue` implementations.

The following sections describe these core concepts, their roles within JMPP, and how to tie them together into a new Processor.

### Readers

`Readers` are responsible for providing packets to the processor. At the moment, packets can only be obtained from Pcap files; processors cannot take in streams of packets from sockets (which is a major feature that needs to be implemented).

| Class      | Description                                                                                    |
|------------|------------------------------------------------------------------------------------------------|
| PcapReader | Creates a stream of packets from a Pcap file, publishing them to an output Disruptor (buffer). |

### Components

`Components` are the heart of JMPP and are responsible for analysing the Packets that are passed through them. These are classified into 3 distinct subcategories:

- `Filters` identify packets with specific traits, such as common headers within the TCP/IP model, or specific address ranges.
- `Rewriters` modify attributes of packets at different layers, including addresses, ports, etc.
- `Outputters` process packets to be accessed beyond the processor (except for `Droppers`). In the graph of Components for each processor, these can be considered "leaf nodes", i.e. the last stage of processing.

Every component records the number of packets that it processes, which is accessible via the `getPacketCount()` method. Future work involves making metric collection an option, reducing the performance impact that may be associated with this. 

The tables below outline the components available and their functionalities.

#### Filters

| Class            | Description                                                    |
|------------------|----------------------------------------------------------------|
| IPv4Filter       | Outputs packets with an IPv4 (Layer 3) header                  |
| IPv6Filter       | Outputs packets with an IPv6 (Layer 3) header                  |
| TCPFilter        | Outputs packets with a TCP (Layer 4) header                    |
| UDPFilter        | Outputs packets with a UDP (Layer 4) header                    |
| MACAddressFilter | Outputs packets that have a MAC address within the given range |
| IPAddressFilter  | Outputs packets that have an IP address within the given range |

#### Rewriters

| Class              | Description                                                                       |
|--------------------|-----------------------------------------------------------------------------------|
| MACAddressRewriter | Rewrites the source and/or destination addresses of the Ethernet (Layer 2) header |
| IPAddressRewriter  | Rewrites the source and/or destination addresses of the IP (Layer 3) header       |
| PortRewriter       | Rewrites the source and/or destination ports of the TCP/UDP (Layer 4) header      |

#### Outputters

| Class   | Description                                                                       |
|---------|-----------------------------------------------------------------------------------|
| Writer  | Writes the packets to a specified file. Particularly useful for functional tests. |
| Dropper | Discards packets, i.e. no-op. Useful for counting packets handled by a buffer.    |

### Disruptors

The `Disruptor` is an open-source, high performance data structure written in Java and developed by the LMAX Group. It aims to reduce latency and high levels of jitter that are discovered when using bounded queues by offering an alternative for exchanging data between concurrent threads. From their own benchmarks, LMAX claims that the mean latency when using the Disruptor for a three-stage pipeline is 3 orders of magnitude lower than an equivalent queue-based approach, and handles approximately 8 times more throughput.

LMAX have built an order matching engine, real-time risk management, and a highly available in-memory transaction processing system using this design pattern. However, it does not have a use only within the Finance industry; it is a general-purpose pattern that provides a high performance solution to a complex concurrency problem.

This library uses Disruptors to pass `Packets` between components, simulating a processing pipeline that can fork and rejoin streams of Packets based on user-defined logic.

### Getting Started

For most user implementations, using the `PacketProcessor` interface and `AbstractPacketProcessor` will save a lot of development time and consideration. This section outlines how to build a Processor around this interface/class.

#### Requirements

- Basic understanding of Disruptors (documentation can be found [here](https://lmax-exchange.github.io/disruptor/))

TODO

#### The Processor Lifecycle

Processors have three distinct stages wrapped into three methods specified by the `PacketProcessor` interface.

1. `initialize()`: Disruptors are started, ready to receive packets
2. `start()`: `Readers` start producing packets until a certain condition is met (or indefinitely)
3. `shutdown()`: Gracefully shuts down the Disruptors associated with the Processor

These need to be called in turn; omitting the `shutdown()` call can result in undefined behaviour.

The separation of these processes allows for easier benchmarking, as demonstrated in later sections of the documentation.

#### Building Processors



#### Example: Redirecting TCP and UDP packets



## Benchmarks

TODO

## Future Work

The bottleneck for 
