package com.pointlessbuilding.journal.client;

import com.mojang.blaze3d.platform.InputConstants.Type;

import net.minecraft.client.KeyMapping;

public class JournalKeyMap extends KeyMapping{

    public JournalKeyMap(String name, Type type, int keyCode, String category) {
        super(name, type, keyCode, category);
    }
    
}
