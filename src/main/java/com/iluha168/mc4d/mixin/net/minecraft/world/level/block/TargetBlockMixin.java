package com.iluha168.mc4d.mixin.net.minecraft.world.level.block;

import com.iluha168.mc4d.api.net.minecraft.world.phys.Vec4;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetBlock.class)
class TargetBlockMixin {
	@Inject(method = "getRedstoneStrength", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/core/Direction;getAxis()Lnet/minecraft/core/Direction$Axis;"
	))
	private static void getRedstoneStrength_distW(
		BlockHitResult hitResult, Vec3 hitLocation, CallbackInfoReturnable<Integer> cir,
		@Share("distW") LocalDoubleRef distW
	) {
		distW.set(Math.abs(Mth.frac(((Vec4) hitLocation).w) - 0.5));
	}
	@Definition(id = "max", method = "Ljava/lang/Math;max(DD)D")
	@Definition(id = "distX", local = @Local(type = double.class, name = "distX"))
	@Expression("max(distX, ?)")
	@ModifyExpressionValue(method = "getRedstoneStrength", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static double getRedstoneStrength_dist4(double dist3, @Share("distW") LocalDoubleRef distW) {
		return Math.max(dist3, distW.get());
	}
	@Definition(id = "max", method = "Ljava/lang/Math;max(DD)D")
	@Definition(id = "distY", local = @Local(type = double.class, name = "distY"))
	@Definition(id = "distZ", local = @Local(type = double.class, name = "distZ"))
	@Expression("max(distY, distZ)")
	@ModifyExpressionValue(method = "getRedstoneStrength", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static double getRedstoneStrength_w(
		double distYZ,
		@Local(name = "axis") Direction.Axis axis,
		@Local(name = "distX") double distX,
		@Share("distW") LocalDoubleRef distW
	) {
		if (axis == Direction.Axis.X) {
			return Math.max(distYZ, distW.get());
		}
		// W
		return Math.max(distYZ, distX);
	}
}
