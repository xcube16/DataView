package io.github.xcube16.data.bbjson;


import io.github.xcube16.data.*;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class BBJSONTests {
    private static final byte[] TEST_DATA = new byte[] {
            (byte)'{', (byte)'N',
            (byte)'B', 0x02, (byte)'a', (byte)'b', (byte)'S', 0x00, 0x28, // ab: 40
            (byte)'B', 0x03, (byte)'y', (byte)'a', (byte)'y', (byte)'i', (byte)0xFF, 0x03, 0x00, 0x00, // yay: 1023
            (byte)'B', 0x02, (byte)'l', (byte)'s', (byte)'[', (byte)'N',
            (byte)'B', 0x03,
            (byte)'B', 0x06,
            (byte)'B', 0x09,
            (byte)']',
            (byte)'}'
    };

    @Test
    public void DecodeBBJSON() throws IOException {
        DataMap obj = BBJSON.decode(new DataInputStream(new ByteArrayInputStream(TEST_DATA))).getMap().get();

        System.out.println(obj);

        assertEquals(40, obj.getShort("ab").get().shortValue());
        assertEquals(1023, obj.getInt("yay").get().intValue());

        assertEquals(1023, obj.getShort("yay").get().shortValue());

        DataList ls = obj.getList("ls").get();
        assertEquals(3, ls.getInt(0).get().intValue());
        assertEquals(6, ls.getInt(1).get().intValue());
        assertEquals(9, ls.getInt(2).get().intValue());
    }

    @Test
    public void EncodeBBJSON() throws IOException {
        DataValue value = new MemoryDataValue();
        DataMap obj = value.createMap();

        obj.set("fluffy", true)
                .set("prickly", false)
                .set("toasters", 22222)
                .createMap("color")
                    .set("red",   (byte) 0x35)
                    .set("green", (byte) 0x80)
                    .set("blue",  (byte) 0xEA);
        obj.createList("shopping_list")
                    .add("marshmellows")
                    .add("pineapple")
                    .add("pine-apple (apples that grow on pine trees)");

        // TODO: this puts a file in the project directory. Maybe unit tests should not do stuff like that.
        FileOutputStream out = new FileOutputStream("test.ubj");
        BBJSON.encode(new DataOutputStream(out), value);
        out.close();

        FileInputStream in = new FileInputStream("test.ubj");
        DataMap obj2 = BBJSON.decode(new DataInputStream(in)).getMap().get();
        in.close();

        assertEquals(true,  obj2.getBoolean("fluffy").get());
        assertEquals(false, obj2.getBoolean("prickly").get());
        assertEquals(22222, obj2.getInt("toasters").get().intValue());
        DataMap color = obj2.getMap("color").get();
        assertEquals((byte) 0x35, color.getByte("red").get().byteValue());
        assertEquals((byte) 0x80, color.getByte("green").get().byteValue());
        assertEquals((byte) 0xEA, color.getByte("blue").get().byteValue());
        DataList list = obj2.getList("shopping_list").get();
        assertEquals("marshmellows", list.getString(0).get());
        assertEquals("pineapple",    list.getString(1).get());
        assertEquals("pine-apple (apples that grow on pine trees)", list.getString(2).get());
    }

}
