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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class MemoryDataValue implements DataValue {

    @Nullable
    private Object value;

    public MemoryDataValue() {
    }

    MemoryDataValue(@Nullable Object value) {
        this.setRaw(value);
    }

    public static MemoryDataValue of(Object value) {
        return new MemoryDataValue().set(value);
    }

    @Override
    public Optional<Object> get() {
        return Optional.ofNullable(this.value);
    }

    @Override
    public MemoryDataValue set(Object value) {
        // TODO: this TODO message is a duplicate of one another set() method... o and the code
        checkNotNull(value, "value");

        if (AbstractDataView.isPrimitive(value) || AbstractDataView.isPrimitiveArray(value)) { // Primitive Allowed Types or Array Allowed Types
            this.setRaw(value);

        } else if (value instanceof DataMap) { // Structure Allowed Types
            copyDataMap((DataMap) value);
        } else if (value instanceof DataList) { // Structure Allowed Types
            copyDataList((DataList) value);

        } else if (value instanceof DataSerializable) { // Serializable Object
            ((DataSerializable) value).toContainer(this);

        } else if (value instanceof Enum) { // common java stuff
            this.setRaw(((Enum) value).name());
        } else if (value instanceof Map) { // common java stuff
            copyMap((Map) value);
        } else if (value instanceof Collection) { // common java stuff
            copyCollection((Collection) value);

        } else {
            // nope, KU-BOOM!
            throw new IllegalArgumentException(value.getClass() + " can not be serialized");
        }
        return this;
    }

    // FIXME: duplicate code
    private void copyCollection(Collection<?> value) {
        DataList sublist = this.createList();
        for (Object object : value) {
            sublist.add(object);
        }
    }

    // FIXME: duplicate code
    private void copyMap(Map<?, ?> value) {
        DataMap submap = this.createMap();
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            if (entry.getKey() instanceof String) {
                submap.set((String) entry.getKey(), entry.getValue());
            } else {
                throw new IllegalArgumentException("map had an unsupported key type");
            }
        }
    }

    // FIXME: duplicate code
    @SuppressWarnings("ConstantConditions")
    private void copyDataMap(DataMap value) {

        DataMap submap = this.createMap();
        for (String subkey : value.getKeys()) {
            submap.set(subkey, value.get(subkey).get());
        }
    }

    // FIXME: duplicate code
    private void copyDataList(DataList value) {

        DataList sublist = this.createList();
        for (int i = 0; i < value.size(); i++) {
            sublist.add(value.get(i).get());
        }
    }

    private void setRaw(Object value) {
        this.value = value;
    }

    @Override
    public void clear() {
        this.value = null;
    }

    @Override
    public DataMap createMap() {
        return (DataMap) (this.value = new MemoryDataMap());
    }

    @Override
    public DataList createList() {
        return (DataList) (this.value = new MemoryDataList());
    }
}
