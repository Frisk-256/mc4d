package com.iluha168.mc4d.mixin.net.minecraft.world.level.gameevent.vibrations;

import com.iluha168.mc4d.api.net.minecraft.world.phys.Vec4;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VibrationInfo.class)
class VibrationInfoMixin {
	@Redirect(method = "lambda$static$0", at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/world/phys/Vec3;CODEC:Lcom/mojang/serialization/Codec;",
		opcode = Opcodes.GETSTATIC
	))
	private static Codec<Vec4> CODEC() {
		return Vec4.CODEC;
	}
}
