package me.nokko.nogravityjump.mixin;

import com.mojang.authlib.GameProfile;
import me.nokko.nogravityjump.TickTimer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
// Lnet/minecraft/entity/player/PlayerEntity;jump()V
public abstract class NoGravityJumpMixin extends LivingEntity {

	protected NoGravityJumpMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	public TickTimer timer;

	// Get the reference at construction time, not every time we jump.
	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;FLcom/mojang/authlib/GameProfile;)V")
	private void addTimerReferenceToCtor(World world, BlockPos pos, float yaw, GameProfile profile, CallbackInfo ci) {
		this.timer = (TickTimer)FabricLoader.getInstance().getObjectShare().get("nogravityjump:gravitytimer");
	}

	@Shadow
	public abstract void sendMessage(Text text, boolean actionBar);

	@Inject(at = @At("HEAD"), method = "jump()V")
	private void noGravJump_jump(CallbackInfo info) {

		if (!this.world.isClient()) {
			this.sendMessage(new LiteralText("Jump Timer Start!"), false);
			this.setNoGravity(true);

			this.timer.addCallback(() -> {
				this.setNoGravity(false);
				this.sendMessage(new LiteralText("Jump Timer End!"), false);
			}, 20, (ServerWorld) this.world);
		}

	}
}
