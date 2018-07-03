### An in-memory data intermediate representation designed to make serialization easy, inspired by SpongeAPI's DataView

TODO: this readme could use some work

Most of the important stuff to look at is in DataView, DataMap, DataList and DataValue
(the javadoc is good)

DataMap and DataList are a type (extend) DataView
DataList's keys are consecutive integers
DataMap's keys are strings

io.github.xcube16.data contains most of the cool stuff

io.github.xcube16.data.bbjson contains a binary serializer and de-serializer and spec for the binary format
BBJSON is my own fork of UBJSON (a kind of dead project if you ask me)

io.github.xcube16.data.configurate contains a DataValue <-> ConfigurationNode serializer/de-serializer

io.github.xcube16.data.configurate contains some usefull methods for serializing common objects.
Right now it just does UUID's

TODO: Should DataValue be allowed to contain null?
I have had a need for that here and there and find my self putting empty strings in it

TODO: MORE EXAMPLE CODE!!! D:

TODO: More unit tests