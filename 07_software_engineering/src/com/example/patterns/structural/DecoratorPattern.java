package com.example.patterns.structural;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 🛠️ Structural Pattern: Decorator
 * 
 * Dynamically attaches additional responsibilities to an object.
 * Decorators provide a flexible alternative to subclassing for extending functionality.
 * Mimics Java I/O streams (e.g., BufferedInputStream wrapping FileInputStream).
 */
public class DecoratorPattern {

    // Component Interface
    public interface DataStream {
        void write(String data);
        String read();
    }

    // Concrete Component
    public static class MemoryDataStream implements DataStream {
        private String content = "";

        @Override
        public void write(String data) {
            this.content = data;
        }

        @Override
        public String read() {
            return this.content;
        }
    }

    // Base Decorator (Wraps a DataStream instance)
    public static abstract class DataStreamDecorator implements DataStream {
        protected final DataStream wrappedStream;

        public DataStreamDecorator(DataStream wrappedStream) {
            this.wrappedStream = wrappedStream;
        }

        @Override
        public void write(String data) {
            wrappedStream.write(data);
        }

        @Override
        public String read() {
            return wrappedStream.read();
        }
    }

    // Concrete Decorator 1: Base64 Encoding/Decoding
    public static class Base64EncodingDecorator extends DataStreamDecorator {
        public Base64EncodingDecorator(DataStream wrappedStream) {
            super(wrappedStream);
        }

        @Override
        public void write(String data) {
            String encoded = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
            super.write(encoded);
        }

        @Override
        public String read() {
            String encoded = super.read();
            byte[] decoded = Base64.getDecoder().decode(encoded);
            return new String(decoded, StandardCharsets.UTF_8);
        }
    }

    // Concrete Decorator 2: Logging & Metric Tracking (SRE Insight)
    public static class MetricLoggingDecorator extends DataStreamDecorator {
        private int writeCount = 0;
        private int readCount = 0;
        private long totalBytesWritten = 0;

        public MetricLoggingDecorator(DataStream wrappedStream) {
            super(wrappedStream);
        }

        @Override
        public void write(String data) {
            writeCount++;
            totalBytesWritten += data.getBytes(StandardCharsets.UTF_8).length;
            super.write(data);
        }

        @Override
        public String read() {
            readCount++;
            return super.read();
        }

        public int getWriteCount() { return writeCount; }
        public int getReadCount() { return readCount; }
        public long getTotalBytesWritten() { return totalBytesWritten; }
    }
}
