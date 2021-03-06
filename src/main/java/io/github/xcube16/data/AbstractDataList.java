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
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implements implementation independent details of DataList.
 */
public abstract class AbstractDataList extends AbstractDataView<Integer> implements DataList {

    /**
     * An internal method that adds a raw value in the underlying data structure.
     * The value is already be sanitized and ready to go.
     */
    protected abstract void addRaw(Object value);

    @Override
    public DataList set(Integer key, Object value) {
        super.set(key, value);
        return this;
    }

    @Override
    public DataList add(Object value) {
        return this.set(this.size(), value);
    }

    /*
     * ===========================
     * ==== queryable methods ====
     * ===========================
     */

    private static final Integer INVALID_KEY = -1;

    @Override
    public Integer key(String key) {
        try {
            return Integer.valueOf(key);
        } catch (NumberFormatException e) {
            return INVALID_KEY;
        }
    }

    @Override
    public DataList set(DataQuery path, Object value) {
        super.set(path, value);
        return this;
    }

    @Override
    public DataList remove(DataQuery path) {
        super.remove(path);
        return this;
    }
}