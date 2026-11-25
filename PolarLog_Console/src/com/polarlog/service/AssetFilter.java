package com.polarlog.service;

import com.polarlog.model.Asset;

/** Our own functional interface (one abstract method), used with lambda expressions. */
@FunctionalInterface
public interface AssetFilter {
    boolean accept(Asset asset);
}
