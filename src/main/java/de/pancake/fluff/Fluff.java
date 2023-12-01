package de.pancake.fluff;

import de.pancake.fluff.utils.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Fluff {

    /** The buffer manager instance */
    private final BufferManager bufferManager = new BufferManager();
    /** The youtube downloader instance */
    private final YouTubeDownloader youTubeDownloader = new YouTubeDownloader();
    /** The audio player instance */
    private final AudioPlayer audioPlayer = new AudioPlayer();
    /** The tray instance */
    private final Tray tray = new Tray();

    /** The list of tracks to play */
    private final Queue<String> tracks = new ConcurrentLinkedQueue<>();
    /** The queue of tasks to play tracks */
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    /** The current track url */
    private String currentTrack;
    /** Is the application playing */
    private boolean playing = true;

    /**
     * Load the tracks from the tracks.txt file
     * @throws Exception If an error occurs while reading the file
     */
    public void loadTracks() throws Exception {
        this.tracks.clear();
        this.queue.clear();

        var tracks = Files.readAllLines(Path.of(System.getProperty("user.home"), "/Music", "/fluff.txt"));
        Collections.shuffle(tracks);
        this.tracks.addAll(tracks);
    }

    /**
     * Start the application
     * @throws Exception If the application is interrupted
     */
    public void start() throws Exception {
        // Play first track
        this.playNext();

        // Load tracks
        this.loadTracks();

        // Prepare tracks in background
        while (true) {
            // Wait for a track to play
            Thread.sleep(1000);

            // Wait for free buffer
            if (this.bufferManager.findBuffer(BufferManager.BufferStatus.EMPTY) == -1)
                continue;

            // Load tracks if there are none
            if (this.tracks.isEmpty())
                this.loadTracks();

            // Prepare track
            this.prepareTrack(this.tracks.poll());
        }
    }

    /**
     * Prepare a track for playing
     *
     * @param url The track url
     */
    private void prepareTrack(String url) {
        CompletableFuture.supplyAsync(() -> {
            System.out.println("Waiting for free buffer... (" + url + ")");

            // Get first empty buffer and update it to filling
            int i;
            do {
                i = this.bufferManager.findBuffer(BufferManager.BufferStatus.EMPTY);
            } while (i == -1);
            int emptyBufferIndex = i;

            this.bufferManager.setBufferStatus(emptyBufferIndex, BufferManager.BufferStatus.FILLING);
            var buffer = this.bufferManager.getBuffer(emptyBufferIndex);

            // Download video and add play task to queue
            try {
                System.out.println("Downloading audio... (" + url + ")");
                this.youTubeDownloader.downloadYoutubeVideo(url, buffer).thenAccept(bytesRead -> {
                    System.out.println("Finished downloading audio! (" + url + ")");

                    // Update buffer to full
                    this.bufferManager.setBufferStatus(emptyBufferIndex, BufferManager.BufferStatus.FULL);

                    // Add play task to queue
                    this.queue.add(() -> {
                        // Play audio from buffer
                        try {
                            System.out.println("Playing audio... (" + url + ")");
                            this.currentTrack = url;
                            this.bufferManager.setBufferStatus(emptyBufferIndex, BufferManager.BufferStatus.PLAYING);
                            this.audioPlayer.playAudio(buffer, bytesRead).onExit().thenRun(this::playNext);
                        } catch (Exception e) {
                            System.err.println("Error while playing audio!");
                            e.printStackTrace();
                        }

                        // Update buffer to empty
                        this.bufferManager.setBufferStatus(emptyBufferIndex, BufferManager.BufferStatus.EMPTY);
                    });
                });
            } catch (Exception e) {
                System.err.println("Error while downloading audio! (" + url + ")");
                e.printStackTrace();
                this.bufferManager.setBufferStatus(emptyBufferIndex, BufferManager.BufferStatus.EMPTY);
            }

            return null;
        });
    }

    /**
     * Play the next track in the queue or wait for a new one
     */
    public void playNext() {
        if (!this.playing)
            return;

        CompletableFuture.runAsync(() -> {
            // Wait until queue is not empty
            while (this.queue.isEmpty())
                Thread.yield();

            // Play next track
            this.queue.poll().run();
        });
    }

    /**
     * The main method of the application
     * @param args The command line arguments
     * @throws Exception If the application is interrupted
     */
    public static void main(String[] args) throws Exception {
        var fluff = new Fluff();
        fluff.start();
    }

}
