/*
 * Dicer - OBO ID range library
 * Copyright © 2025 Damien Goutte-Gattat
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   (1) Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *   (2) Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 *   (3) Neither the name of the copyright holder nor the names its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.incenp.obofoundry.dicer.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

/**
 * A helper class to read a TSV/CSV file.
 */
public class TSVReader {

    private BufferedReader reader;
    private List<ITSVListener> listeners = new ArrayList<>();
    private int separator = -1;

    /**
     * Creates a new instance.
     * 
     * @param file The file to read from.
     * @throws FileNotFoundException If the specified file does not exist.
     */
    public TSVReader(File file) throws FileNotFoundException {
        reader = new BufferedReader(new FileReader(file));
    }

    /**
     * Creates a new instance.
     * 
     * @param stream The stream to read from.
     */
    public TSVReader(InputStream stream) {
        reader = new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * Creates a new instance.
     * 
     * @param filename The name of the file to read from. If {@code -}, the object
     *                 will read from standard input.
     * @throws FileNotFoundException If the specified file does not exist.
     */
    public TSVReader(String filename) throws FileNotFoundException {
        if ( filename.equals("-") ) {
            reader = new BufferedReader(new InputStreamReader(System.in));
        } else {
            reader = new BufferedReader(new FileReader(new File(filename)));
        }
    }

    /**
     * Sets the expected column separator character. If set to -1 (which is the
     * default), the reader will attempt to infer the separator by peeking into the
     * first 64 bytes of the header line.
     * 
     * @param separator The column separator character, or -1 to let the reader
     *                  attempt to guess the separator.
     */
    public void setSeparator(int separator) {
        this.separator = separator;
    }

    /**
     * Adds a listener object to react to events emitted by this reader.
     * 
     * @param listener The listener to add.
     */
    public void addListener(ITSVListener listener) {
        listeners.add(listener);
    }

    /**
     * Reads the file.
     * 
     * @throws IOException If any I/O error occurs.
     */
    public void read() throws IOException {
        String comment = null;
        while ( (comment = readComment()) != null ) {
            onComment(comment);
        }

        if ( separator == -1 ) {
            separator = peekSeparator();
        }
        CsvMapper mapper = new CsvMapper();
        MappingIterator<List<String>> it = mapper.readerForListOf(String.class)
                .with(CsvSchema.emptySchema().withColumnSeparator((char) separator))
                .with(CsvParser.Feature.WRAP_AS_ARRAY)
                .readValues(reader);
        if ( it.hasNext() ) {
            onHeader(it.next());
        }
        while ( it.hasNext() ) {
            onRow(it.next());
        }

        reader.close();
    }

    /**
     * Called when a comment line is read.
     * 
     * @param comment The comment line, excluding the initial '#' character and the
     *                terminating newline character.
     */
    protected void onComment(String comment) {
        for ( ITSVListener listener : listeners ) {
            listener.onComment(comment);
        }
    }

    /**
     * Called when the first non-comment line is read.
     * 
     * @param header The values from the first non-comment line.
     */
    protected void onHeader(List<String> header) {
        for ( ITSVListener listener : listeners ) {
            listener.onHeader(header, (char) separator);
        }
    }

    /**
     * Called when a data row is read.
     * 
     * @param row The values from the row that has just been read.
     */
    protected void onRow(List<String> row) {
        for ( ITSVListener listener : listeners ) {
            listener.onRow(row);
        }
    }

    private String readComment() throws IOException {
        StringBuilder sb;
        boolean done;
        int c;

        reader.mark(1);
        c = reader.read();
        if ( c != '#' ) {
            reader.reset();
            return null;
        }

        sb = new StringBuilder();
        done = false;
        while ( !done ) {
            c = reader.read();
            if ( c == -1 || c == '\n' ) {
                done = true;
            } else {
                sb.append((char) c);
            }
        }

        return sb.toString();
    }

    private char peekSeparator() throws IOException {
        reader.mark(64);
        int i, n = 0, sep = -1;
        do {
            i = reader.read();
            if ( i == '\t' || i == ',' ) {
                sep = i;
            }
            n += 1;
        } while ( sep == -1 && i != -1 && n < 64 );
        reader.reset();

        return sep != -1 ? (char) sep : '\t';
    }
}
