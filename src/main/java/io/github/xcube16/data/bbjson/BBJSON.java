// Copyright (c) all rights reserved
// I am lazy right now, I will mess around with copyright/licensing later if need be.
package io.github.xcube16.data.bbjson;

import io.github.xcube16.data.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class BBJSON {

    private static final byte NOP = ' ';
    private static final byte NULL = 'N';
    private static final byte TRUE = 'T';
    private static final byte FALSE = 'F';
    private static final byte CHAR = 'u';
    private static final byte STRING = 'U';

    private static final byte BYTE = 'B';
    private static final byte SHORT = 'S';
    private static final byte INT = 'I';
    private static final byte LONG = 'L';
    private static final byte FLOAT = ',';
    private static final byte DOUBLE = ';';

    private static final byte SHORT_L = 's';
    private static final byte INT_L = 'i';
    private static final byte LONG_L = 'l';
    private static final byte FLOAT_L = '.';
    private static final byte DOUBLE_L = ':';

    private static final byte HNUM = 'H';

    private static final byte MAP = '{';
    private static final byte ARRAY = '[';

    private static final byte MAP_END = '}';
    private static final byte ARRAY_END = ']';

    private static CharsetDecoder UTF8_DECODE;
    private static CharsetEncoder UTF8_ENCODE;

    static {
        Charset set = Charset.forName("UTF8");
        UTF8_DECODE = set.newDecoder();
        UTF8_DECODE.onMalformedInput(CodingErrorAction.REPORT);
        UTF8_DECODE.onUnmappableCharacter(CodingErrorAction.REPORT);

        UTF8_ENCODE = set.newEncoder();
        UTF8_ENCODE.onMalformedInput(CodingErrorAction.REPORT);
        UTF8_ENCODE.onUnmappableCharacter(CodingErrorAction.REPORT);
    }

    /*
     * decoder
     */
    public static DataValue decode(DataInput in) throws IOException {
        DataValue value = new MemoryDataValue();
        value.set(decode(in, in.readByte()));
        return value;
    }

    private static Object decode(DataInput in, byte type) throws IOException {
        switch (type) {
            case MAP:
                return decodeMap(in, new MemoryDataMap());

            case ARRAY:
                return decodeArray(in);

            case NULL:
                return ""; // FIXME: is there something better we can return?

            case TRUE:
                return true;

            case FALSE:
                return false;

            case CHAR:
                return readChar(in);

            case STRING:
                return decodeString(in);

            case BYTE:
                return in.readByte();

            case SHORT:
                return in.readShort();

            case INT:
                return in.readInt();

            case LONG:
                return in.readLong();

            case FLOAT:
                return in.readFloat();

            case DOUBLE:
                return in.readDouble();

            case SHORT_L:
                return readLittleShort(in);

            case INT_L:
                return readLittleInt(in);

            case LONG_L:
                return readLittleLong(in);

            case FLOAT_L:
                return Float.intBitsToFloat(readLittleInt(in));

            case DOUBLE_L:
                return Double.longBitsToDouble(readLittleLong(in));

            case HNUM: // TODO: don't turn this into a string
                return decodeString(in);

            default:
                throw new IOException("Unknown type code '" + (char) type + "'");
        }
    }

    private static DataMap decodeMap(DataInput in, DataMap map) throws IOException {
        byte opt = in.readByte();
        if (opt == NULL) { // [{][N]

            for (byte type = in.readByte(); type != MAP_END; type = in.readByte()) {
                if (type == NOP) {
                    continue;
                }
                String key = decodeString(in, decodeSize(in, type));
                map.set(key, decode(in, in.readByte()));
            }

        } else if (opt == '#') { // [{][#][iType][count]
            int count = decodeSize(in, in.readByte());

            for (; count > 0; count--) {
                String key = decodeString(in);
                map.set(key, decode(in, in.readByte()));
            }

        } else if (opt == '$') { // [{][$][iType][count][type]
            int count = decodeSize(in, in.readByte());
            byte type = in.readByte();

            for (; count > 0; count--) {
                String key = decodeString(in);
                map.set(key, decode(in, type));
            }

        } else {
            throw new IOException("Expected 'N' or '#' or '$', got '" + (char) opt + "'");
        }
        return map;
    }

    private static Object decodeArray(DataInput in) throws IOException {
        byte opt = in.readByte();
        if (opt == '$') { // [[]...[$][iType][count][type]
            return decodeSingleTypeArray(in, decodeSize(in, in.readByte()), in.readByte());
        }

        DataList list = new MemoryDataList();
        if (opt == NULL) {  // [[][N]

            for (byte type = in.readByte(); type != ARRAY_END; type = in.readByte()) {
                if (type == NOP) {
                    continue;
                }
                list.add(decode(in, type));
            }
        } else if (opt == '#') {            // [[][#]

            int count = decodeSize(in, in.readByte());
            for (int i = 0; i < count; i++) {
                byte type = in.readByte();

                list.add(decode(in, type));
            }
        }  else {
            throw new IOException("Expected 'N' or '#' or '$', got '" + (char) opt + "'");
        }
        return list;
    }

    private static Object decodeSingleTypeArray(DataInput in, int count, byte type) throws IOException {
        switch(type){
            case NULL:
                return null; // FIXME: idk about this
            case MAP: {
                DataList list = new MemoryDataList();

                for (int i = 0; i < count; i++) {
                    list.set(i, decodeMap(in, new MemoryDataMap()));
                }

                return list;
            }
            case ARRAY: {
                DataList list = new MemoryDataList();

                for (int i = 0; i < count; i++) {
                    list.set(i, decodeArray(in));
                }

                return list;
            }
            case HNUM: // FIXME: hnums will be coerced later, but when re-encoded it will be strings
            case STRING: {
                DataList list = new MemoryDataList();

                for (int i = 0; i < count; i++) {
                    list.set(i, decodeString(in));
                }

                return list;
            }
            case TRUE: {
                boolean[] a = new boolean[count];
                Arrays.fill(a, true);
                return a;
            }
            case FALSE:
                return new boolean[count]; // boolean[] defaults to false
            case CHAR:
                return decodeString(in, count);
            case BYTE: {
                byte[] a = new byte[count];
                for(int i = 0;i < count;i++){
                    a[i] = in.readByte();
                }
                return a;
            }
            case SHORT: {
                short[] a = new short[count];
                for(int i = 0;i < count;i++){
                    a[i] = in.readShort();
                }
                return a;
            }
            case INT: {
                int[] a = new int[count];
                for(int i = 0;i < count;i++){
                    a[i] = in.readInt();
                }
                return a;
            }
            case LONG: {
                long[] a = new long[count];
                for(int i = 0;i < count;i++){
                    a[i] = in.readLong();
                }
                return a;
            }
            case FLOAT: {
                float[] a = new float[count];
                for(int i = 0;i < count;i++){
                    a[i] = in.readFloat();
                }
                return a;
            }
            case DOUBLE: {
                double[] a = new double[count];
                for(int i = 0;i < count;i++){
                    a[i] = in.readDouble();
                }
                return a;
            }
            case SHORT_L: {
                short[] a = new short[count];
                for(int i = 0;i < count;i++){
                    a[i] = readLittleShort(in);
                }
                return a;
            }
            case INT_L: {
                int[] a = new int[count];
                for(int i = 0;i < count;i++){
                    a[i] = readLittleInt(in);
                }
                return a;
            }
            case LONG_L: {
                long[] a = new long[count];
                for(int i = 0;i < count;i++){
                    a[i] = readLittleLong(in);
                }
                return a;
            }
            case FLOAT_L: {
                float[] a = new float[count];
                for(int i = 0;i < count;i++){
                    a[i] = Float.intBitsToFloat(readLittleInt(in));
                }
                return a;
            }
            case DOUBLE_L: {
                double[] a = new double[count];
                for(int i = 0;i < count;i++){
                    a[i] = Double.longBitsToDouble(readLittleLong(in));
                }
                return a;
            }
            default:
                throw new IOException("Unknown type code '" + (char)type + "'");
        }
    }

    private static String decodeString(DataInput in, int size) throws IOException {
        byte[] bytes = new byte[size];
        in.readFully(bytes);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return UTF8_DECODE.decode(buffer).toString(); // TODO: could use some optimizations
    }

    private static String decodeString(DataInput in) throws IOException {
        return decodeString(in, decodeSize(in, in.readByte()));
    }

    private static char readChar(DataInput in) throws IOException {
        int b1 = in.readByte(); // read the first byte
        int b2;
        int b3;

        switch (b1 >> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                /* 0xxxxxxx*/
                return (char) b1;

            case 12:
            case 13:
                /* 110x xxxx   10xx xxxx*/

                b2 = in.readByte();
                if ((b2 & 0xC0) != 0x80)
                    throw new UTFDataFormatException(
                        "malformed input byte");

                return (char) (((b1 & 0x1F) << 6) |
                    (b2 & 0x3F));

            case 14:
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                b2 = in.readByte();
                b3 = in.readByte();
                if (((b2 & 0xC0) != 0x80) || ((b3 & 0xC0) != 0x80))
                    throw new UTFDataFormatException(
                        "malformed input byte");

                return (char) (((b1 & 0x0F) << 12) |
                    ((b2 & 0x3F) << 6) |
                    ((b3 & 0x3F)     ));

            default:
                /* 10xx xxxx,  1111 xxxx */
                throw new UTFDataFormatException(
                    "malformed input byte");
        }
    }

    private static int decodeSize(DataInput in, byte type) throws IOException {
        switch (type) {
            case BYTE:
                return in.readByte();
            case SHORT:
                return in.readShort();
            case INT:
                return in.readInt();
            case LONG:
                return (int) in.readLong();
            case SHORT_L:
                return readLittleShort(in);
            case INT_L:
                return readLittleInt(in);
            case LONG_L:
                return (int) readLittleLong(in);
            default:
                throw new IOException("'" + type + "' can not be used as a size type");
        }
    }

    private static short readLittleShort(DataInput in) throws IOException {
        return (short) ((in.readByte() & 0xff) | (in.readByte() << 8));
    }

    private static int readLittleInt(DataInput in) throws IOException {
        return ((in.readByte() & 0xff) |
            ((in.readByte() & 0xff) << 8) |
            ((in.readByte() & 0xff) << 16) |
            ((in.readByte() & 0xff) << 24));
    }

    private static long readLittleLong(DataInput in) throws IOException {
        return (((long) (in.readByte() & 0xff)) |
            ((long) (in.readByte() & 0xff) << 8) |
            ((long) (in.readByte() & 0xff) << 16) |
            ((long) (in.readByte() & 0xff) << 24) |
            ((long) (in.readByte() & 0xff) << 32) |
            ((long) (in.readByte() & 0xff) << 40) |
            ((long) (in.readByte() & 0xff) << 48) |
            ((long) (in.readByte() & 0xff) << 56));
    }

    /*
     *  encoder
     */
    public static void encode(DataOutput out, DataValue value) throws IOException {
        Optional<Object> opt = value.get();
        if (opt.isPresent()) {
            encode(out, opt.get(), true);
        } else {
            out.writeByte(NULL);
        }
    }

    private static void encode(DataOutput out, Object obj, boolean typePrefix) throws IOException {

        if (obj instanceof DataList) {

            if (typePrefix) out.writeByte(ARRAY);
            DataList list = (DataList) obj;

            int count = list.size();
            byte type = 0;
            boolean fixedtype = false;

            boolean first = true;
            for (int i = 0; i < count; i++) { // scan for optimizations
                Object item = list.get(i).get();

                if (first) {
                    first = false;
                    fixedtype = true;
                    type = getObjType(item);
                } else if (fixedtype) {
                    if (type != getObjType(item)) {
                        fixedtype = false; // mixed type object... thats normal, but we will need to use 1 extra byte per field
                    }
                }
            }

            if (fixedtype) {
                out.writeByte('$');
                encodeSize(out, count);
                out.writeByte(type);

                for (int i = 0; i < count; i++) {
                    encode(out, list.get(i).get(), false);
                }
            } else {
                out.writeByte('#');
                encodeSize(out, count);

                for (int i = 0; i < count; i++) {
                    encode(out, list.get(i).get(), true);
                }
            }
            return;

        } else if (obj instanceof DataMap) {

            if (typePrefix) out.writeByte(MAP);
            DataMap map = (DataMap) obj;
            Set<String> keys = map.getKeys();

            int count = map.size();
            byte type = 0;
            boolean fixedtype = false;

            boolean first = true;
            for (String key : keys) { // scan for optimizations
                Object item = map.get(key).get();

                if (first) {
                    first = false;
                    fixedtype = true;
                    type = getObjType(item);
                } else if (fixedtype) {
                    if (type != getObjType(item)) {
                        fixedtype = false; // mixed type object... that's ok, but we will need to use 1 extra byte per field
                    }
                }
            }

            if (fixedtype) {
                out.writeByte('$');
                encodeSize(out, count);
                out.writeByte(type);

                for (String key : keys) {
                    encodeString(out, key);
                    encode(out, map.get(key).get(), false);
                }
            } else {
                out.writeByte('#');
                encodeSize(out, count);

                for (String key : keys) {
                    encodeString(out, key);
                    encode(out, map.get(key).get(), true);
                }
            }
            return;
        }

        if (obj == null) {
            if (typePrefix) out.writeByte(NULL);

        } else if (obj instanceof Boolean) {
            if (typePrefix) out.writeByte((Boolean) obj ? TRUE : FALSE);

        } else if (obj instanceof Character) {
            if (typePrefix) out.writeByte(CHAR);
            writeChar(out, (Character) obj);

        } else if (obj instanceof Byte) {
            if (typePrefix) out.writeByte(BYTE);
            out.writeByte((Byte) obj);

        } else if (obj instanceof Short) {
            if (typePrefix) out.writeByte(SHORT);
            out.writeShort((Short) obj);

        } else if (obj instanceof Integer) {
            if (typePrefix) out.writeByte(INT);
            out.writeInt((Integer) obj);

        } else if (obj instanceof Long) {
            if (typePrefix) out.writeByte(LONG);
            out.writeLong((Long) obj);

        } else if (obj instanceof Float) {
            if (typePrefix) out.writeByte(FLOAT);
            out.writeFloat((Float) obj);

        } else if (obj instanceof Double) {
            if (typePrefix) out.writeByte(DOUBLE);
            out.writeDouble((Double) obj);

        } else if (obj instanceof boolean[]) {
            if (typePrefix) out.writeByte(ARRAY);
            out.writeByte('#');
            boolean[] a = (boolean[]) obj;
            encodeSize(out, a.length);
            for (boolean b : a) {
                out.writeByte(b ? TRUE : FALSE);
            }

        } else if (obj instanceof String) {
            if (typePrefix) out.writeByte(STRING);
            encodeString(out, (String) obj);

        } else {
            if (typePrefix) out.writeByte(ARRAY);
            out.writeByte('$');
            encodeSize(out, Array.getLength(obj));

            if (obj instanceof byte[]) {
                out.writeByte(BYTE);
                for (byte b : (byte[]) obj) {
                    out.writeByte(b);
                }

            } else if (obj instanceof short[]) {
                out.writeByte(SHORT);
                for (short s : (short[]) obj) {
                    out.writeShort(s);
                }

            } else if (obj instanceof int[]) {
                out.writeByte(INT);
                for (int i : (int[]) obj) {
                    out.writeInt(i);
                }

            } else if (obj instanceof long[]) {
                out.writeByte(LONG);
                for (long l : (long[]) obj) {
                    out.writeLong(l);
                }

            } else if (obj instanceof float[]) {
                out.writeByte(FLOAT);
                for (float f : (float[]) obj) {
                    out.writeFloat(f);
                }

            } else if (obj instanceof double[]) {
                out.writeByte(DOUBLE);
                for (double d : (double[]) obj) {
                    out.writeDouble(d);
                }

            } else {
                throw new IOException("Unable to encode objects of type " + obj.getClass().getName());
            }
        }
    }

    private static byte getObjType(Object obj) throws IOException {
        if (obj instanceof DataMap) {
            return MAP;
        } else if (obj instanceof DataList || obj.getClass().isArray()) {
            return ARRAY;
        } else if (obj instanceof Boolean) {
            return (Boolean) obj ? TRUE : FALSE;
        } else if (obj instanceof Character) {
            return CHAR;
        } else if (obj instanceof String) {
            return STRING;
        } else if (obj instanceof Byte) {
            return BYTE;
        } else if (obj instanceof Short) {
            return SHORT;
        } else if (obj instanceof Integer) {
            return INT;
        } else if (obj instanceof Long) {
            return LONG;
        } else if (obj instanceof Float) {
            return FLOAT;
        } else if (obj instanceof Double) {
            return DOUBLE;
        } else {
            throw new IOException("Unable to encode objects of type " + obj.getClass().getName());
        }
    }

    private static void encodeString(DataOutput out, String str) throws IOException {
        ByteBuffer buffer = UTF8_ENCODE.encode(CharBuffer.wrap(str)); // TODO: could use some optimizations
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);

        encodeSize(out, data.length);
        out.write(data);
    }

    private static void writeChar(DataOutput out, char ch) throws IOException {

        if ((ch >= 0x0001) && (ch <= 0x007F)) {
            out.writeByte(ch);

        } else if (ch > 0x07FF) {
            out.writeByte(0xE0 | ((ch >> 12) & 0x0F));
            out.writeByte(0x80 | ((ch >> 6) & 0x3F));
            out.writeByte(0x80 | ((ch     ) & 0x3F));
        } else {
            out.writeByte(0xC0 | ((ch >> 6) & 0x1F));
            out.writeByte(0x80 | ((ch     ) & 0x3F));
        }
    }

    private static void encodeSize(DataOutput out, int size) throws IOException {
        if (size < 0x8FFF) {
            if (size < 0x8F) {
                out.writeByte(BYTE);
                out.writeByte(size);
            } else {
                out.writeByte(SHORT);
                out.writeShort(size);
            }
        } else {
            out.writeByte(INT);
            out.writeInt(size);
        }
    }
}
