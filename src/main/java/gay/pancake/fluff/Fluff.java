package gay.pancake.fluff;

import gay.pancake.fluff.utils.MediaKeys;
import gay.pancake.fluff.utils.PlaybackEngine;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Fluff {

    /** The youtube downloader instance */
    private final PlaybackEngine youTubeDownloader = new PlaybackEngine();
    /** The media keys instance */
    private final MediaKeys mediaKeys = new MediaKeys(
            this.youTubeDownloader::decreaseVolume,
            this.youTubeDownloader::increaseVolume,
            this::remove,
            this::togglePlay,
            this::playNext,
            this::browse
    );

    /** The list of tracks to play */
    private final Queue<String> tracks = new ConcurrentLinkedQueue<>();
    /** The queue of tasks to play tracks */
    private final Queue<Map.Entry<Thread, String>> queue = new ConcurrentLinkedQueue<>();
    /** Current track */
    private String currentTrack = null;
    /** Current playback thread */
    private Thread currentPlayback = null;

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
        CompletableFuture.runAsync(this::playNext);

        // Load tracks
        this.loadTracks();

        // Prepare tracks in background
        while (true) {
            // Wait for a track to play
            Thread.sleep(1000);

            // Load tracks if there are none
            if (this.tracks.isEmpty())
                this.loadTracks();

            // Track only up to 12 tracks
            if (this.queue.size() > 12)
                continue;

            // Get next track
            var url = this.tracks.poll();

            // Try to prepare track
            try {
                System.out.println("Downloading audio... (" + url + ")");
                this.queue.add(new AbstractMap.SimpleEntry<>(this.youTubeDownloader.downloadYoutubeVideo(url, this::playNext), url));
            } catch (Exception e) {
                System.err.println("Error while preparing track! (" + url + ")");
                e.printStackTrace();
            }
        }
    }

    /**
     * Play the next track in the queue
     */
    public void playNext() {
        // Set current playback to exited
        if (this.currentPlayback != null && this.currentPlayback.isAlive())
            this.currentPlayback.setName("Exited");

        // Wait until queue is not empty
        while (this.queue.isEmpty())
            Thread.yield();

        // Play next track
        System.out.println("Playing next track...");
        var entry = this.queue.poll();
        this.currentPlayback = Objects.requireNonNull(entry).getKey();
        this.currentTrack = entry.getValue();
        this.currentPlayback.start();
    }

    /**
     * Browse to the current track
     */
    public void browse() {
        try {
            Desktop.getDesktop().browse(URI.create(this.currentTrack));
        } catch (IOException ignored) {

        }
    }

    /**
     * Toggle the play state of the audio player
     */
    public void togglePlay() {
        if (!this.currentPlayback.isAlive())
            this.playNext();
        else
            this.currentPlayback.setName("Exited");
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
            this.playNext();
        } catch (Exception ignored) {

        }
    }

    /**
     * The main method of the application
     * @param args The command line arguments
     * @throws Exception If the application is interrupted
     */
    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.out.println("Starting Fluff...");
        } else if (args.length == 1 && args[0].equals("--list-devices")) {
            PlaybackEngine.listDevices();
            return;
        } else if (args.length >= 2 && args[0].equals("--set-device")) {
            PlaybackEngine.defaultAudioDevice(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        } else {
            System.err.println("Invalid arguments! (Use --list-devices or --set-device)");
            return;
        }

        // Try to create tray icon
        try {
            var trayIcon = new TrayIcon(new ImageIcon(Objects.requireNonNull(Fluff.class.getResource("/fluff.png"))).getImage(), "Fluff");
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> System.exit(0));
            SystemTray.getSystemTray().add(trayIcon);
        } catch (Exception ignored) {

        }

        // Start application
        var fluff = new Fluff();
        fluff.start();
    }

}
