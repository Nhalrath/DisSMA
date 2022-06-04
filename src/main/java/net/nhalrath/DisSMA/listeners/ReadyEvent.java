package net.nhalrath.DisSMA.listeners;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReadyEvent extends ListenerAdapter {
    @Override
    public void onReady(net.dv8tion.jda.api.events.ReadyEvent event) {
        event.getJDA().getPresence().setPresence(
            OnlineStatus.ONLINE,
            Activity.watching("over you"));
    }
}
