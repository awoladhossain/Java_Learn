package com.example.io;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Set;

/**
 * Section 4.1.4: Non-Blocking I/O Concepts (Channels, Buffers, Selectors).
 * 
 * Demonstrates:
 * - ByteBuffer state transitions (capacity, position, limit, flip, clear, compact).
 * - Direct vs Heap ByteBuffers (Zero-Copy kernel memory mapping).
 * - FileChannel high-speed file transfer (transferTo / transferFrom).
 * - Selector & Non-Blocking ServerSocketChannel multiplexing architecture.
 */
public class NonBlockingChannelsBuffersDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 4.1.4 NON-BLOCKING I/O: Channels, Buffers & Selectors");
        System.out.println("------------------------------------------------------------------------");

        File tempDir = new File("temp_nio_channels");
        if (!tempDir.exists()) tempDir.mkdirs();

        File channelFile = new File(tempDir, "channel_data.txt");
        File destChannelFile = new File(tempDir, "channel_data_copy.txt");

        try {
            // 1. ByteBuffer State Transitions Mechanics
            System.out.println("\n--- 1. ByteBuffer State Machine (Write -> flip() -> Read -> clear()) ---");
            ByteBuffer buffer = ByteBuffer.allocate(64); // Heap ByteBuffer
            
            System.out.printf("Initial State   : Pos=%d, Limit=%d, Cap=%d\n", buffer.position(), buffer.limit(), buffer.capacity());

            // Writing data into ByteBuffer
            byte[] textBytes = "High-Throughput NIO Channel Data".getBytes(StandardCharsets.UTF_8);
            buffer.put(textBytes);
            System.out.printf("After Put (%d B): Pos=%d, Limit=%d, Cap=%d\n", textBytes.length, buffer.position(), buffer.limit(), buffer.capacity());

            // FLIP: Switch from Write Mode to Read Mode
            buffer.flip();
            System.out.printf("After flip()    : Pos=%d, Limit=%d, Cap=%d (Ready for Reading!)\n", buffer.position(), buffer.limit(), buffer.capacity());

            // Reading data out of ByteBuffer
            byte[] readContent = new byte[buffer.remaining()];
            buffer.get(readContent);
            System.out.println("Read Content    : \"" + new String(readContent, StandardCharsets.UTF_8) + "\"");
            System.out.printf("After Get       : Pos=%d, Limit=%d, Cap=%d\n", buffer.position(), buffer.limit(), buffer.capacity());

            // CLEAR: Reset positions back for writing mode
            buffer.clear();
            System.out.printf("After clear()   : Pos=%d, Limit=%d, Cap=%d\n", buffer.position(), buffer.limit(), buffer.capacity());

            // 2. Direct vs Heap ByteBuffers & FileChannel I/O
            System.out.println("\n--- 2. Heap vs Direct ByteBuffer FileChannel I/O ---");
            // Direct ByteBuffer allocates native OS memory outside JVM Heap, eliminating kernel-to-userland buffer copy
            ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
            directBuffer.put("Direct Native Memory Buffer Payload\n".getBytes(StandardCharsets.UTF_8));
            directBuffer.flip();

            try (FileChannel fileChannel = FileChannel.open(channelFile.toPath(), 
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
                fileChannel.write(directBuffer);
                System.out.println("Wrote Direct ByteBuffer payload to FileChannel. Direct? " + directBuffer.isDirect());
            }

            // 3. High-Performance Zero-Copy File Transfer (transferTo)
            System.out.println("\n--- 3. High-Speed Zero-Copy Transfer (transferTo) ---");
            try (FileChannel srcChannel = FileChannel.open(channelFile.toPath(), StandardOpenOption.READ);
                 FileChannel destChannel = FileChannel.open(destChannelFile.toPath(), 
                         StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                
                long transferred = srcChannel.transferTo(0, srcChannel.size(), destChannel);
                System.out.printf("Transferred %,d bytes using OS sendfile/zero-copy kernel transfer!\n", transferred);
            }

            // 4. Selector Non-Blocking Network Server Architecture
            System.out.println("\n--- 4. Selector & Non-Blocking ServerSocketChannel Architecture ---");
            try (Selector selector = Selector.open();
                 ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
                
                // Configure Non-Blocking Mode
                serverChannel.configureBlocking(false);
                serverChannel.bind(new InetSocketAddress("127.0.0.1", 0)); // Bind to ephemeral port
                int boundPort = ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();

                // Register Server Socket Channel with Selector for ACCEPT events
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);

                System.out.println("Non-Blocking ServerSocketChannel bound on port " + boundPort);
                System.out.println("Registered Selector keys count: " + selector.keys().size());

                // Client connection simulation (non-blocking connect)
                try (SocketChannel clientChannel = SocketChannel.open()) {
                    clientChannel.configureBlocking(false);
                    clientChannel.connect(new InetSocketAddress("127.0.0.1", boundPort));

                    // Selector Event Loop Triage Simulation
                    int readyChannels = selector.select(100); // 100ms timeout non-blocking select
                    System.out.println("Selector select() returned ready channel events count: " + readyChannels);

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isAcceptable()) {
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            SocketChannel acceptedClient = server.accept();
                            if (acceptedClient != null) {
                                acceptedClient.configureBlocking(false);
                                System.out.println("✅ Accepted Non-Blocking Client Connection: " + acceptedClient.getRemoteAddress());
                                acceptedClient.close();
                            }
                        }
                        keyIterator.remove(); // Must remove key to prevent re-processing!
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("NIO Channel Demo Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            channelFile.delete();
            destChannelFile.delete();
            tempDir.delete();
        }

        System.out.println("\n💡 SRE Architectural Insight: Non-Blocking NIO Selectors allow a SINGLE thread");
        System.out.println("   to handle 10,000+ concurrent socket connections (C10K problem solution),");
        System.out.println("   which is the core engine behind Netty, Nginx, and Node.js!");
    }
}
