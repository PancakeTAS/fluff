package de.pancake.fluff.utils;

import java.util.Arrays;

/**
 * Multiple buffer management class
 *
 * @author Pancake
 */
public class BufferManager {

    /** Buffer status enum **/
    public enum BufferStatus {
        EMPTY, FILLING, FULL, PLAYING
    }

    /** Buffer size **/
    private static final int BUFFER_SIZE = 1024 * 1024 * 16;
    /** Buffer count **/
    private static final int BUFFER_COUNT = 12;

    /** Buffers **/
    private final byte[][] buffers = new byte[BUFFER_COUNT][BUFFER_SIZE];
    /** Buffer status **/
    private final BufferStatus[] bufferStatuses = new BufferStatus[BUFFER_COUNT];

    /**
     * Initialize buffer manager
     */
    public BufferManager() {
        Arrays.fill(bufferStatuses, BufferStatus.EMPTY);
    }

    /**
     * Update buffer status
     * @param bufferIndex Buffer index
     * @param bufferStatus Buffer status
     */
    public void setBufferStatus(int bufferIndex, BufferStatus bufferStatus) {
        bufferStatuses[bufferIndex] = bufferStatus;
    }

    /**
     * Get buffer status
     * @param bufferIndex Buffer index
     * @return Buffer status
     */
    public BufferStatus getBufferStatus(int bufferIndex) {
        return bufferStatuses[bufferIndex];
    }

    /**
     * Get buffer
     * @param bufferIndex Buffer index
     * @return Buffer
     */
    public byte[] getBuffer(int bufferIndex) {
        return buffers[bufferIndex];
    }

    /**
     * Find buffer by status
     * @param bufferStatus Buffer status
     * @return Buffer index
     */
    public int findBuffer(BufferStatus bufferStatus) {
        for (int i = 0; i < BUFFER_COUNT; i++)
            if (bufferStatuses[i] == bufferStatus)
                return i;

        return -1;
    }

}
