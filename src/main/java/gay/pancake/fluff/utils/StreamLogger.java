package gay.pancake.fluff.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class wrapping around an input stream to log it to the console.
 *
 * @author Pancake
 */
public class StreamLogger {

    /** The stream to log */
    private final BufferedReader stream;
    /** The prefix to prepend to each line */
    private final String prefix;

    /**
     * Create a new stream logger
     *
     * @param stream The stream to log
     * @param prefix The prefix to prepend to each line
     */
    public StreamLogger(InputStream stream, String prefix) {
        this.stream = new BufferedReader(new InputStreamReader(stream));
        this.prefix = prefix;
    }

    /**
     * Start logging the stream
     * @param playNext The runnable to run when the stream ends
     */
    public void start(Runnable playNext) {
        Thread.ofPlatform().name("StreamLogger: " + this.prefix).daemon(true).start(() -> {
            try {
                String line;
                while ((line = this.stream.readLine()) != null) {
                    System.err.println(this.prefix + " >>> " + line);
                    if (line.contains("video:0kB"))
                        playNext.run();
                }
            } catch (Exception ignored) {

            }
        });
    }

    /**
     * Stop logging the stream
     */
    public void stop() {
        try {
            this.stream.close();
        } catch (Exception ignored) {

        }
    }

}
