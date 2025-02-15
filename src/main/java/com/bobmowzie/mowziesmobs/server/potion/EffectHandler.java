package com.bobmowzie.mowziesmobs.server.potion;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MowziesMobs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EffectHandler {
    private EffectHandler() {
    }

    public static final EffectSunsBlessing SUNS_BLESSING = (EffectSunsBlessing) new EffectSunsBlessing().setRegistryName(MowziesMobs.MODID, "suns_blessing");
    public static final EffectGeomancy GEOMANCY = (EffectGeomancy) new EffectGeomancy().setRegistryName(MowziesMobs.MODID, "geomancy");
    public static final EffectFrozen FROZEN = (EffectFrozen) new EffectFrozen().setRegistryName(MowziesMobs.MODID, "frozen");
    public static final EffectPoisonResist POISON_RESIST = (EffectPoisonResist) new EffectPoisonResist().setRegistryName(MowziesMobs.MODID, "poison_resist");
    public static final EffectSunblock SUNBLOCK = (EffectSunblock) new EffectSunblock().setRegistryName(MowziesMobs.MODID, "sunblock");

    @SubscribeEvent
    public static void register(RegistryEvent.Register<MobEffect> event) {
        event.getRegistry().registerAll(
                SUNS_BLESSING,
                GEOMANCY,
                FROZEN,
                POISON_RESIST,
                SUNBLOCK
        );
    }

    public static void addOrCombineEffect(LivingEntity entity, MobEffect effect, int duration, int amplifier, boolean ambient, boolean showParticles) {
        if (effect == null) return;
        MobEffectInstance effectInst = entity.getEffect(effect);
        MobEffectInstance newEffect = new MobEffectInstance(effect, duration, amplifier, ambient, showParticles);
        if (effectInst != null) effectInst.update(newEffect);
        else entity.addEffect(newEffect);
    }
}
