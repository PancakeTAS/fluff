package gay.pancake.fluff;

import gay.pancake.fluff.utils.*;

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
    /** The media keys instance */
    private final MediaKeys mediaKeys = new MediaKeys(
            this.audioPlayer::decreaseVolume,
            this.audioPlayer::increaseVolume,
            this::remove,
            this::togglePlay,
            this.audioPlayer::stop,
            this::browse
    );

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

            // Load tracks if there are none
            if (this.tracks.isEmpty())
                this.loadTracks();

            // Find empty buffer
            int emptyBufferIndex = this.bufferManager.findBuffer(BufferManager.BufferStatus.EMPTY);
            if (emptyBufferIndex == -1)
                continue;

            // Get next track and set buffer to filling
            var url = this.tracks.poll();
            this.bufferManager.setBufferStatus(emptyBufferIndex, BufferManager.BufferStatus.FILLING);

            // Try to prepare track
            try {
                this.prepareTrack(url, emptyBufferIndex);
            } catch (Exception e) {
                System.err.println("Error while preparing track! (" + url + ")");
                e.printStackTrace();
                this.bufferManager.setBufferStatus(emptyBufferIndex, BufferManager.BufferStatus.EMPTY);
            }
        }
    }

    /**
     * Prepare a track for playing
     *
     * @param url The track url
     * @throws Exception If an error occurs while preparing the track
     */
    private void prepareTrack(String url, int emptyBufferIndex) throws Exception {
        System.out.println("Downloading audio... (" + url + ")");

        // Download video
        var buffer = this.bufferManager.getBuffer(emptyBufferIndex);
        var bytesRead = this.youTubeDownloader.downloadYoutubeVideo(url, buffer);

        // Update buffer to full
        this.bufferManager.setBufferStatus(emptyBufferIndex, BufferManager.BufferStatus.FULL);
        System.out.println("Finished downloading audio! (" + url + ")");

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
    }

    /**
     * Play the next track in the queue or wait for a new one asynchronously
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
     * Browse to the current track
     */
    public void browse() {
        try {
            Desktop.getDesktop().browse(URI.create(currentTrack));
        } catch (IOException ignored) {

        }
    }

    /**
     * Toggle the play state of the audio player
     */
    public void togglePlay() {
        try {
            this.playing = !this.playing;
            if (this.playing)
                this.playNext();
            else
                this.audioPlayer.stop();
        } catch (Exception ignored) {

        }
    }

    /**
     * Remove the current track from the list of tracks
     */
    public void remove() {
        try {
            var tracks = Files.readAllLines(Path.of(System.getProperty("user.home"), "/Music", "/fluff.txt"));
            tracks.remove(this.currentTrack);
            Files.write(Path.of(System.getProperty("user.home"), "/Music", "/fluff.txt"), tracks);
            this.loadTracks();
            this.audioPlayer.stop();
        } catch (Exception ignored) {

        }
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
