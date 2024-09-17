package dev.luminous.core.impl;

import java.util.ArrayList;
import java.util.List;

public class FPSManager {
    private final List<Long> records = new ArrayList<>();

    public void record() {
        records.add(System.currentTimeMillis());
    }

    public int getFps() {
        records.removeIf(aLong -> aLong + 1000 < System.currentTimeMillis());
        return records.size();
    }
}
