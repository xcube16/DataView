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
import java.util.Optional;

/**
 * Can store a single value/element.<br/>
 * <p>The value must be absent or one of {@link DataView} Allowed Types.</p>
 */
public interface DataValue {

    /**
     * Gets the value as an object or absent.
     *
     * <p>The returned Object shall be one of the Allowed Types.</p>
     *
     * <p>Warning: Inconsistency leak!
     * Only use this method if you don't care what specific type the returned Object is.
     * Example: If you set an Integer, get() may not return an Integer,
     * but another Allowed Type that can losslessly be coerced into an Integer!</p>
     *
     * @return The Object, if available
     */
    Optional<Object> get();

    /**
     * <p>Sets the value to the given Object</p>
     *
     * <p>The value must be one of<br/>
     * * Allowed Types<br/>
     * * {@link DataSerializable}<br/>
     * * {@link Enum} (the enum's name will be stored as a {@link String})<br/>
     * * {@link Map} (will be coerced into a {@link DataMap}, or error on failure)<br/>
     * * {@link Collection} (will be coerced into a {@link DataList}/array, or error on failure)</p>
     *
     * @param value The value of the data
     * @throws IllegalArgumentException thrown when {@code value} is of an unsupported type
     */
    DataValue set(Object value);

    /**
     * Removes the value.
     */
    void clear();

    /**
     * Creates a new {@link DataMap}.
     * <p>If the value was not absent, that value will be
     * overwritten with the newly constructed {@link DataMap}.</p>
     *
     * @return The newly created {@link DataMap}
     */
    DataMap createMap();

    /**
     * Creates a new {@link DataList}.
     * <p>If the value was not absent, that value will be
     * overwritten with the newly constructed {@link DataList}.</p>
     *
     * @return The newly created {@link DataList}
     */
    DataList createList();

    // TODO: add create methods that work well with super classes when serializing!

    /**
     * Gets a {@link DataMap}, if available.
     *
     * <p>If the value is not a {@link DataMap}
     * and can not be coerced into a {@link DataMap}, an absent is
     * returned.</p>
     *
     * @return The {@link DataMap}, if available
     */
    default Optional<DataMap> getMap() {
        return this.get().flatMap(o -> Coerce2.asDataMap(o, null, (ignored) -> this.createMap()));
    }

    /**
     * Gets a {@link DataList}, if available.
     *
     * <p>If the value is not a
     * {@link DataList} or Array Allowed Types,
     * an absent is returned.</p>
     *
     * <p>Implementation note: If the value is an Array Allowed Types,
     * it must also be converted into the {@link DataList} in order for mutations to work.<p/>
     *
     * @return The {@link DataList}, if available
     */
    default Optional<DataList> getList() {
        return this.get().flatMap(o -> Coerce2.asDataList(o, null, (ignored) -> this.createList()));
    }

    /**
     * Gets a {@link Boolean}, if available.
     *
     * <p>If the value is not a {@link Boolean}
     * and can not be coerced into a {@link Boolean}, an absent is returned.</p>
     *
     * @return The {@link Boolean}, if available
     */
    default Optional<Boolean> getBoolean() {
        return this.get().flatMap(Coerce2::asBoolean);
    }

    /**
     * Gets a {@link Byte}, if available.
     *
     * <p>If the value is not a {@link Byte}
     * and can not be coerced into a {@link Byte}, an absent is returned.</p>
     *
     * @return The {@link Byte}, if available
     */
    default Optional<Byte> getByte() {
        return this.get().flatMap(Coerce2::asByte);
    }

    /**
     * Gets a {@link Character}, if available.
     *
     * <p>If the value is not a {@link Character}
     * and can not be coerced into a {@link Character}, an absent is returned.</p>
     *
     * @return The {@link Character}, if available
     */
    default Optional<Character> getCharacter() {
        return this.get().flatMap(Coerce2::asChar);
    }

    /**
     * Gets a {@link Short}, if available.
     *
     * <p>If the value is not a {@link Short}
     * and can not be coerced into a {@link Short}, an absent is returned.</p>
     *
     * @return The {@link Short}, if available
     */
    default Optional<Short> getShort() {
        return this.get().flatMap(Coerce2::asShort);
    }

    /**
     * Gets a {@link Integer}, if available.
     *
     * <p>If the value is not a {@link Integer}
     * and can not be coerced into a {@link Integer}, an absent is returned.</p>
     *
     * @return The {@link Integer}, if available
     */
    default Optional<Integer> getInt() {
        return this.get().flatMap(Coerce2::asInteger);
    }

    /**
     * Gets a {@link Long}, if available.
     *
     * <p>If the value is not a {@link Long}
     * and can not be coerced into a {@link Long}, an absent is returned.</p>
     *
     * @return The {@link Long}, if available
     */
    default Optional<Long> getLong() {
        return this.get().flatMap(Coerce2::asLong);
    }

    /**
     * Gets a {@link Float}, if available.
     *
     * <p>If the value is not a {@link Float}
     * and can not be coerced into a {@link Float}, an absent is returned.</p>
     *
     * @return The {@link Float}, if available
     */
    default Optional<Float> getFloat() {
        return this.get().flatMap(Coerce2::asFloat);
    }

    /**
     * Gets a {@link Double}, if available.
     *
     * <p>If the value is not a {@link Double}
     * and can not be coerced into a {@link Double}, an absent is returned.</p>
     *
     * @return The {@link Double}, if available
     */
    default Optional<Double> getDouble() {
        return this.get().flatMap(Coerce2::asDouble);
    }

    /**
     * Gets a boolean array, if available.
     *
     * <p>If the boolean array does not exist,
     * or the value can not be coerced into a boolean array,
     * an absent is returned.</p>
     *
     * @return The boolean array, if available
     */
    default Optional<boolean[]> getBooleanArray() {
        return this.get().flatMap(Coerce2::asBooleanArray);
    }

    /**
     * Gets a byte array, if available.
     *
     * <p>If the byte array does not exist,
     * or the value can not be coerced into a byte array,
     * an absent is returned.</p>
     *
     * @return The byte array, if available
     */
    default Optional<byte[]> getByteArray() {
        return this.get().flatMap(Coerce2::asByteArray);
    }

    /**
     * Gets a {@link String}, if available.
     *
     * <p>If the value is not a {@link String}
     * and can not be coerced into a {@link String}, an absent is returned.</p>
     *
     * @return The {@link String}, if available
     */
    default Optional<String> getString() {
        return this.get().flatMap(Coerce2::asString);
    }

    /**
     * Gets a short array, if available.
     *
     * <p>If the short array does not exist,
     * or the value can not be coerced into a short array,
     * an absent is returned.</p>
     *
     * @return The short array, if available
     */
    default Optional<short[]> getShortArray() {
        return this.get().flatMap(Coerce2::asShortArray);
    }

    /**
     * Gets a int array, if available.
     *
     * <p>If the int array does not exist,
     * or the value can not be coerced into a int array,
     * an absent is returned.</p>
     *
     * @return The int array, if available
     */
    default Optional<int[]> getIntegerArray() {
        return this.get().flatMap(Coerce2::asIntArray);
    }

    /**
     * Gets a long array, if available.
     *
     * <p>If the long array does not exist,
     * or the value can not be coerced into a long array,
     * an absent is returned.</p>
     *
     * @return The long array, if available
     */
    default Optional<long[]> getLongArray() {
        return this.get().flatMap(Coerce2::asLongArray);
    }

    /**
     * Gets a float array, if available.
     *
     * <p>If the float array does not exist,
     * or the value can not be coerced into a float array,
     * an absent is returned.</p>
     *
     * @return The float array, if available
     */
    default Optional<float[]> getFloatArray() {
        return this.get().flatMap(Coerce2::asFloatArray);
    }

    /**
     * Gets a double array, if available.
     *
     * <p>If the double array does not exist,
     * or the value can not be coerced into a double array,
     * an absent is returned.</p>
     *
     * @return The double array, if available
     */
    default Optional<double[]> getDoubleArray() {
        return this.get().flatMap(Coerce2::asDoubleArray);
    }
}
