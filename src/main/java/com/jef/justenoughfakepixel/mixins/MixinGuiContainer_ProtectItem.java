package com.jef.justenoughfakepixel.mixins;

import com.jef.justenoughfakepixel.events.SlotClickEvent;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks GuiContainer#handleMouseClick to fire a cancellable SlotClickEvent
 * before the click is forwarded to PlayerControllerMP#windowClick.
 *
 * This gives ProtectItemFeature a chance to block the drop-key (Q) action
 * on protected items when the player's inventory is open — matching the
 * behaviour of Skytils' ProtectItems (DROPKEYININVENTORY protection type).
 */
@Mixin(GuiContainer.class)
public class MixinGuiContainer_ProtectItem {

    @Inject(
            method = "handleMouseClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;windowClick(IIIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"
            ),
            cancellable = true
    )
    private void onHandleMouseClick(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        GuiContainer gui = (GuiContainer) (Object) this;
        SlotClickEvent event = new SlotClickEvent(gui, slot, slotId, clickedButton, clickType);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
