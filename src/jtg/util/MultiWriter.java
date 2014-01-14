package jtg.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;

/**
 * A {@link Writer} that delegates its output to multiple other {@link Writer}s.
 * No guarantees are made of the order in which this is done.
 * 
 * A {@code MultiWriter} without any assigned {@code Writer}s discards any
 * input.
 * 
 * @author jesse
 * 
 */
public class MultiWriter extends Writer {

    private Collection<Writer> writers;

    {
        this.writers = new HashSet<>();
    }

    /**
     * Initializes this {@code MultiWriter} with no underlying {@link Writer}s.
     */
    public MultiWriter() {

    }

    /**
     * Initializes this {@code MultiWriter} with a single {@link Writer}.
     * 
     * @param writer The {@code Writer} to initialize with
     */
    public MultiWriter(Writer writer) {
        this.writers.add(writer);
    }

    /**
     * Initializes this {@code MultiWriter} with multiple {@link Writer}s,
     * delivered in a {@link Collection}.
     * 
     * @param writers The {@code Writer}s to add.
     */
    public MultiWriter(Collection<Writer> writers) {
        this.writers.addAll(writers);
    }

    /**
     * Initializes this {@code MultiWriter} with multiple {@link Writer}s,
     * delivered in an array.
     * 
     * @param writers The {@code Writer}s to add.
     */
    public MultiWriter(Writer[] writers) {
        for (Writer w : writers) {
            this.writers.add(w);
        }
    }

    public synchronized boolean addWriter(Writer writer) {
        return this.writers.add(writer);
    }

    /**
     * Removes a given {@link Writer}, and return {@code true} if it succeeded.
     * 
     * @param writer The {@code Writer} to remove.
     * @return True if {@code writer} was successfully removed.
     */
    public synchronized boolean removeWriter(Writer writer) {
        return this.writers.remove(writer);
    }

    @Override
    public synchronized void write(char[] cbuf, int off, int len) throws IOException {
        for (Writer w : this.writers) {
            w.write(cbuf, off, len);
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        for (Writer w : this.writers) {
            w.flush();
        }
    }

    @Override
    public synchronized void close() throws IOException {
        for (Writer w : this.writers) {
            w.close();
        }
    }
}
