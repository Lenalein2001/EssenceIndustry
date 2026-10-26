package com.neolena.essenceindustry.mixin;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
/** The native PiP submission queue has no public extraction helper in 26.1.2. */
@Mixin(GuiGraphicsExtractor.class)
public interface ArrayGuiAccessor {
 @Accessor("guiRenderState") GuiRenderState essenceindustry$renderState();
}
