// Copyright (c) all rights reserved
// I am lazy right now, I will mess around with copyright/licensing later if need be.
package io.github.xcube16.data.configurate;

import com.google.common.collect.ImmutableSet;
import io.github.xcube16.data.DataList;
import io.github.xcube16.data.DataMap;
import io.github.xcube16.data.DataValue;
import io.github.xcube16.data.MemoryDataValue;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;

import java.util.List;
import java.util.Map;

public class Configurate {

    // TODO: I dont remember why I need this
    public static final ImmutableSet<Class<?>> NODE_PRIMITIVES = ImmutableSet.of(Map.class, List.class, Double.class,
            Long.class, Integer.class, Boolean.class, String.class, Number.class);

    public static DataValue decode(ConfigurationNode node) {

        DataValue value = new MemoryDataValue();

        if (node.hasMapChildren()) {
            DataMap map = value.createMap();
            Map<Object, ? extends ConfigurationNode> nodeMap = node.getChildrenMap();
            nodeMap.forEach((key, nodeValue) -> {
                map.set(key.toString(), Configurate.decode(nodeValue));
            });
        } else if (node.hasListChildren()) {
            DataList list = value.createList();
            List<? extends ConfigurationNode> nodeList = node.getChildrenList();
            nodeList.forEach(nodeValue -> {
                list.add(Configurate.decode(nodeValue));
            });
        } else {
            // TODO: null data value?
            value.set(node.getValue());
        }

        return value;
    }

    public static ConfigurationNode encode(DataValue value, ConfigurationOptions options) {

        options = options.setAcceptedTypes(Configurate.NODE_PRIMITIVES); // TODO: I dont rmemeber why I did this

        ConfigurationNode node = SimpleConfigurationNode.root(options);
        return encode(value, node);
    }

    private static ConfigurationNode encode(DataValue value, ConfigurationNode node) {


        Object obj = value.get();
        if (obj instanceof DataMap) { // TODO: idea says this is always false! TEST THIS!!!!!
            DataMap map = ((DataMap) obj);
            map.forEachKey(key -> {
                Configurate.encode(map.getValue(key).get(), node.getNode(key));
            });
        } else if (obj instanceof DataList) { // TODO: idea says this is always false! TEST THIS!!!!!
            DataList list = ((DataList) obj);
            for (int i = 0; i < list.size(); i++) {
                Configurate.encode(list.getValue(i).get(), node.getAppendedNode());
            }
        } else {
            node.setValue(obj);
        }

        return node;
    }
}
