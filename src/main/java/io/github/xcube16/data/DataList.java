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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A {@link DataView} that stores values like a {@link List}.
 */
public interface DataList extends DataView<Integer> {

    /**
     * Adds an element to the end of the list.
     *
     * <p>The element must be one of<br/>
     * * Allowed Types<br/>
     * * {@link DataSerializable}<br/>
     * * {@link Enum} (the enum's name will be stored as a {@link String})<br/>
     * * {@link Map} (will be coerced into a DataMap, or error on failure)<br/>
     * * {@link Collection} (will be coerced into a {@link DataList}/array, or error on failure)</p>
     *
     * @param element The element to add
     * @return This list, for chaining
     * @throws IllegalArgumentException thrown when {@code element} is of an unsupported type
     */
    DataList add(Object element);

    /**
     * Creates a new {@link DataMap} and adds it to the end of the list.
     *
     * @return The newly created {@link DataMap}
     */
    DataMap addMap();

    /**
     * Creates a new {@link DataList} and adds it to the end of the list.
     *
     * @return The newly created {@link DataList}
     */
    DataList addList();

    @Override
    DataList set(Integer index, Object element);

    @Override
    DataList set(DataQuery query, Object value);

    @Override
    DataList remove(Integer index);

    @Override
    DataList remove(DataQuery path);

    @Override
    default void forEachKey(Consumer<Integer> consumer) {
        for (int i = 0; i < this.size(); i++) {
            consumer.accept(i);
        }
    }
}
