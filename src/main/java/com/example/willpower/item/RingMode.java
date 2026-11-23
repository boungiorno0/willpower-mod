package com.example.willpower.item;

/** Режимы кольца. Переключаются ПКМ с зажатым Shift. */
public enum RingMode {
    BRIDGE(8),     // мост 3 x 16 по направлению взгляда
    WALL(10),      // стена 5 x 4 перед игроком
    PLATFORM(6),   // платформа 5 x 5 под ногами
    SHIELD(20),    // сфера-щит вокруг игрока
    DASH(4),      // рывок вперёд
    FLIGHT(0);     // полёт (вкл/выкл), заряд тратится во время полёта

    public final int cost;

    RingMode(int cost) {
        this.cost = cost;
    }

    public RingMode next() {
        RingMode[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public static RingMode byId(int id) {
        RingMode[] all = values();
        return (id >= 0 && id < all.length) ? all[id] : BRIDGE;
    }

    public String translationKey() {
        return "mode.willpower." + name().toLowerCase();
    }
}
