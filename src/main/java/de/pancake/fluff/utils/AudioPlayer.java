package de.pancake.fluff.utils;

import java.io.IOException;

/**
 * Class wrapping around ffplay for playing audio.
 *
 * @author Pancake
 */
public class AudioPlayer {

    /** The command to run ffplay */
    private static final String[] FFPLAY = { "ffplay.exe", "-nodisp", "-nostats", "-autoexit", "-" };

    /** The current track */
    private Process process;

    /**
     * Play audio from a buffer
     *
     * @param buffer The buffer to read the audio from
     * @param length The length of the audio in the buffer
     * @throws Exception If an error occurs while playing the audio
     * @return A completable future that completes when the audio has finished playing
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

}
