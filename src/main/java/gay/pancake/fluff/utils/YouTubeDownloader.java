package gay.pancake.fluff.utils;

/**
 * Class wrapping around yt-dlp for streaming youtube videos.
 *
 * @author Pancake
 */
public class YouTubeDownloader {

    /** The command to run yt-dlp */
    private static final String[] YTDL = { "yt-dlp.exe", "url", "--no-part", "--no-warnings", "--no-playlist", "--no-check-certificate", "--no-part", "-f", "bestaudio", "-o", "-" };

    /**
     * Download a youtube video
     *
     * @param url The url of the video
     * @param buffer The buffer to write the video to
     * @throws Exception If an error occurs while downloading the video
     * @return The amount of bytes read
     */
    public int downloadYoutubeVideo(String url, byte[] buffer) throws Exception {
        // Launch yt-dlp
        YTDL[1] = url;
        var processBuilder = new ProcessBuilder(YTDL);
        var process = processBuilder.start();

        // Get input and error streams
        var inputStream = process.getInputStream();
        var errorStream = process.getErrorStream();

        // Create logger for error stream
        var streamLogger = new StreamLogger(errorStream, "yt-dlp");
        streamLogger.start();

        // Read from input stream
        int read, total = 0;
        do {
            read = inputStream.read(buffer, total, 1024);
            total += Math.max(read, 0);
        } while (read != -1);

        // Check if buffer was too small
        if (total == buffer.length) {
            process.destroyForcibly();
            throw new Exception("Failed to download video: buffer too small");
        }

        // Wait for process to finish
        inputStream.close();
        process.waitFor();
        return total;
    }

}
