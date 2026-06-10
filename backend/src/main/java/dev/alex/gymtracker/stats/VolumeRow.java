package dev.alex.gymtracker.stats;

import java.math.BigDecimal;

public interface VolumeRow {
    String getPeriod();
    BigDecimal getVolumeKg();
}
