package gay.pancake.fluff.utils;

import javax.sound.sampled.*;

/**
 * Fluff's playback engine
 *
 * @author Pancake
 */
public class PlaybackEngine {

    /** Audio format */
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(48000, 16, 2, true, false);

    /** The mixer */
    private static Mixer mixer;

    /** The current volume */
    private FloatControl volumeControl;
    /** The volume before muting */
    private float volume = -20.0f;
    /** Is paused */
    public boolean paused = false;

    /**
     * List all available audio devices
     */
    public static void listDevices() {
        var lineInfo = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
        for (var mixerInfo : AudioSystem.getMixerInfo())
            if (AudioSystem.getMixer(mixerInfo).isLineSupported(lineInfo))
                System.err.println(mixerInfo.getName() + " / " + mixerInfo.getDescription());
    }

    /**
     * Set the default audio device
     *
     * @param device The device to set
     */
    public static void defaultAudioDevice(String device) {
        for (var mixerInfo : AudioSystem.getMixerInfo()) {
            if (!mixerInfo.getName().contains(device))
                continue;

            System.err.println("Setting default audio device to " + mixerInfo.getName());
            mixer = AudioSystem.getMixer(mixerInfo);
        }
    }

    /**
     * Play a song
     *
     * @param url The url of the video
     * @param playNext The runnable to run when the song finishes
     * @throws Exception If an error occurs while downloading the song
     * @return The thread that is downloading the video
     */
    public Thread play(String url, Runnable playNext) throws Exception {
        var inputStream = YouTubeDownloader.convertToPcm(YouTubeDownloader.downloadYoutubeVideo(url));

        return Thread.ofPlatform().unstarted(() -> {
            try {
                // Get audio line
                SourceDataLine line;
                var lineInfo = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
                if (mixer == null)
                    line = (SourceDataLine) AudioSystem.getLine(lineInfo);
                else
                    line = (SourceDataLine) mixer.getLine(lineInfo);

                // Open audio line
                line.open(AUDIO_FORMAT, 8192);
                line.start();

                // Get volume control
                this.volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                this.volumeControl.setValue(this.volume);

                // Read from input stream and write to audio line
                var buffer = new byte[4096];
                var read = 0;
                while (!Thread.currentThread().getName().equals("Exited") && (read = inputStream.read(buffer)) != -1) {
                    while (this.paused)
                        Thread.sleep(16L);

                    line.write(buffer, 0, read);
                }

                // Play next track if the current track has finished
                if (!Thread.currentThread().getName().equals("Exited"))
                    playNext.run();

                // Close audio line
                line.drain();
                line.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Increase the volume by 5db
     */
    public void increaseVolume() {
        this.volume = Math.min(0.0f, this.volume + 3f);
        this.volumeControl.setValue(this.volume);
    }

    /**
     * Decrease the volume by 5db
     */
    public void decreaseVolume() {
        this.volume = Math.max(this.volumeControl.getMinimum(), this.volume - 3f);
        this.volumeControl.setValue(this.volume);
    }

}
