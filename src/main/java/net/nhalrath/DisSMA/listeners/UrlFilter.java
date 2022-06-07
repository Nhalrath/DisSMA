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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.nhalrath.DisSMA.App;

public class UrlFilter extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(UrlFilter.class);

    private HashMap<String, Integer> memberOffenses;
    private boolean strictMode;

    public UrlFilter(boolean enforceStrict) {
        this.memberOffenses = new HashMap<String, Integer>();
        this.strictMode = enforceStrict;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Member author = event.getMember();
        TextChannel channel = event.getChannel();
        Message message = event.getMessage();
        String[] potentiallyDangerousUrlProtocols = { "http://", "ws://" };

        if (author.getUser() == event.getJDA().getSelfUser()) return;
        
        for (String s : potentiallyDangerousUrlProtocols) {
            // We could use regex instead for more accurate match.
            if (message.getContentDisplay().toLowerCase().contains(s)) {
                String id = author.getId();
                logger.info(
                    "[{} - {}] {}",
                    author.getAsMention(),
                    event.getChannel().getAsMention(),
                    event.getMessage().getContentRaw());

                if (!strictMode) return;
                if (memberOffenses.containsKey(id)) {
                    memberOffenses.replace(id, memberOffenses.get(id) + 1);
                } else {
                    memberOffenses.put(id, 1);
                }

                if (!strictMode) return;
                message.reply("Hold on right there! That's is a potentially dangerous link.");

                int offenseCount = memberOffenses.get(id);
                if (offenseCount == 3) {
                    channel.sendMessage("""
                        You've sent potentially dangerous link this channel more than 3 times already
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
            }
        }
    }
}
 