package com.example.willpower.world;

import com.example.willpower.core.LanternCorps;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public final class CentralBatteryMultiblock {
    private static final List<BlockPos> OFFSETS = buildOffsets();

    private CentralBatteryMultiblock() {}

    private static List<BlockPos> buildOffsets() {
        List<BlockPos> pts = new ArrayList<>(66);
        // Основание 3x3 (9 блоков)
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++)
                pts.add(new BlockPos(x, -4, z));
        // Ствол (2 блока)
        pts.add(new BlockPos(0, -3, 0));
        pts.add(new BlockPos(0, -2, 0));
        // Ядро 3x3x3 без центра (26 блоков)
        for (int y = -1; y <= 1; y++)
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++)
                    if (!(x == 0 && y == 0 && z == 0))
                        pts.add(new BlockPos(x, y, z));
        // Боковые крылья 3x3 слева и справа (18 блоков)
        for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
                pts.add(new BlockPos(-2, y, z));
                pts.add(new BlockPos(2, y, z));
            }
        }
        // Верхняя площадка 3x3 (9 блоков) и шпиль (2 блока) => итого 66 + ядро = 67
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++)
                pts.add(new BlockPos(x, 2, z));
        pts.add(new BlockPos(0, 3, 0));
        pts.add(new BlockPos(0, 4, 0));
        return List.copyOf(pts);
    }

    public static LanternCorps detectCompleted(World world, BlockPos corePos) {
        for (LanternCorps corps : LanternCorps.values()) {
            if (countMatching(world, corePos, corps) == OFFSETS.size()) {
                return corps;
            }
        }
        return null;
    }

    public static int countMatching(World world, BlockPos corePos, LanternCorps corps) {
        Block required = corps.getBatteryBlock();
        int count = 0;
        for (BlockPos offset : OFFSETS) {
            if (world.getBlockState(corePos.add(offset)).isOf(required)) {
                count++;
            }
        }
        return count;
    }

    public static int totalRequiredSurrounding() {
        return OFFSETS.size();
    }
}
