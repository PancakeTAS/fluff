package gay.pancake.fluff.utils;

import javax.sound.sampled.*;

/**
 * Class wrapping around yt-dlp for streaming youtube videos.
 *
 * @author Pancake
 */
public class PlaybackEngine {

    /** Audio format */
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(192000, 16, 2, true, false);
    /** The command to run yt-dlp */
    private static final String YTDL = "yt-dlp %URL% --no-part --no-warnings --no-playlist --no-check-certificate -f ba -o -";
    /** The command to run ffmpeg */
    private static final String FFMPEG = "ffmpeg -i - -c:a pcm_s16le -af \"silenceremove=1:0:-55dB\" -f s16le -ar 192000 -";

    /** The mixer */
    private static Mixer mixer;

    /** The current volume */
    private FloatControl volumeControl;
    /** The volume before muting */
    private float volume = -20.0f;

    /**
     * List all available audio devices
     */
    public static void listDevices() {
        var lineInfo = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
        for (var mixerInfo : AudioSystem.getMixerInfo())
            if (AudioSystem.getMixer(mixerInfo).isLineSupported(lineInfo))
                System.out.println(mixerInfo.getName() + " / " + mixerInfo.getDescription());
    }

    /**
     * Set the default audio device
     *
     * @param device The device to set
     */
    public static void defaultAudioDevice(String device) {
        var lineInfo = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
        for (var mixerInfo : AudioSystem.getMixerInfo()) {
            if (!mixerInfo.getName().contains(device))
                continue;

            System.out.println("Setting default audio device to " + mixerInfo.getName());
            mixer = AudioSystem.getMixer(mixerInfo);
        }
    }

    /**
     * Download a youtube video
     *
     * @param url The url of the video
     * @param playNext The runnable to run when the video finishes
     * @throws Exception If an error occurs while downloading the video
     * @return The thread that is downloading the video
     */
    public Thread downloadYoutubeVideo(String url, Runnable playNext) throws Exception {
        // Launch yt-dlp
        ProcessBuilder processBuilder;
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            processBuilder = new ProcessBuilder("cmd.exe", "/k", (YTDL + " | " + FFMPEG).replace("%URL%", url));
        else
            processBuilder = new ProcessBuilder("/bin/bash", "-c", (YTDL + " | " + FFMPEG).replace("%URL%", url));

        var process = processBuilder.start();

        // Get input and error streams
        var inputStream = process.getInputStream();
        var errorStream = process.getErrorStream();

        // Create logger for error stream
        var streamLogger = new StreamLogger(errorStream, "yt-dlp");
        streamLogger.start(playNext);

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
                while (!Thread.currentThread().getName().equals("Exited") && (read = inputStream.read(buffer)) != -1)
                    line.write(buffer, 0, read);

                // Close audio line
                line.drain();
                line.close();
                process.destroy();
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
