// Copyright (c) all rights reserved
// I am lazy right now, I will mess around with copyright/licensing later if need be.
package io.github.xcube16.data.extra;

import io.github.xcube16.data.DataValue;
import io.github.xcube16.data.MemoryDataValue;

import java.util.Optional;
import java.util.UUID;

/**
 * Pure functions to serialize and de-serialize common types
 */
public class DataUtils {

    /**
     * Serializes a {@link UUID}
     *
     * @param uuid The {@link UUID} to serialize
     * @return The {@link DataValue} containing the serialized {@link UUID}
     */
    public static DataValue fromUUID(UUID uuid) {
        return MemoryDataValue.of(new long[] {uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()});
    }

    /**
     * De-serializes a {@link UUID}
     *
     * @param value The {@link DataValue} to de-serialize
     * @return The UUID if de-serialization was successful
     */
    public static Optional<UUID> toUUID(DataValue value) {
        return value.getLongArray().flatMap(array -> array.length == 2 ? Optional.of(new UUID(array[0], array[1])) : Optional.empty());
    }
}
