package systems.mythical.cloudcore.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.DigestOutputStream;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.function.Consumer;
import javax.net.ssl.HttpsURLConnection;

public class DownloadManager {
    private final ExecutorService executorService;
    private final int maxRetries;
    private final int bufferSize;
    private final int timeout;
    private final CloudLogger cloudLogger = CloudLoggerFactory.get();

    public static class DownloadProgress {
        private final long totalBytes;
        private long downloadedBytes;
        private final String fileName;
        private double progress;
        private long speed; // bytes per second
        private long estimatedTimeRemaining; // in seconds

        public DownloadProgress(long totalBytes, String fileName) {
            this.totalBytes = totalBytes;
            this.fileName = fileName;
            this.downloadedBytes = 0;
            this.progress = 0.0;
            this.speed = 0;
            this.estimatedTimeRemaining = 0;
        }

        public long getTotalBytes() { return totalBytes; }
        public long getDownloadedBytes() { return downloadedBytes; }
        public String getFileName() { return fileName; }
        public double getProgress() { return progress; }
        public long getSpeed() { return speed; }
        public long getEstimatedTimeRemaining() { return estimatedTimeRemaining; }
    }

    public DownloadManager(Logger logger) {
        this(logger, 3, 8192, 30000);
    }

    public DownloadManager(Logger logger, int maxRetries, int bufferSize, int timeout) {
        this.maxRetries = maxRetries;
        this.bufferSize = bufferSize;
        this.timeout = timeout;
        this.executorService = Executors.newFixedThreadPool(3);
    }

    public Future<Path> downloadFileAsync(String urlStr, String destination, String expectedChecksum, Consumer<DownloadProgress> progressCallback) {
        return executorService.submit(() -> downloadFile(urlStr, destination, expectedChecksum, progressCallback));
    }

    public Path downloadFile(String urlStr, String destination, String expectedChecksum, Consumer<DownloadProgress> progressCallback) throws IOException {
        Path destinationPath = Paths.get(destination);
        Files.createDirectories(destinationPath.getParent());

        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                @SuppressWarnings("deprecation") // This is used for backwards compatibility with older versions of Java
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                configureConnection(connection);

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Server returned HTTP " + connection.getResponseCode());
                }

                long fileSize = connection.getContentLengthLong();
                DownloadProgress progress = new DownloadProgress(fileSize, destinationPath.getFileName().toString());

                try (InputStream in = new BufferedInputStream(connection.getInputStream());
                     OutputStream out = new BufferedOutputStream(Files.newOutputStream(destinationPath));
                     DigestOutputStream digestOut = new DigestOutputStream(out, MessageDigest.getInstance("SHA-256"))) {

                    byte[] buffer = new byte[bufferSize];
                    long startTime = System.currentTimeMillis();
                    long lastProgressUpdate = startTime;
                    int bytesRead;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        digestOut.write(buffer, 0, bytesRead);
                        progress.downloadedBytes += bytesRead;

                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastProgressUpdate >= 1000) { // Update progress every second
                            updateProgress(progress, startTime, currentTime);
                            if (progressCallback != null) {
                                progressCallback.accept(progress);
                            }
                            lastProgressUpdate = currentTime;
                        }
                    }

                    // Verify checksum if provided
                    if (expectedChecksum != null) {
                        String actualChecksum = bytesToHex(digestOut.getMessageDigest().digest());
                        if (!actualChecksum.equalsIgnoreCase(expectedChecksum)) {
                            throw new IOException("Checksum verification failed");
                        }
                    }

                    return destinationPath;
                }

            } catch (IOException | NoSuchAlgorithmException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    cloudLogger.error("Failed to download file after " + maxRetries + " attempts: " + e.getMessage());
                    throw new IOException("Download failed after " + maxRetries + " attempts", e);
                }
                cloudLogger.warn("Download attempt " + retryCount + " failed, retrying in 5 seconds...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Download interrupted", ie);
                }
            }
        }

        throw new IOException("Download failed after " + maxRetries + " attempts");
    }

    private void configureConnection(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setInstanceFollowRedirects(true);
        
        // Add common headers
        connection.setRequestProperty("User-Agent", "CloudCore-DownloadManager/1.0");
        connection.setRequestProperty("Accept", "*/*");
        
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);
        }
    }

    private void updateProgress(DownloadProgress progress, long startTime, long currentTime) {
        long elapsedSeconds = (currentTime - startTime) / 1000;
        if (elapsedSeconds > 0) {
            progress.speed = progress.downloadedBytes / elapsedSeconds;
            if (progress.speed > 0) {
                progress.estimatedTimeRemaining = (progress.totalBytes - progress.downloadedBytes) / progress.speed;
            }
        }
        if (progress.totalBytes > 0) {
            progress.progress = (double) progress.downloadedBytes / progress.totalBytes * 100;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
