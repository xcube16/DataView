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

import java.lang.reflect.Array;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class for coercing unknown values to specific target types.
 */
public final class Coerce2 {

    /**
     * No subclasses for you.
     */
    private Coerce2() {}

    /**
     * Gets the given object as a {@link Boolean}.
     *
     * @param obj The object to translate
     * @return The boolean, if available
     */
    public static Optional<Boolean> asBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return Optional.of((Boolean) obj);
        }

        Optional<Integer> optional = asInteger(obj);
        if (optional.isPresent()) {
            return optional.map(i -> i != 0); // 0 = false, anything else = true (just like C)
        }

        String str = obj.toString().trim();
        if (str.equalsIgnoreCase("true")
                || str.equalsIgnoreCase("yes")
                || str.equalsIgnoreCase("t")
                || str.equalsIgnoreCase("y")) {
            return Optional.of(true);
        }
        if (str.equalsIgnoreCase("false")
                || str.equalsIgnoreCase("no")
                || str.equalsIgnoreCase("f")
                || str.equalsIgnoreCase("n")) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    /**
     * Gets the given object as a {@link Byte}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The byte value, if available
     */
    public static Optional<Byte> asByte(Object obj) {
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).byteValue());
        }

        try {
            return Optional.of(Byte.valueOf(Coerce2.sanitiseNumber(obj)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets the given object as a {@link Character}.
     *
     * @param obj The object to translate
     * @return The character, if available
     */
    public static Optional<Character> asChar(Object obj) {
        if (obj instanceof Character) {
            return Optional.of((Character) obj);
        }

        String str = obj.toString();
        if (str.length() > 0) {
            return Optional.of(str.charAt(0));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Gets the given object as a {@link Short}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The short value, if available
     */
    public static Optional<Short> asShort(Object obj) {
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).shortValue());
        }

        try {
            // use parseFloat() so dots don't cause it to fail
            return Optional.of((short) Float.parseFloat(Coerce2.sanitiseNumber(obj)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets the given object as a {@link Integer}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The integer value, if available
     */
    public static Optional<Integer> asInteger(Object obj) {
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).intValue());
        }

        try {
            // use parseDouble() so dots don't cause it to fail
            return Optional.of((int) Double.parseDouble(Coerce2.sanitiseNumber(obj)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets the given object as a {@link Long}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The long value, if available
     */
    public static Optional<Long> asLong(Object obj) {
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).longValue());
        }

        String str = Coerce2.sanitiseNumber(obj);

        try {
            return Optional.of(Long.valueOf(str));
        } catch (NumberFormatException e) {
            try {
                return Optional.of((long) Double.parseDouble(str));
            } catch (NumberFormatException e2) {
                return Optional.empty();
            }
        }
    }

    /**
     * Gets the given object as a {@link Float}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The float value, if available
     */
    public static Optional<Float> asFloat(Object obj) {
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).floatValue());
        }

        try {
            return Optional.of(Float.valueOf(Coerce2.sanitiseNumber(obj)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets the given object as a {@link Double}.
     *
     * <p>Note that this does not translate numbers spelled out as strings.</p>
     *
     * @param obj The object to translate
     * @return The double value, if available
     */
    public static Optional<Double> asDouble(Object obj) {
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).doubleValue());
        }

        try {
            return Optional.of(Double.valueOf(Coerce2.sanitiseNumber(obj)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets the given object as a boolean[].
     *
     * @param obj The object to translate
     * @return The boolean[], if available
     */
    public static Optional<boolean[]> asBooleanArray(Object obj) {
        if (obj instanceof boolean[]) {
            return Optional.of((boolean[]) obj); // fast path
        }

        if (obj instanceof DataList) {
            DataList list = (DataList) obj;

            boolean[] array = new boolean[list.size()];
            for (int i = 0; i < array.length; i++) {
                Optional<Boolean> opt = list.getBoolean(i);
                if (!opt.isPresent()) {
                    return Optional.empty(); // We hit something that can not be turned into a boolean!
                }
                array[i] = opt.get();
            }
            return Optional.of(array);
        }

        Optional<NumArray> numsOpt = wrapNumArray(obj);
        if (numsOpt.isPresent()) {
            NumArray numbers = numsOpt.get();
            boolean[] booleans = new boolean[numbers.size()];

            for (int i = 0; i < numbers.size(); i++) {
                booleans[i] = numbers.intValue(i) != 0; // 0 = false, anything else = true (just like C)
            }
            return Optional.of(booleans);
        }

        return Optional.empty();
    }

    /**
     * Gets the given object as a byte[].
     *
     * @param obj The object to translate
     * @return The byte[], if available
     */
    public static Optional<byte[]> asByteArray(Object obj) {
        if (obj instanceof byte[]) {
            return Optional.of((byte[]) obj); // fast path
        }

        Optional<NumArray> numsOpt = wrapNumArray(obj);
        if (numsOpt.isPresent()) {
            NumArray numbers = numsOpt.get();
            byte[] bytes = new byte[numbers.size()];

            for (int i = 0; i < numbers.size(); i++) {
                bytes[i] = (byte) numbers.intValue(i);
            }
            return Optional.of(bytes);

        } else if (obj instanceof DataList) {
            DataList list = (DataList) obj;

            byte[] bytes = new byte[list.size()];
            for (int i = 0; i < bytes.length; i++) {
                Optional<Byte> opt = list.getByte(i);
                if (!opt.isPresent()) {
                    return Optional.empty();
                }
                bytes[i] = opt.get();
            }
            return Optional.of(bytes);
        }
        return Optional.empty();
    }

    /**
     * Gets the given object as a {@link String}.
     *
     * @param obj The object to translate
     * @return The boolean, if available
     */
    public static Optional<String> asString(Object obj) {
        if (obj instanceof char[]) {
            return Optional.of(String.valueOf((char[]) obj));
        }

        if (obj instanceof DataList) {
            DataList list = (DataList) obj;

            char[] chars = new char[list.size()];
            for (int i = 0; i < chars.length; i++) {
                Optional<Character> opt = list.getCharacter(i);
                if (!opt.isPresent()) {
                    return Optional.empty();
                }
                chars[i] = opt.get();
            }
            return Optional.of(new String(chars)); // TODO: Maybe optimize for large strings? (remove extra allocation?)
        }
        return Optional.of(obj.toString());
    }

    /**
     * Gets the given object as a short[].
     *
     * @param obj The object to translate
     * @return The short[], if available
     */
    public static Optional<short[]> asShortArray(Object obj) {
        if (obj instanceof short[]) {
            return Optional.of((short[]) obj); // fast path
        }

        Optional<NumArray> numsOpt = wrapNumArray(obj);
        if (numsOpt.isPresent()) {
            NumArray numbers = numsOpt.get();
            short[] shorts = new short[numbers.size()];

            for (int i = 0; i < numbers.size(); i++) {
                shorts[i] = (short) numbers.intValue(i);
            }
            return Optional.of(shorts);
        } else if (obj instanceof DataList) {
            DataList list = (DataList) obj;

            short[] shorts = new short[list.size()];
            for (int i = 0; i < shorts.length; i++) {
                Optional<Short> opt = list.getShort(i);
                if (!opt.isPresent()) {
                    return Optional.empty();
                }
                shorts[i] = opt.get();
            }
            return Optional.of(shorts);
        }
        return Optional.empty();
    }

    /**
     * Gets the given object as a int[].
     *
     * @param obj The object to translate
     * @return The int[], if available
     */
    public static Optional<int[]> asIntArray(Object obj) {
        if (obj instanceof int[]) {
            return Optional.of((int[]) obj); // fast path
        }

        Optional<NumArray> numsOpt = wrapNumArray(obj);
        if (numsOpt.isPresent()) {
            NumArray numbers = numsOpt.get();
            int[] ints = new int[numbers.size()];

            for (int i = 0; i < numbers.size(); i++) {
                ints[i] = numbers.intValue(i);
            }
            return Optional.of(ints);
        } else if (obj instanceof DataList) {
            DataList list = (DataList) obj;

            int[] ints = new int[list.size()];
            for (int i = 0; i < ints.length; i++) {
                Optional<Integer> opt = list.getInt(i);
                if (!opt.isPresent()) {
                    return Optional.empty();
                }
                ints[i] = opt.get();
            }
            return Optional.of(ints);
        }
        return Optional.empty();
    }

    /**
     * Gets the given object as a long[].
     *
     * @param obj The object to translate
     * @return The long[], if available
     */
    public static Optional<long[]> asLongArray(Object obj) {
        if (obj instanceof long[]) {
            return Optional.of((long[]) obj); // fast path
        }

        Optional<NumArray> numsOpt = wrapNumArray(obj);
        if (numsOpt.isPresent()) {
            NumArray numbers = numsOpt.get();
            long[] longs = new long[numbers.size()];

            for (int i = 0; i < numbers.size(); i++) {
                longs[i] = numbers.longValue(i);
            }
            return Optional.of(longs);
        } else if (obj instanceof DataList) {
            DataList list = (DataList) obj;

            long[] longs = new long[list.size()];
            for (int i = 0; i < longs.length; i++) {
                Optional<Long> opt = list.getLong(i);
                if (!opt.isPresent()) {
                    return Optional.empty();
                }
                longs[i] = opt.get();
            }
            return Optional.of(longs);
        }
        return Optional.empty();
    }

    /**
     * Gets the given object as a float[].
     *
     * @param obj The object to translate
     * @return The float[], if available
     */
    public static Optional<float[]> asFloatArray(Object obj) {
        if (obj instanceof float[]) {
            return Optional.of((float[]) obj); // fast path
        }

        Optional<NumArray> numsOpt = wrapNumArray(obj);
        if (numsOpt.isPresent()) {
            NumArray numbers = numsOpt.get();
            float[] floats = new float[numbers.size()];

            for (int i = 0; i < numbers.size(); i++) {
                floats[i] = numbers.floatValue(i);
            }
            return Optional.of(floats);
        } else if (obj instanceof DataList) {
            DataList list = (DataList) obj;

            float[] floats = new float[list.size()];
            for (int i = 0; i < floats.length; i++) {
                Optional<Float> opt = list.getFloat(i);
                if (!opt.isPresent()) {
                    return Optional.empty();
                }
                floats[i] = opt.get();
            }
            return Optional.of(floats);
        }
        return Optional.empty();
    }

    /**
     * Gets the given object as a double[].
     *
     * @param obj The object to translate
     * @return The double[], if available
     */
    public static Optional<double[]> asDoubleArray(Object obj) {
        if (obj instanceof double[]) {
            return Optional.of((double[]) obj); // fast path
        }

        Optional<NumArray> numsOpt = wrapNumArray(obj);
        if (numsOpt.isPresent()) {
            NumArray numbers = numsOpt.get();
            double[] doubles = new double[numbers.size()];

            for (int i = 0; i < numbers.size(); i++) {
                doubles[i] = numbers.doubleValue(i);
            }
            return Optional.of(doubles);
        } else if (obj instanceof DataList) {
            DataList list = (DataList) obj;

            double[] doubles = new double[list.size()];
            for (int i = 0; i < doubles.length; i++) {
                Optional<Double> opt = list.getDouble(i);
                if (!opt.isPresent()) {
                    return Optional.empty();
                }
                doubles[i] = opt.get();
            }
            return Optional.of(doubles);
        }
        return Optional.empty();
    }

    public static <K> Optional<DataMap> asDataMap(Object obj, K key, Function<K, DataMap> create) {
        if (obj instanceof DataMap) {
            return Optional.of((DataMap) obj);
        }
        // TODO: turn lists/arrays into DataMaps by turning indexes into keys?
        return Optional.empty();
    }

    public static <K> Optional<DataList> asDataList(Object obj, K key, Function<K, DataList> create) {
        if (obj instanceof DataList) {
            return Optional.of((DataList) obj);
        } else if (obj instanceof String) {
            String str = (String) obj;

            DataList list = create.apply(key);
            for (int i = 0; i < str.length(); i++) {
                list.add(str.charAt(i));
            }
            return Optional.of(list);
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);

            DataList list = create.apply(key);
            for (int i = 0; i < len; i++) {
                list.add(Array.get(obj, i));
            }
            return Optional.of(list);
        }

        return Optional.empty();
    }

    /**
     * Gets the given object as a DataView Allowed Type, or {@link Enum}, if available.
     *
     * <p>If {@code type} is an {@link Enum} and {@code obj}
     * can be coerced into a {@link String} representing the specific {@link Enum}
     * value's name, present is returned.</p>
     *
     * @param <T> The type of object
     * @param type The class of the object
     * @return The deserialized object, if available
     */
    @SuppressWarnings("unchecked") // everything should be safe
    public static <T> Optional<T> asObject(Object obj, Class<T> type) {
        if (type == DataMap.class && obj instanceof DataMap) {
            return Optional.of((T) obj);
        } else if (type == DataList.class && obj instanceof DataList) {
            return Optional.of((T) obj);
        } else if (type == Boolean.class) {
            return (Optional<T>) Coerce2.asBoolean(obj);
        } else if (type == Byte.class) {
            return (Optional<T>) Coerce2.asByte(obj);
        } else if (type == Character.class) {
            return (Optional<T>) Coerce2.asChar(obj);
        } else if (type == Short.class) {
            return (Optional<T>) Coerce2.asShort(obj);
        } else if (type == Integer.class) {
            return (Optional<T>) Coerce2.asInteger(obj);
        } else if (type == Long.class) {
            return (Optional<T>) Coerce2.asLong(obj);
        } else if (type == Float.class) {
            return (Optional<T>) Coerce2.asFloat(obj);
        } else if (type == Double.class) {
            return (Optional<T>) Coerce2.asDouble(obj);
        } else if (type == boolean[].class) {
            return (Optional<T>) Coerce2.asBooleanArray(obj);
        } else if (type == byte[].class) {
            return (Optional<T>) Coerce2.asByteArray(obj);
        } else if (type == String.class) {
            return (Optional<T>) Coerce2.asString(obj);
        } else if (type == short[].class) {
            return (Optional<T>) Coerce2.asShortArray(obj);
        } else if (type == int[].class) {
            return (Optional<T>) Coerce2.asIntArray(obj);
        } else if (type == long[].class) {
            return (Optional<T>) Coerce2.asLongArray(obj);
        } else if (type == float[].class) {
            return (Optional<T>) Coerce2.asFloatArray(obj);
        } else if (type == double[].class) {
            return (Optional<T>) Coerce2.asDoubleArray(obj);
        }

        if (Enum.class.isAssignableFrom(type)) {
            return Coerce2.asString(obj).map(s -> (T) Enum.valueOf((Class<? extends Enum>) type, s));
        }
        return Optional.empty();
    }

    private static String sanitiseNumber(Object obj) {
        return obj.toString().trim();
    }

    /**
     * Wraps a primitive number array in a {@link NumArray}
     */
    private static Optional<NumArray> wrapNumArray(Object obj) {
        if (obj instanceof byte[]) {
            return Optional.of(new ByteArray((byte[]) obj));
        } else if (obj instanceof short[]) {
            return Optional.of(new ShortArray((short[]) obj));
        } else if (obj instanceof int[]) {
            return Optional.of(new IntegerArray((int[]) obj));
        } else if (obj instanceof long[]) {
            return Optional.of(new LongArray((long[]) obj));
        } else if (obj instanceof float[]) {
            return Optional.of(new FloatArray((float[]) obj));
        } else if (obj instanceof double[]) {
            return Optional.of(new DoubleArray((double[]) obj));
        }
        return Optional.empty();
    }

    /*
     * Helper classes to wrap number arrays so they can be dalt with generically
     * (saves a lot of special cases in the array coerce methods)
     */

    private interface NumArray {

        int size();

        int intValue(int index);
        long longValue(int index);
        float floatValue(int index);
        double doubleValue(int index);
    }

    private static class ByteArray implements NumArray {

        private byte[] array;

        ByteArray(byte[] array) {
            this.array = array;
        }

        @Override public int size() {
            return array.length;
        }

        @Override public int intValue(int index) { return array[index]; }
        @Override public long longValue(int index) { return array[index]; }
        @Override public float floatValue(int index) { return array[index]; }
        @Override public double doubleValue(int index) { return array[index]; }
    }

    private static class ShortArray implements NumArray {

        private short[] array;

        ShortArray(short[] array) {
            this.array = array;
        }

        @Override public int size() {
            return array.length;
        }

        @Override public int intValue(int index) { return array[index]; }
        @Override public long longValue(int index) { return array[index]; }
        @Override public float floatValue(int index) { return array[index]; }
        @Override public double doubleValue(int index) { return array[index]; }
    }

    private static class IntegerArray implements NumArray {

        private int[] array;

        IntegerArray(int[] array) {
            this.array = array;
        }

        @Override public int size() {
            return array.length;
        }

        @Override public int intValue(int index) { return array[index]; }
        @Override public long longValue(int index) { return array[index]; }
        @Override public float floatValue(int index) { return array[index]; }
        @Override public double doubleValue(int index) { return array[index]; }
    }

    private static class LongArray implements NumArray {

        private long[] array;

        LongArray(long[] array) {
            this.array = array;
        }

        @Override public int size() {
            return array.length;
        }

        @Override public int intValue(int index) { return (int) array[index]; }
        @Override public long longValue(int index) { return array[index]; }
        @Override public float floatValue(int index) { return array[index]; }
        @Override public double doubleValue(int index) { return array[index]; }
    }

    private static class FloatArray implements NumArray {

        private float[] array;

        FloatArray(float[] array) {
            this.array = array;
        }

        @Override public int size() {
            return array.length;
        }

        @Override public int intValue(int index) { return (int) array[index]; }
        @Override public long longValue(int index) { return (long) array[index]; }
        @Override public float floatValue(int index) { return array[index]; }
        @Override public double doubleValue(int index) { return array[index]; }
    }

    private static class DoubleArray implements NumArray {

        private double[] array;

        DoubleArray(double[] array) {
            this.array = array;
        }

        @Override public int size() {
            return array.length;
        }

        @Override public int intValue(int index) { return (int) array[index]; }
        @Override public long longValue(int index) { return (long) array[index]; }
        @Override public float floatValue(int index) { return (float) array[index]; }
        @Override public double doubleValue(int index) { return array[index]; }
    }
}
