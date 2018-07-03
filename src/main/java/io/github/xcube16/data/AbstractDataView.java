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
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implements the queryable methods for DataView.
 * Node: It could be done with default methods in DataView in java 9 (private methods)
 *
 * @param <K> The key type
 */
public abstract class AbstractDataView<K> implements DataView<K> {

    /**
     * is a Primitive Allowed Type
     */
    static boolean isPrimitive(Object value) {
        return value instanceof Boolean ||
                value instanceof Byte ||
                value instanceof Character ||
                value instanceof Short ||
                value instanceof Integer ||
                value instanceof Long ||
                value instanceof Float ||
                value instanceof Double ||
                value instanceof String;
    }

    /**
     * is an Array Allowed Type
     */
    static boolean isPrimitiveArray(Object value) {
        return value instanceof boolean[] ||
                value instanceof byte[] ||
                value instanceof String ||
                value instanceof short[] ||
                value instanceof int[] ||
                value instanceof long[] ||
                value instanceof float[] ||
                value instanceof double[];
    }

    protected void copyCollection(K key, Collection<?> value) {
        DataList sublist = this.createList(key);
        for (Object object : value) {
            sublist.add(object);
        }
    }

    protected void copyMap(K key, Map<?, ?> value) {
        DataMap submap = this.createMap(key);
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            if (entry.getKey() instanceof String) {
                submap.set((String) entry.getKey(), entry.getValue());
            } else {
                throw new IllegalArgumentException("map had an unsupported key type");
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected void copyDataMap(K key, DataMap value) {
        checkArgument(!value.equals(this), "Cannot insert self-referencing Objects!");

        DataMap submap = this.createMap(key);
        for (String subkey : value.getKeys()) {
            submap.set(subkey, value.get(subkey).get());
        }
    }

    protected void copyDataList(K key, DataList value) {
        checkArgument(!value.equals(this), "Cannot insert self-referencing Objects!");

        DataList sublist = this.createList(key);
        for (int i = 0; i < value.size(); i++) {
            sublist.add(value.get(i).get());
        }
    }

    /*
     * ===========================
     * ==== queryable methods ====
     * ===========================
     */

    /**
     * Gets the {@link DataView} that contains a query's last element if it exists
     *
     * @param parts The parts of the query
     * @return The DataView, if available
     */
    private Optional<DataView> getHolder(List<String> parts) {
        DataView view = this;
        for (int i = 0; i < parts.size() - 1; i++) {
            Optional<Object> opt = get(view, parts.get(i));
            if (opt.isPresent() && opt.get() instanceof DataView) {
                view = (DataView) opt.get();
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(view);
    }

    /**
     * Gets the {@link DataView} that contains a query's last element,
     * creating {@link DataMap}'s for any element that does not exist along the way.
     *
     * @param parts The parts of the query
     * @return The DataView
     */
    private DataView getOrCreateHolder(List<String> parts) {
        DataView view = this;
        for (int i = 0; i < parts.size() - 1; i++) {
            Optional<Object> opt = get(view, parts.get(i));
            if (opt.isPresent() && opt.get() instanceof DataView) {
                view = (DataView) opt.get();
            } else {
                view = createMap(view, parts.get(i));
            }
        }
        return view;
    }

    /**
     * A lot like {@link DataView#get(Object)} but takes a query instead.
     *
     * @param path The path to the Object
     * @return The Object, if available
     */
    private Optional<Object> get(DataQuery path) {
        checkNotNull(path, "path");
        List<String> parts = path.getParts();

        if (parts.isEmpty()) {
            return Optional.of(this);
        }

        return getHolder(parts).map(v -> get(v, parts.get(parts.size() - 1)));
    }

    @Override
    public DataView<K> set(DataQuery path, Object value) {
        checkNotNull(path, "path");
        checkNotNull(value, "value");
        List<String> parts = path.getParts();
        checkArgument(parts.isEmpty(), "The query not be empty");

        set(getOrCreateHolder(parts), parts.get(parts.size() - 1), value);
        return this;
    }

    @Override
    public DataView<K> remove(DataQuery path) {
        checkNotNull(path, "path");
        List<String> parts = path.getParts();
        checkArgument(parts.isEmpty(), "The query can not be empty");

        getHolder(parts).ifPresent(v -> remove(v, parts.get(parts.size() - 1)));
        return this;
    }

    @Override
    public DataMap createMap(DataQuery path) {
        checkNotNull(path, "path");
        List<String> parts = path.getParts();
        checkArgument(parts.isEmpty(), "The query not be empty");

        return createMap(getOrCreateHolder(parts), parts.get(parts.size() - 1));
    }

    @Override
    public DataList createList(DataQuery path) {
        checkNotNull(path, "path");
        List<String> parts = path.getParts();
        checkArgument(parts.isEmpty(), "The query not be empty");

        return createList(getOrCreateHolder(parts), parts.get(parts.size() - 1));
    }

    @Override
    public Optional<DataMap> getMap(DataQuery path) {
        return this.get(path).flatMap(o -> Coerce2.asDataMap(o, path, this::createMap));
    }

    @Override
    public Optional<DataList> getList(DataQuery path) {
        return this.get(path).flatMap(o -> Coerce2.asDataList(o, path, this::createList));
    }

    @Override
    public Optional<Boolean> getBoolean(DataQuery path) {
        return this.get(path).flatMap(Coerce2::asBoolean);
    }

    @Override
    public Optional<Byte> getByte(DataQuery path) {
        return this.get(path).flatMap(Coerce2::asByte);
    }

    @Override
    public Optional<Character> getCharacter(DataQuery path) {
        return this.get(path).flatMap(Coerce2::asChar);
    }

    @Override
    public Optional<Short> getShort(DataQuery path) {
        return get(path).flatMap(Coerce2::asShort);
    }

    @Override
    public Optional<Integer> getInt(DataQuery path) {
        return get(path).flatMap(Coerce2::asInteger);
    }

    @Override
    public Optional<Long> getLong(DataQuery path) {
        return get(path).flatMap(Coerce2::asLong);
    }

    @Override
    public Optional<Float> getFloat(DataQuery path) {
        return get(path).flatMap(Coerce2::asFloat);
    }

    @Override
    public Optional<Double> getDouble(DataQuery path) {
        return get(path).flatMap(Coerce2::asDouble);
    }

    @Override
    public Optional<boolean[]> getBooleanArray(DataQuery path) {
        return get(path).flatMap(Coerce2::asBooleanArray);
    }

    @Override
    public Optional<byte[]> getByteArray(DataQuery path) {
        return get(path).flatMap(Coerce2::asByteArray);
    }

    @Override
    public Optional<String> getString(DataQuery path) {
        return get(path).flatMap(Coerce2::asString);
    }

    @Override
    public Optional<short[]> getShortArray(DataQuery path) {
        return get(path).flatMap(Coerce2::asShortArray);
    }

    @Override
    public Optional<int[]> getIntArray(DataQuery path) {
        return get(path).flatMap(Coerce2::asIntArray);
    }

    @Override
    public Optional<long[]> getLongArray(DataQuery path) {
        return get(path).flatMap(Coerce2::asLongArray);
    }

    @Override
    public Optional<float[]> getFloatArray(DataQuery path) {
        return get(path).flatMap(Coerce2::asFloatArray);
    }

    @Override
    public Optional<double[]> getDoubleArray(DataQuery path) {
        return get(path).flatMap(Coerce2::asDoubleArray);
    }

    /*
     * Helper methods
     */

    @SuppressWarnings("unchecked")
    private static Optional<Object> get(DataView view, String key) {
        return view.get(view.key(key));
    }
    @SuppressWarnings("unchecked")
    private static void set(DataView view, String key, Object value) {
        view.set(view.key(key), value);
    }
    @SuppressWarnings("unchecked")
    private static DataMap createMap(DataView view, String key) {
        return view.createMap(view.key(key));
    }
    @SuppressWarnings("unchecked")
    private static DataList createList(DataView view, String key) {
        return view.createList(view.key(key));
    }
    @SuppressWarnings("unchecked")
    private static void remove(DataView view, String key) {
        view.remove(view.key(key));
    }
}
