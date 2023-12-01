package de.pancake.fluff.utils;

import com.tulskiy.keymaster.common.Provider;

import javax.swing.*;

/**
 * This class adds callbacks for media keys.
 *
 * @author Pancake
 */
public class MediaKeys {

    /** The key provider */
    private final Provider provider = Provider.getCurrentProvider(false);

    /** Media key callbacks */
    private final Runnable quieter, louder, stopMedia, togglePlay, nextTrack, search;

    /**
     * Initialize media keys
     *
     * @param quieter Quieter callback
     * @param louder Louder callback
     * @param stopMedia Stop media callback
     * @param togglePlay Toggle play callback
     * @param nextTrack Next track callback
     * @param search Search callback
     */
    public MediaKeys(Runnable quieter, Runnable louder, Runnable stopMedia, Runnable togglePlay, Runnable nextTrack, Runnable search) {
        this.quieter = quieter;
        this.louder = louder;
        this.stopMedia = stopMedia;
        this.togglePlay = togglePlay;
        this.nextTrack = nextTrack;
        this.search = search;

        this.provider.register(KeyStroke.getKeyStroke(0xAE, 0), e -> this.quieter.run());
        this.provider.register(KeyStroke.getKeyStroke(0xAF, 0), e -> this.louder.run());
        this.provider.register(KeyStroke.getKeyStroke(0xB2, 0), e -> this.stopMedia.run());
        this.provider.register(KeyStroke.getKeyStroke(0xB3, 0), e -> this.togglePlay.run());
        this.provider.register(KeyStroke.getKeyStroke(0xB0, 0), e -> this.nextTrack.run());
        this.provider.register(KeyStroke.getKeyStroke(0xAA, 0), e -> this.search.run());
    }

}
