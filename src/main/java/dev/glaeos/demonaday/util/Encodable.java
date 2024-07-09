package dev.glaeos.demonaday.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@FunctionalInterface
public interface Encodable {

    @NotNull Collection<Byte> encode();

}
