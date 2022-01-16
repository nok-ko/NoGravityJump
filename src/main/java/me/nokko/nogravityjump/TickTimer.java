package me.nokko.nogravityjump;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.function.Function;

public class TickTimer {

    record TickCallback(Runnable callback, Function<Long, Boolean> agedOut) {
    }

    public ArrayList<TickCallback> callbacks = new ArrayList<>();

    public TickTimer() {
//			this.callbacks = ;
    }

    // Adds a callback that is called `ticks` ticks after this function call
    public void addCallback(Runnable callback, int ticks, ServerWorld world) {
        long tickTime = world.getTime();
        callbacks.add(new TickCallback(callback,
                (currentTick) -> (currentTick >= tickTime + ticks)
        ));
    }

    public void initialize() {
        ServerTickEvents.END_WORLD_TICK.register((ServerWorld world) -> {

            long ticks = world.getTime();
            for (var tickCallback : this.callbacks) {
                if (tickCallback.agedOut.apply(ticks)) {
                    tickCallback.callback.run();
                }
            }

            this.callbacks.removeIf((callback) -> callback.agedOut.apply(ticks));
        });
    }
}
