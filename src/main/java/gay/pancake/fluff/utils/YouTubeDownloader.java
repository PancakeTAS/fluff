package gay.pancake.fluff.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Class wrapping around yt-dlp and ffmpeg for streaming youtube videos.
 *
 * @author Pancake
 */
public class YouTubeDownloader {

    /** The command to run yt-dlp */
    private static final String YTDL = "yt-dlp %URL% --no-part --no-warnings --no-playlist --no-check-certificate -f ba -o -";
    /** The command to run ffmpeg */
    private static final String FFMPEG = "ffmpeg -rtbufsize 2M -re -i - -c:a pcm_s16le -bufsize 2M -af \"silenceremove=1:0:-55dB\" -f s16le -ar 48000 -";

    /**
     * Download a youtube video
     *
     * @param url The url of the video
     * @throws Exception If an error occurs while downloading the video
     * @return The audio data
     */
    public static byte[] downloadYoutubeVideo(String url) throws Exception {
        // Launch yt-dlp
        var processBuilder = new ProcessBuilder(YTDL.replace("%URL%", url).split(" "));
        var process = processBuilder.start();

        // Get input and error streams
        var inputStream = process.getInputStream();
        var errorStream = process.getErrorStream();

        // Create logger for error stream
        var streamLogger = new StreamLogger(errorStream, "yt-dlp");
        streamLogger.start(null);

        // Get audio data
        var data = inputStream.readAllBytes();

        // Close streams
        streamLogger.stop();
        inputStream.close();
        process.destroy();

        return data;
    }

    /**
     * Convert youtube video to pcm
     *
     * @param data The audio data
     */
    public static InputStream convertToPcm(byte[] data) throws IOException {
        // Launch ffmpeg
        var processBuilder = new ProcessBuilder(FFMPEG.split(" "));
        var process = processBuilder.start();

        // Get input and error streams
        var inputStream = process.getInputStream();
        var outputStream = process.getOutputStream();
        var errorStream = process.getErrorStream();

        // Create logger for error stream
        var streamLogger = new StreamLogger(errorStream, "ffmpeg");
        streamLogger.start(null);

        // Write audio data to ffmpeg
        CompletableFuture.runAsync(() -> {
            try {
                outputStream.write(data);
                outputStream.flush();
                outputStream.close();
            } catch (Exception ignored) {

            }
        });

        return inputStream;
    }

}
