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
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of a {@link DataView} being used in memory.
 */
public class MemoryDataMap extends AbstractDataMap {

    protected final Map<String, Object> map = Maps.newLinkedHashMap();

    public MemoryDataMap() {}

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public Set<String> getKeys() {
        return this.map.keySet();
    }

    @Override
    public Optional<Object> get(String key) {
        checkNotNull(key, "key");

        return Optional.ofNullable(this.map.get(key));
    }

    @Override
    public void setRaw(String key, Object value) {
        this.map.put(key, value);
    }

    @Override
    public MemoryDataMap remove(String key) {
        checkNotNull(key, "key");
        this.map.remove(key);
        return this;
    }

    @Override
    public DataMap createMap(String key) {
        checkNotNull(key, "key");

        DataMap result = new MemoryDataMap();
        this.setRaw(key, result);
        return result;
    }

    @Override
    public DataList createList(String key) {
        checkNotNull(key, "key");

        DataList result = new MemoryDataList();
        this.setRaw(key, result);
        return result;
    }

    @Override
    public DataMap copy() {
        final DataMap container = new MemoryDataMap();
        getKeys()
                .forEach(key ->
                        get(key).ifPresent(obj ->
                                container.set(key, obj)
                        )
                );
        return container;
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final MemoryDataMap other = (MemoryDataMap) obj;

        return Objects.equal(this.map.entrySet(), other.map.entrySet());
    }

    @Override
    public String toString() {
        final MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
        return helper.add("map", this.map).toString();
    }
}
