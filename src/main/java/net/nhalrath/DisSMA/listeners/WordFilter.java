package net.nhalrath.DisSMA.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.nhalrath.DisSMA.App;

public class WordFilter extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WordFilter.class);

    private HashMap<String, Integer> memberOffences;
    private List<String> badWords;

    public WordFilter() {
        badWords = new ArrayList<String>();
        memberOffences = new HashMap<String, Integer>();

        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("badwords.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                badWords.add(line);
            }
        } catch (IOException e) {
            logger.error("Failed to read badwords.txt");
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Member author = event.getMember();
        TextChannel channel = event.getChannel();
        Message message = event.getMessage();

        if (author.getUser() == event.getJDA().getSelfUser()) return;

        for (String w : badWords) {
            if (message.getContentDisplay().toLowerCase().contains(w)) {
                String id = author.getId();
                logger.info(
                    "[%s - %s] %s",
                    author.getEffectiveName(),
                    event.getChannel().getName(),
                    event.getMessage().getContentRaw());

                if (memberOffences.containsKey(id)) {
                    memberOffences.replace(id, memberOffences.get(id) + 1);
                } else {
                    memberOffences.put(id, 1);
                }

                int offenceCount = memberOffences.get(id);
                
                if (offenceCount == 3) {
                    channel.sendMessage("""
                        You've sent words that are banned from this channel more than 3 times already
                        during this session.
                        If you think that this is a mistake, please contact the admin.
                        """).queue();
                }
                else if (offenceCount >= 5) {
                    event.getGuild().addRoleToMember(
                        id,
                        event.getGuild().getRoleById(App.getMutedRoleId())).queue();
                    channel.sendMessageFormat("%s has been muted.", event.getAuthor().getAsMention());
                }
            }

            message.delete().queue();
        }
    }
}
