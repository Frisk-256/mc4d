package com.iluha168.mc4d.mixin.net.minecraft.world.level.block;

import com.iluha168.mc4d.api.net.minecraft.core.Direction4;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DiodeBlock.class)
abstract class DiodeBlockMixin extends BlockMixin {
	@Shadow
	protected abstract boolean sideInputDiodesOnly();

	@Definition(id = "direction", local = @Local(type = Direction.class, name = "direction"))
	@Expression("direction = ?")
	@Inject(method = "getAlternateSignal", cancellable = true, at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
	protected void getAlternateSignal(
		SignalGetter level, BlockPos pos, BlockState state, CallbackInfoReturnable<Integer> cir,
		@Local(name = "direction") Direction direction
	) {
		final boolean sideInputDiodesOnly = this.sideInputDiodesOnly();
		int signal = 0;
		for (final Direction side : Direction4.as(direction).getHorizontalPerpendiculars()) {
			signal = Math.max(
				signal,
				level.getControlInputSignal(pos.relative(side), side, sideInputDiodesOnly)
			);
		}
		cir.setReturnValue(signal);
	}
}
