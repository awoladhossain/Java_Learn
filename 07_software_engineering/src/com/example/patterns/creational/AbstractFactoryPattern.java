package com.example.patterns.creational;

/**
 * 🛠️ Creational Pattern: Abstract Factory
 * 
 * Provides an interface for creating families of related or dependent objects without specifying their concrete classes.
 * Example: Multi-cloud infrastructure provisioning (AWS vs GCP compute & storage components).
 */
public class AbstractFactoryPattern {

    // Abstract Product A
    public interface ComputeInstance {
        String launch();
        String getStatus();
    }

    // Abstract Product B
    public interface StorageBucket {
        String storeFile(String fileName);
        String getBucketUri();
    }

    // AWS Concrete Products
    public static class AwsComputeInstance implements ComputeInstance {
        @Override
        public String launch() {
            return "AWS EC2 instance (t3.xlarge) launched in us-east-1";
        }
        @Override
        public String getStatus() {
            return "AWS EC2: RUNNING";
        }
    }

    public static class AwsStorageBucket implements StorageBucket {
        private final String bucketName;

        public AwsStorageBucket(String bucketName) {
            this.bucketName = bucketName;
        }

        @Override
        public String storeFile(String fileName) {
            return String.format("Uploaded %s to s3://%s/", fileName, bucketName);
        }
        @Override
        public String getBucketUri() {
            return "s3://" + bucketName;
        }
    }

    // GCP Concrete Products
    public static class GcpComputeInstance implements ComputeInstance {
        @Override
        public String launch() {
            return "GCP Compute Engine instance (n2-standard-4) launched in us-central1";
        }
        @Override
        public String getStatus() {
            return "GCP VM: RUNNING";
        }
    }

    public static class GcpStorageBucket implements StorageBucket {
        private final String bucketName;

        public GcpStorageBucket(String bucketName) {
            this.bucketName = bucketName;
        }

        @Override
        public String storeFile(String fileName) {
            return String.format("Uploaded %s to gs://%s/", fileName, bucketName);
        }
        @Override
        public String getBucketUri() {
            return "gs://" + bucketName;
        }
    }

    // Abstract Factory Interface
    public interface CloudResourceFactory {
        ComputeInstance createComputeInstance();
        StorageBucket createStorageBucket(String name);
    }

    // Concrete Factory 1: AWS
    public static class AwsResourceFactory implements CloudResourceFactory {
        @Override
        public ComputeInstance createComputeInstance() {
            return new AwsComputeInstance();
        }
        @Override
        public StorageBucket createStorageBucket(String name) {
            return new AwsStorageBucket(name);
        }
    }

    // Concrete Factory 2: GCP
    public static class GcpResourceFactory implements CloudResourceFactory {
        @Override
        public ComputeInstance createComputeInstance() {
            return new GcpComputeInstance();
        }
        @Override
        public StorageBucket createStorageBucket(String name) {
            return new GcpStorageBucket(name);
        }
    }
}
