package com.example.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Section 4.1.1: Byte Streams vs Character Streams.
 *
 * Demonstrates:
 * - Byte Streams (FileInputStream / FileOutputStream) for binary & raw data.
 * - Character Streams (FileReader / FileWriter) for text data with character
 * encoding.
 * - Multi-byte character handling (UTF-8, Emoji, non-ASCII characters).
 * - Bridge Streams (InputStreamReader / OutputStreamWriter) for custom charset
 * handling.
 */
public class ByteVsCharacterStreamsDemo {

    public static void runDemo() {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println("📌 4.1.1 BYTE STREAMS vs CHARACTER STREAMS");
        System.out.println("------------------------------------------------------------------------");

        File tempDir = new File("temp_io_demo");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File binaryFile = new File(tempDir, "sample_binary.dat");
        File textFile = new File(tempDir, "sample_utf8.txt");

        try {
            // 1. Byte Stream Demo (Raw Binary Data)
            System.out.println("\n--- 1. Byte Streams (FileOutputStream / FileInputStream) ---");
            byte[] rawBytes = new byte[] { (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF, 65, 66, 67 };

            try (FileOutputStream fos = new FileOutputStream(binaryFile)) {
                fos.write(rawBytes);
                System.out.println("Wrote " + rawBytes.length + " raw binary bytes to " + binaryFile.getName());
            }

            try (FileInputStream fis = new FileInputStream(binaryFile)) {
                byte[] readBytes = fis.readAllBytes();
                System.out.print("Read Binary Hex Stream: ");
                for (byte b : readBytes) {
                    System.out.printf("0x%02X ", b);
                }
                System.out.println();
            }

            // 2. Character Stream Demo (UTF-8 Encoded Text)
            System.out.println("\n--- 2. Character Streams (FileWriter / FileReader with UTF-8) ---");
            String sampleText = "Java I/O & NIO.2 Mastery 🚀 | UTF-8: 🌍 こんにちは, 世界! (Hello World)";

            try (FileWriter writer = new FileWriter(textFile, StandardCharsets.UTF_8)) {
                writer.write(sampleText);
                System.out.println(
                        "Wrote UTF-8 character string (" + sampleText.length() + " chars) to " + textFile.getName());
            }

            try (FileReader reader = new FileReader(textFile, StandardCharsets.UTF_8)) {
                char[] charBuffer = new char[128];
                int charsRead = reader.read(charBuffer);
                String result = new String(charBuffer, 0, charsRead);
                System.out.println("Read UTF-8 Character Stream (" + charsRead + " chars):");
                System.out.println("  \"" + result + "\"");
            }

            // 3. Byte Stream vs Character Stream Multi-byte Char Trap
            System.out.println("\n--- 3. Byte Stream vs Character Stream Multi-Byte Encoding Trap ---");
            // Japanese character '日' takes 3 bytes in UTF-8: 0xE6 0x97 0xA5
            byte[] multiByteChar = "日".getBytes(StandardCharsets.UTF_8);
            System.out.println("Character '日' UTF-8 byte length: " + multiByteChar.length + " bytes");

            System.out.print("Reading byte-by-byte as char (Byte Stream trap): ");
            for (byte b : multiByteChar) {
                System.out.print((char) (b & 0xFF) + " "); // Corrupted display because 1 byte != 1 char
            }
            System.out.println(" (CORRUPTED!)");

            try (InputStreamReader isr = new InputStreamReader(new FileInputStream(textFile), StandardCharsets.UTF_8)) {
                System.out.print("Reading via InputStreamReader (Bridge Stream with UTF-8 decoder): ");
                int ch;
                int count = 0;
                while ((ch = isr.read()) != -1 && count < 25) {
                    System.out.print((char) ch);
                    count++;
                }
                System.out.println("... (ACCURATE DECISION DECODING)");
            }

        } catch (Exception e) {
            System.err.println("I/O Demo Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up demo files
            binaryFile.delete();
            textFile.delete();
            tempDir.delete();
        }

        System.out.println("\n💡 SRE Rule:");
        System.out.println(
                "   - Use Byte Streams (InputStream / OutputStream) for BINARY data (images, archives, network protocols).");
        System.out.println(
                "   - Use Character Streams (Reader / Writer) for TEXT data, ALWAYS explicitly specifying UTF-8 charset.");
    }
}
