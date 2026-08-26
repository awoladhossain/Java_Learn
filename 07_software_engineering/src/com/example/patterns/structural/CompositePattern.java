package com.example.patterns.structural;

import java.util.ArrayList;
import java.util.List;

/**
 * 🛠️ Structural Pattern: Composite
 * 
 * Composes objects into tree structures to represent part-whole hierarchies.
 * Composite lets clients treat individual objects (Leaves) and compositions of objects (Composites) uniformly.
 * E.g., File System hierarchy with Files (Leaf) and Directories (Composite).
 */
public class CompositePattern {

    // Component Interface
    public interface FileSystemNode {
        String getName();
        long getSize();
        void printTree(String indent);
    }

    // Leaf Element
    public static class FileNode implements FileSystemNode {
        private final String name;
        private final long sizeInBytes;

        public FileNode(String name, long sizeInBytes) {
            this.name = name;
            this.sizeInBytes = sizeInBytes;
        }

        @Override
        public String getName() { return name; }

        @Override
        public long getSize() { return sizeInBytes; }

        @Override
        public void printTree(String indent) {
            System.out.println(indent + "├── 📄 " + name + " (" + sizeInBytes + " bytes)");
        }
    }

    // Composite Element
    public static class DirectoryNode implements FileSystemNode {
        private final String name;
        private final List<FileSystemNode> children = new ArrayList<>();

        public DirectoryNode(String name) {
            this.name = name;
        }

        public void addNode(FileSystemNode node) {
            children.add(node);
        }

        public void removeNode(FileSystemNode node) {
            children.remove(node);
        }

        public List<FileSystemNode> getChildren() {
            return children;
        }

        @Override
        public String getName() { return name; }

        @Override
        public long getSize() {
            // Recursive calculation of directory size across children
            return children.stream().mapToLong(FileSystemNode::getSize).sum();
        }

        @Override
        public void printTree(String indent) {
            System.out.println(indent + "📁 " + name + "/ [Total Size: " + getSize() + " bytes]");
            for (FileSystemNode child : children) {
                child.printTree(indent + "    ");
            }
        }
    }
}
