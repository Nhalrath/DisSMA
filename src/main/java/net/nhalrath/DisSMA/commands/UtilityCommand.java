package net.nhalrath.DisSMA.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UtilityCommand extends ListenerAdapter {
    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("ping")) {
            long time = System.currentTimeMillis();
            event.reply("Pinging...").flatMap(v ->
                event.getHook().editOriginalFormat("Ping: %dms", System.currentTimeMillis() - time)
            ).queue();
        }
    }
}
