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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageModifiedListener extends ListenerAdapter {
    private final Logger logger = LoggerFactory.getLogger(MessageModifiedListener.class);
    private HashMap<String, Map<String, String>> messageCache;

    public MessageModifiedListener() {
        this.messageCache = new HashMap<String, Map<String, String>>();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        HashMap<String, String> message = new HashMap<String, String>();
        message.put(event.getAuthor().getAsMention(), event.getMessage().getContentDisplay());
        messageCache.put(event.getMessageId(), message);
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        Map<String, String> message = messageCache.get(event.getMessageId());
        String messageAuthor = message.keySet().toArray(new String[message.size()])[0];
        String messageContent = message.get(messageAuthor);

        logger.info("""
                [{} - {}] Message edited
                Original:\t{}
                Edited:\t{}
                """,
                event.getAuthor().getId(),
                event.getChannel().getId(),
                messageContent,
                event.getMessage().getContentRaw());
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        Map<String, String> message = messageCache.get(event.getMessageId());
        String messageAuthor = message.keySet().toArray(new String[message.size()])[0];
        String messageContent = message.get(messageAuthor);
        
        logger.info("""
                [{} - {}] Message deleted
                Content:\t{}
                """,
                messageAuthor,
                event.getChannel().getId(),
                messageContent);
    }
}
