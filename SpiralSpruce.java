package com.example.spiralspruce;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class SpiralSpruce implements ModInitializer {
    public static final String MOD_ID = "spiralspruce";

    @Override
    public void onInitialize() {
        // 26.3: Feature adalah interface; codec-nya didaftarkan ke FEATURE_TYPE
        Registry.register(BuiltInRegistries.FEATURE_TYPE,
                Identifier.fromNamespaceAndPath(MOD_ID, "pine_model"),
                PineModelFeature.CODEC);
    }
}
