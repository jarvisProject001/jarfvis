package com.example.spiralspruce;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Menumbuhkan pohon dari model litematic (6 variasi, tinggi 17-36 blok).
 * Semua blok kayu adalah spruce; daun spruce dengan distance sudah dihitung.
 */
public class PineModelFeature implements Feature {
    public static final MapCodec<PineModelFeature> CODEC = MapCodec.unit(PineModelFeature::new);

    private record Entry(int dx, int dy, int dz, String code) {}

    private static final List<Entry[]> TREES = new ArrayList<>();

    static {
        for (String tree : PineData.TREES) {
            String[] parts = tree.split(";");
            Entry[] arr = new Entry[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String[] p = parts[i].split(",");
                arr[i] = new Entry(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), p[3]);
            }
            TREES.add(arr);
        }
    }

    public PineModelFeature() {}

    @Override
    public MapCodec<PineModelFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int n = TREES.size();
        int start = random.nextInt(n);
        int rot = random.nextInt(4);
        // coba variasi acak dulu; kalau batangnya terhalang, coba yang lebih pendek
        for (int k = 0; k < n; k++) {
            Entry[] tree = TREES.get(Math.floorMod(start - k, n));
            if (trunkFree(level, origin, tree, rot)) {
                build(level, origin, tree, rot);
                return true;
            }
        }
        return false;
    }

    private static int rx(int dx, int dz, int rot) {
        int x = dx, z = dz;
        for (int i = 0; i < rot; i++) { int nx = -z; z = x; x = nx; }
        return x;
    }

    private static int rz(int dx, int dz, int rot) {
        int x = dx, z = dz;
        for (int i = 0; i < rot; i++) { int nx = -z; z = x; x = nx; }
        return z;
    }

    private static boolean replaceable(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.REPLACEABLE_BY_TREES);
    }

    private static boolean trunkFree(WorldGenLevel level, BlockPos origin, Entry[] tree, int rot) {
        for (Entry e : tree) {
            if (e.dx() == 0 && e.dz() == 0 && !e.code().startsWith("L")) {
                if (!replaceable(level, origin.offset(0, e.dy(), 0))) return false;
            }
        }
        return true;
    }

    private static void build(WorldGenLevel level, BlockPos origin, Entry[] tree, int rot) {
        // kayu dulu, daun belakangan supaya daun tidak menimpa kayu
        for (int pass = 0; pass < 2; pass++) {
            for (Entry e : tree) {
                boolean leaf = e.code().startsWith("L");
                if (leaf != (pass == 1)) continue;
                BlockPos pos = origin.offset(rx(e.dx(), e.dz(), rot), e.dy(), rz(e.dx(), e.dz(), rot));
                if (!replaceable(level, pos)) continue;
                level.setBlock(pos, state(e.code(), rot), 19);
            }
        }
    }

    private static Direction dir(char c, int rot) {
        Direction d = switch (c) {
            case 'e' -> Direction.EAST;
            case 'w' -> Direction.WEST;
            case 's' -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        for (int i = 0; i < rot; i++) d = d.getClockWise();
        return d;
    }

    private static BlockState state(String code, int rot) {
        char c = code.charAt(0);
        switch (c) {
            case 'a': return Blocks.SPRUCE_LOG.defaultBlockState();
            case 'b': return Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
            case 'c': return Blocks.SPRUCE_WOOD.defaultBlockState();
            case 'f': return Blocks.SPRUCE_FENCE.defaultBlockState();
            case 'G':
                return Blocks.SPRUCE_FENCE_GATE.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, dir(code.charAt(1), rot));
            case 'T':
                return Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, dir(code.charAt(1), rot))
                        .setValue(BlockStateProperties.HALF, code.charAt(2) == 't' ? Half.TOP : Half.BOTTOM);
            case 'L':
                return Blocks.SPRUCE_LEAVES.defaultBlockState()
                        .setValue(BlockStateProperties.DISTANCE, Integer.parseInt(code.substring(1)))
                        .setValue(BlockStateProperties.PERSISTENT, false);
            default: return Blocks.SPRUCE_PLANKS.defaultBlockState();
        }
    }
}
