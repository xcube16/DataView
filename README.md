# DataView

An in-memory data intermediate representation designed to make serialization easy, inspired by SpongeAPI's DataView

See https://github.com/SpongePowered/SpongeAPI/issues/1861 for reasons to use this over the one in SpongeAPI.

## Basics

In this section I will go over some of the basics and various interfaces.

### Allowed Types

Allowed Types are the primitive types that any given DataView or DataValue may contain.

#### Structure Allowed Types
* DataMap
* DataList

#### Primitive Allowed Types
* Boolean
* Byte
* Character
* Short
* Integer
* Long
* Float
* Double

#### Array Allowed Types
* boolean[]
* byte[]
* String
* short[]
* int[]
* long[]
* float[]
* double[]

### DataMap

DataMap is a DataView that stores Allowed Types at String keys. (see DataMap javadoc)

### DataList

DataList is a DataView that stores Allowed Types at consecutive Integer keys starting at 0. (see DataList javadoc)

### DataValue

DataValue is a mutable wrapper for a Primitive Type, it is useful when serializing/de-serializing an object. (see DataValue javadoc)

## Serializing and De-serializing objects

There is more than one way this library can be used and here are only some examples. You may want to make your own thingy for content versioning in case a save file or database contains outdated stuff in it.

First make the objects you want to serialize implement DataSerializable.
// TODO: add a way for external serializers to avoid the contents of DataValue being cloned
```
public class SerializableFoo implements DataSerializable {

    private double a_number;
    private String bar;
    private Baz a_nested_serializable;

    public void toContainer(DataValue data) {
        data.createMap()
                .set("a_number", a_number)
                .set("bar", bar)
                .set("a_nested_serializable", a_nested_serializable);
    }
}
```
a_nested_serializable will have it's toContainer() method called by the DataMap automatically.

Or, if the class is out of your control, you can create an external serializer method, however DataView#set(Object) will not be able to automatically serialize the object.

```
public static void serializeFoo(DataValue data, Foo foo) {
    data.createMap()
            .set("a_number", foo.getA_number())
            .set("bar", foo.getBar())
            .set("a_nested_serializable", foo.getA_nested_serializable());
}
```

You may want to create a more complicated system for de-serializing objects.
Here is a simple static method.
```
public static Optional<Foo> deserializeFoo(DataValue data) {
    return data.getMap().flatMap(dataMap -> {
        Optional<Double> a_number = dataMap.getDouble("a_number");
        Optional<String> bar = dataMap.getString("bar");
        Optional<Baz> a_nested_serializable = deserializeBaz(dataMap.getValue("a_nested_serializable"));

        if (a_number.isPresent() && bar.isPresent() && a_nested_serializable.isPresent()) {
            return Optional.of(new Foo(a_number, bar, a_nested_serializable));
        }
        return Optional.empty();
    });
}
```

or with Exceptions instead of Optionals (I really wish Java had a Result class like Rust. Its like combining Optional and Exception into a return type.)
```
public static Foo deserializeFoo(DataValue data) {
    DataMap dataMap = data.orElseThrow(() -> new Exception("data was not a DataMap"));
    return new Foo(
            dataMap.getDouble("a_number").orElseThrow(() -> new Exception("a_number was not present")),
            dataMap.getString("bar").orElseThrow(() -> new Exception("bar was not present")),
            deserializeBaz(dataMap.getValue("a_nested_serializable")).orElseThrow(() -> new Exception("a_nested_serializable was not present"))
    );
}
```

## Serializing a DataValue or DataView to/form a binary format

Depending on the binary format in question, its top level may have restrictions on the type. Some may with a specific type like DataMap and others may work with DataValue.
In this example I will be serializing Foo (a DataSerializable form the last example) to a binary format I have included in this library.
```
    // foo is an instance of Foo
    DataValue data = MemoryDataValue.of(foo);

    try (FileOutputStream out = new FileOutputStream("foo.bbj")) {
        BBJSON.encode(new DataOutputStream(out), value);
    }

```
foo has been saved to the file 'foo.bbj'
Now lets de-serialize it
```
    try (FileInputStream in = new FileInputStream("foo.bbj")) {
        Optional<Foo> foo = deserializeFoo(
                BBJSON.decode(new DataInputStream(in)));

        // foo now contains an Optional<Foo>
    }
```

There is also a Configurate serializer included in this library.

## BBJSON

Included in this library is a spec for Better Binary JSON, my own fork of UBJSON.
(see BBJSON_0.2 the spec. see BBJSON.java for the implementation)


## WORK IN PROGRESS

// TODO: Fix support for empty DataValues

// TODO: More unit tests