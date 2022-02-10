package net.witixin.mountables2;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec GENERAL_SPEC;
    public static ForgeConfigSpec.ConfigValue<Integer> MAX_BATTERY_AMOUNT;
    public static ForgeConfigSpec.ConfigValue<Integer> BATTERY_RADIUS;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("MultiWattery config");
        MAX_BATTERY_AMOUNT = builder.comment("The maximum FE that a battery can hold").define("max_battery_amount", 100_000);
        BATTERY_RADIUS = builder.comment("The radius, from the battery's core that the multiblock extends to").define("radius", 2);
    }
}
