package net.vulkanmod.mixin.chunk;

import net.minecraft.client.renderer.chunk.VisibilitySet;
import net.minecraft.core.Direction;
import net.vulkanmod.interfaces.VisibilitySetExtended;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(VisibilitySet.class)
public class VisibilitySetMixin implements VisibilitySetExtended {

    private long vis = 0;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void set(Direction dir1, Direction dir2, boolean p_112989_) {
        vis |= 1L << ((dir1.ordinal() << 3) + dir2.ordinal()) | 1L << ((dir2.ordinal() << 3) + dir1.ordinal());
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void setAll(boolean bl) {
        if(bl) vis = 0xFFFFFFFFFFFFFFFFL;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean visibilityBetween(Direction dir1, Direction dir2) {
        return (vis & (1L << ((dir1.ordinal() << 3) + dir2.ordinal()))) != 0;
    }

    @Override
    public long getVisibility() {
        return vis;
    }
}
