/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.util;

/**
 * Fixed-capacity circular ring buffer with zero autoboxing.
 *
 * <p>Provides a generic object variant and three primitive specializations
 * ({@link OfDouble}, {@link OfLong}, {@link OfInt}) for hot-path use.
 * All implementations use a fixed-size array with a circular write index —
 * no heap allocation after construction.
 *
 * <h2>Thread safety</h2>
 * Not thread-safe. Each {@link dev.tyoudm.assasin.data.PlayerData} instance
 * owns its own buffers; concurrent access is prevented by the packet-processing
 * model (one goroutine per player).
 *
 * @param <T> element type (use primitive specializations to avoid boxing)
 * @author TyouDm
 * @version 1.0.0
 */
public final class RingBuffer<T> {

    private final Object[] data;
    private final int      capacity;
    private int            writeIndex;
    private int            size;

    /**
     * Creates a new ring buffer with the given capacity.
     *
     * @param capacity maximum number of elements (must be &gt; 0)
     * @throws IllegalArgumentException if capacity ≤ 0
     */
    public RingBuffer(final int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("RingBuffer capacity must be > 0, got: " + capacity);
        }
        this.capacity   = capacity;
        this.data       = new Object[capacity];
        this.writeIndex = 0;
        this.size       = 0;
    }

    // ─── Write ────────────────────────────────────────────────────────────────

    /**
     * Adds an element to the buffer, overwriting the oldest entry when full.
     *
     * @param value the element to add
     */
    public void add(final T value) {
        data[writeIndex] = value;
        writeIndex = (writeIndex + 1) % capacity;
        if (size < capacity) size++;
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    /**
     * Returns the element at logical index {@code i} (0 = oldest, size-1 = newest).
     *
     * @param i logical index
     * @return element at index {@code i}
     * @throws IndexOutOfBoundsException if {@code i} is out of range
     */
    @SuppressWarnings("unchecked")
    public T get(final int i) {
        if (i < 0 || i >= size) {
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for size " + size);
        }
        // oldest element is at (writeIndex - size + capacity) % capacity
        return (T) data[(writeIndex - size + i + capacity) % capacity];
    }

    /**
     * Returns the newest element (tail).
     *
     * @return newest element
     * @throws IllegalStateException if the buffer is empty
     */
    public T newest() {
        if (size == 0) throw new IllegalStateException("RingBuffer is empty.");
        return get(size - 1);
    }

    /**
     * Returns the oldest element (head).
     *
     * @return oldest element
     * @throws IllegalStateException if the buffer is empty
     */
    public T oldest() {
        if (size == 0) throw new IllegalStateException("RingBuffer is empty.");
        return get(0);
    }

    // ─── State ────────────────────────────────────────────────────────────────

    /** Returns the number of elements currently stored. */
    public int size()     { return size; }

    /** Returns the maximum capacity. */
    public int capacity() { return capacity; }

    /** Returns {@code true} if the buffer contains no elements. */
    public boolean isEmpty() { return size == 0; }

    /** Returns {@code true} if the buffer is at full capacity. */
    public boolean isFull() { return size == capacity; }

    /** Clears all elements without reallocating the backing array. */
    public void clear() {
        writeIndex = 0;
        size       = 0;
    }

    // =========================================================================
    // Primitive specialization — double
    // =========================================================================

    /**
     * Fixed-capacity circular ring buffer for primitive {@code double} values.
     * Zero autoboxing.
     *
     * @author TyouDm
     */
    public static final class OfDouble {

        private final double[] data;
        private final int      capacity;
        private int            writeIndex;
        private int            size;

        /**
         * Creates a new double ring buffer.
         *
         * @param capacity maximum number of elements (must be &gt; 0)
         */
        public OfDouble(final int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("RingBuffer capacity must be > 0, got: " + capacity);
            }
            this.capacity   = capacity;
            this.data       = new double[capacity];
            this.writeIndex = 0;
            this.size       = 0;
        }

        /** Adds a value, overwriting the oldest when full. */
        public void add(final double value) {
            data[writeIndex] = value;
            writeIndex = (writeIndex + 1) % capacity;
            if (size < capacity) size++;
        }

        /**
         * Returns the element at logical index {@code i} (0 = oldest).
         *
         * @param i logical index
         * @return element at index {@code i}
         */
        public double get(final int i) {
            if (i < 0 || i >= size) {
                throw new IndexOutOfBoundsException("Index " + i + " out of bounds for size " + size);
            }
            return data[(writeIndex - size + i + capacity) % capacity];
        }

        /** Returns the newest value. */
        public double newest() {
            if (size == 0) throw new IllegalStateException("RingBuffer is empty.");
            return get(size - 1);
        }

        /** Returns the oldest value. */
        public double oldest() {
            if (size == 0) throw new IllegalStateException("RingBuffer is empty.");
            return get(0);
        }

        /** Computes the arithmetic mean of all stored values. Returns 0 if empty. */
        public double mean() {
            if (size == 0) return 0.0;
            double sum = 0.0;
            for (int i = 0; i < size; i++) sum += get(i);
            return sum / size;
        }

        public int size()        { return size; }
        public int capacity()    { return capacity; }
        public boolean isEmpty() { return size == 0; }
        public boolean isFull()  { return size == capacity; }

        /** Clears all elements without reallocating. */
        public void clear() { writeIndex = 0; size = 0; }
    }

    // =========================================================================
    // Primitive specialization — long
    // =========================================================================

    /**
     * Fixed-capacity circular ring buffer for primitive {@code long} values.
     * Zero autoboxing.
     *
     * @author TyouDm
     */
    public static final class OfLong {

        private final long[] data;
        private final int    capacity;
        private int          writeIndex;
        private int          size;

        /**
         * Creates a new long ring buffer.
         *
         * @param capacity maximum number of elements (must be &gt; 0)
         */
        public OfLong(final int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("RingBuffer capacity must be > 0, got: " + capacity);
            }
            this.capacity   = capacity;
            this.data       = new long[capacity];
            this.writeIndex = 0;
            this.size       = 0;
        }

        /** Adds a value, overwriting the oldest when full. */
        public void add(final long value) {
            data[writeIndex] = value;
            writeIndex = (writeIndex + 1) % capacity;
            if (size < capacity) size++;
        }

        /**
         * Returns the element at logical index {@code i} (0 = oldest).
         *
         * @param i logical index
         * @return element at index {@code i}
         */
        public long get(final int i) {
            if (i < 0 || i >= size) {
                throw new IndexOutOfBoundsException("Index " + i + " out of bounds for size " + size);
            }
            return data[(writeIndex - size + i + capacity) % capacity];
        }

        /** Returns the newest value. */
        public long newest() {
            if (size == 0) throw new IllegalStateException("RingBuffer is empty.");
            return get(size - 1);
        }

        /** Returns the oldest value. */
        public long oldest() {
            if (size == 0) throw new IllegalStateException("RingBuffer is empty.");
            return get(0);
        }

        public int size()        { return size; }
        public int capacity()    { return capacity; }
        public boolean isEmpty() { return size == 0; }
        public boolean isFull()  { return size == capacity; }

        /** Clears all elements without reallocating. */
        public void clear() { writeIndex = 0; size = 0; }
    }

    // =========================================================================
    // Primitive specialization — int
    // =========================================================================

    /**
     * Fixed-capacity circular ring buffer for primitive {@code int} values.
     * Zero autoboxing.
     *
     * @author TyouDm
     */
    public static final class OfInt {

        private final int[] data;
        private final int   capacity;
        private int         writeIndex;
        private int         size;

        /**
         * Creates a new int ring buffer.
         *
         * @param capacity maximum number of elements (must be &gt; 0)
         */
        public OfInt(final int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("RingBuffer capacity must be > 0, got: " + capacity);
            }
            this.capacity   = capacity;
            this.data       = new int[capacity];
            this.writeIndex = 0;
            this.size       = 0;
        }

        /** Adds a value, overwriting the oldest when full. */
        public void add(final int value) {
            data[writeIndex] = value;
            writeIndex = (writeIndex + 1) % capacity;
            if (size < capacity) size++;
        }

        /**
         * Returns the element at logical index {@code i} (0 = oldest).
         *
         * @param i logical index
         * @return element at index {@code i}
         */
        public int get(final int i) {
            if (i < 0 || i >= size) {
                throw new IndexOutOfBoundsException("Index " + i + " out of bounds for size " + size);
            }
            return data[(writeIndex - size + i + capacity) % capacity];
        }

        /** Returns the newest value. */
        public int newest() {
            if (size == 0) throw new IllegalStateException("RingBuffer is empty.");
            return get(size - 1);
        }

        /** Returns the oldest value. */
        public int oldest() {
            if (size == 0) throw new IllegalStateException("RingBuffer is empty.");
            return get(0);
        }

        public int size()        { return size; }
        public int capacity()    { return capacity; }
        public boolean isEmpty() { return size == 0; }
        public boolean isFull()  { return size == capacity; }

        /** Clears all elements without reallocating. */
        public void clear() { writeIndex = 0; size = 0; }
    }
}
