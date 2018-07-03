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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of a {@link DataView} being used in memory.
 */
public class MemoryDataList extends AbstractDataList {

    protected final List<Object> list = Lists.newArrayList();

    public MemoryDataList() {}

    @Override
    public int size() {
        return this.list.size();
    }

    private boolean contains(Integer key) {
        return key >= 0 && key < this.size();
    }

    @Override
    public Optional<Object> get(Integer key) {
        checkNotNull(key, "key");

        return this.contains(key) ? Optional.of(this.list.get(key)) : Optional.empty();
    }

    @Override
    public void setRaw(Integer key, Object value) {
        if (key == this.size()) {
            addRaw(value);
        } else {
            this.list.set(key, value);
        }
    }

    @Override
    public void addRaw(Object value) {
        this.list.add(value);
    }

    @Override
    public MemoryDataList remove(Integer key) {
        checkNotNull(key, "key");
        this.list.remove(key);
        return this;
    }

    @Override
    public DataMap createMap(Integer key) {
        checkNotNull(key, "key");

        DataMap result = new MemoryDataMap();
        this.setRaw(key, result);
        return result;
    }

    @Override
    public DataList createList(Integer key) {
        checkNotNull(key, "key");

        DataList result = new MemoryDataList();
        this.setRaw(key, result);
        return result;
    }

    @Override
    public DataMap addMap() {
        return this.createMap(this.size());
    }

    @Override
    public DataList addList() {
        return this.createList(this.size());
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public void clear() {
        this.list.clear();
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final MemoryDataList other = (MemoryDataList) obj;

        return Objects.equal(this.list, other.list);
    }

    @Override
    public String toString() {
        final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
        return helper.add("list", this.list).toString();
    }
}
