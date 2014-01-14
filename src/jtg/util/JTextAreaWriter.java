package jtg.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.swing.JTextArea;

/**
 * Writes to a {@link JTextArea}. Meant to be used in combination with a
 * {@link PrintWriter} or similar.
 * 
 * @author jesse
 */
public class JTextAreaWriter extends Writer {

    private JTextArea textArea;

    public JTextAreaWriter(JTextArea textArea) {
        super();
        this.textArea = textArea;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        this.textArea.append(String.copyValueOf(cbuf, off, len));

    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {
        this.flush();
    }

}
