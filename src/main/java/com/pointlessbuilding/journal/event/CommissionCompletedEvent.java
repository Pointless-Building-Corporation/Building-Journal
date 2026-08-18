package com.pointlessbuilding.journal.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class CommissionCompletedEvent extends Event{
    
    private final String commissionId;
    private final Player player;

    public CommissionCompletedEvent(Player player, String commissionId) {
        this.player = player;
        this.commissionId = commissionId;
    }

    public Player getPlayer() {
        return player;
    }

    public String getId() {
        return commissionId;
    }

}
