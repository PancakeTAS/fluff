package de.pancake.fluff.utils;

import java.io.IOException;

/**
 * Class wrapping around ffplay for playing audio.
 *
 * @author Pancake
 */
public class AudioPlayer {

    /** The command to run ffplay */
    private static final String[] FFPLAY = { "ffplay.exe", "-nodisp", "-af", "silenceremove=1:0:-55dB", "-fflags", "nobuffer", "-flags", "low_delay", "-framedrop", "-strict", "experimental", "-nostats", "-autoexit", "-" };
    /** The command to run SoundVolumeView */
    private static final String[] SOUND_VOLUME_VIEW = { "SoundVolumeView.exe", "/SetVolume", "ffplay", "100" };

    /** The current track */
    private Process process;
    /** The current volume */
    private int volume = 100;

    /**
     * Play audio from a buffer
     *
     * @param buffer The buffer to read the audio from
     * @param length The length of the audio in the buffer
     * @throws Exception If an error occurs while playing the audio
     * @return The process playing the audio
     */
    public Process playAudio(byte[] buffer, int length) throws Exception {
        // Stop current track
        if (this.process != null)
            this.process.destroyForcibly();

        // Launch ffplay
        var processBuilder = new ProcessBuilder(FFPLAY);
        this.process = processBuilder.start();

        // Create logger for error stream
        var streamLogger = new StreamLogger(process.getErrorStream(), "ffplay");
        streamLogger.start();

        try {
            // Write to input stream
            var outputStream = process.getOutputStream();
            outputStream.write(buffer, 0, length);
            outputStream.close();
        } catch (IOException ignored) {

        }

        // Return process
        return process;
    }

    public void stop() {
        if (this.process != null)
            this.process.destroyForcibly();
    }

    /**
     * Set the volume of the audio player
     */
    private void setVolume() {
        try {
            SOUND_VOLUME_VIEW[3] = String.valueOf(this.volume);
            var processBuilder = new ProcessBuilder(SOUND_VOLUME_VIEW);
            processBuilder.start();
        } catch (IOException ignored) {

        }
    }

    /**
     * Increase the volume by 10%
     */
    public void increaseVolume() {
        this.volume = Math.min(100, this.volume + 10);
        this.setVolume();
    }

    /**
     * Decrease the volume by 10%
     */
    public void decreaseVolume() {
        this.volume = Math.max(0, this.volume - 10);
        this.setVolume();
    }

}
