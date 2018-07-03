/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.xcube16.data;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A {@link DataView} that stores values like a {@link Map}.
 */
public interface DataMap extends DataView<String> {

    /**
     * Gets a collection containing all keys in this {@link DataMap}.
     *
     * @return A set of current keys in this container
     */
    Set<String> getKeys();

    /**
     * Copies this {@link DataView} and all of it's contents into a new
     * {@link DataMap}.
     *
     * <p>Note that the copy will not have the same path as this
     * {@link DataView} since it will be constructed with the top level path
     * being itself.</p>
     *
     * @return The newly constructed data view
     */
    DataMap copy();

    @Override
    DataMap set(String key, Object element);

    @Override
    DataMap set(DataQuery query, Object value);

    @Override
    DataMap remove(String key);

    @Override
    DataMap remove(DataQuery path);

    @Override
    default void forEachKey(Consumer<String> consumer) {
        getKeys().forEach(consumer);
    }
}
