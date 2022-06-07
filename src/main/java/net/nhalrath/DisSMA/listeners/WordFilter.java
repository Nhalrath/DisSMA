/*
 * MIT License
 * 
 * Copyright (c) 2022 Nhalrath
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
    private final Logger logger = LoggerFactory.getLogger(WordFilter.class);

    private HashMap<String, Integer> memberOffenses;
    private List<String> badWords;
    private boolean strictMode;

    public WordFilter(boolean enforceStrict) {
        this.badWords = new ArrayList<String>();
        this.memberOffenses = new HashMap<String, Integer>();
        this.strictMode = enforceStrict;

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

        for (String w : message.getContentDisplay().split(" ")) {
            if (badWords.contains(w)) {
                String id = author.getId();
                logger.info(
                    "[{} - {}] {}",
                    author.getId(),
                    event.getChannel().getId(),
                    event.getMessage().getContentRaw());

                if (!strictMode) return;
                if (memberOffenses.containsKey(id)) {
                    memberOffenses.replace(id, memberOffenses.get(id) + 1);
                } else {
                    memberOffenses.put(id, 1);
                }

                int offenseCount = memberOffenses.get(id);
                if (offenseCount == 3) {
                    channel.sendMessage("""
                        You've sent words that are banned from this channel more than 3 times already
                        during this session.
                        If you think that this is a mistake, please contact the admin.
                        """).queue();
                }
                else if (offenseCount >= 5) {
                    event.getGuild().addRoleToMember(
                        id,
                        event.getGuild().getRoleById(App.getMutedRoleId())).queue();
                    channel.sendMessageFormat("%s has been muted.", event.getAuthor().getAsMention());
                }

                message.delete().queue();
                return;
            }
        }
    }
}
