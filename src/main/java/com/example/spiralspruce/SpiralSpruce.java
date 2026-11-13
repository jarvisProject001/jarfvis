package com.example.spiralspruce;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SpiralSpruce implements ModInitializer {
    public static final String MOD_ID = "spiralspruce";

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.FEATURE,
                Identifier.fromNamespaceAndPath(MOD_ID, "pine_model"),
                new PineModelFeature(NoneFeatureConfiguration.CODEC));
    }
}
