/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io.output;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * A {@link FilterWriter} that throws {@link UncheckedIOException} instead of {@link IOException}.
 *
 * @see FilterWriter
 * @see IOException
 * @see UncheckedIOException
 * @since 2.12.0
 */
public class UncheckedFilterWriter extends FilterWriter {

    /**
     * Creates a new filtered writer.
     *
     * @param writer a Writer object providing the underlying stream.
     * @return a new UncheckedFilterReader.
     * @throws NullPointerException if {@code writer} is {@code null}.
     */
    public static UncheckedFilterWriter on(final Writer writer) {
        return new UncheckedFilterWriter(writer);
    }

    /**
     * Creates a new filtered writer.
     *
     * @param writer a Writer object providing the underlying stream.
     * @throws NullPointerException if {@code writer} is {@code null}.
     */
    protected UncheckedFilterWriter(Writer writer) {
        super(writer);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public Writer append(char c) throws UncheckedIOException {
        try {
            return super.append(c);
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public Writer append(CharSequence csq) throws UncheckedIOException {
        try {
            return super.append(csq);
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public Writer append(CharSequence csq, int start, int end) throws UncheckedIOException {
        try {
            return super.append(csq, start, end);
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void close() throws UncheckedIOException {
        try {
            super.close();
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void flush() throws UncheckedIOException {
        try {
            super.flush();
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    private UncheckedIOException uncheck(IOException e) {
        return new UncheckedIOException(e);
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(char[] cbuf) throws UncheckedIOException {
        try {
            super.write(cbuf);
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws UncheckedIOException {
        try {
            super.write(cbuf, off, len);
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(int c) throws UncheckedIOException {
        try {
            super.write(c);
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(String str) throws UncheckedIOException {
        try {
            super.write(str);
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

    /**
     * Calls this method's super and rethrow {@link IOException} as {@link UncheckedIOException}.
     */
    @Override
    public void write(String str, int off, int len) throws UncheckedIOException {
        try {
            super.write(str, off, len);
        } catch (IOException e) {
            throw uncheck(e);
        }
    }

}
