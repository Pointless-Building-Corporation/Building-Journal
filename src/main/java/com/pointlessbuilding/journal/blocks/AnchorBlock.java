package com.pointlessbuilding.journal.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class AnchorBlock extends Block {
    
    public AnchorBlock() {
        super(Properties.of()
            .strength(0.0f, 9f) //Insta break but resistant to explosions
            .requiresCorrectToolForDrops()
            .sound(SoundType.BONE_BLOCK)
        );
    }

}
