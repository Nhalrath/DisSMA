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

package net.nhalrath.DisSMA;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.nhalrath.DisSMA.commands.UtilityCommand;
import net.nhalrath.DisSMA.listeners.ReadyEvent;
import net.nhalrath.DisSMA.listeners.WordFilter;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static JDA jda;

    private static String guildId;
    private static String generalChannelId;
    private static String memeChannelId;
    private static String logChannelId;
    private static String mutedRoleId;

    public static String getGuildId() { return guildId; }
    public static String getGeneralChannelId() { return generalChannelId; }
    public static String getMemeChannelId() { return memeChannelId; }
    public static String getLogChannelId() { return logChannelId; }
    public static String getMutedRoleId() { return mutedRoleId; }

    public static void main(String[] args) {
        String token;

        token = System.getenv("TOKEN");
        guildId = System.getenv("GUILD");
        generalChannelId = System.getenv("CH_GENERAL");
        memeChannelId = System.getenv("CH_MEME");
        logChannelId = System.getenv("CH_LOG");
        mutedRoleId = System.getenv("RL_MUTE");

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--token") || args[i].equals("-t")) {
                if (args[i + 1].startsWith("--") || args[i + 1].startsWith("-")) return;
                token = args[++i];
            }
            else if (args[i].equals("--guild") || args[i].equals("-s")) {
                if (args[i + 1].startsWith("--") || args[i + 1].startsWith("-")) return;
                guildId = args[++i];
            }
            else if (args[i].equals("--general-channel") || args[i].equals("-g")) {
                if (args[i + 1].startsWith("--") || args[i + 1].startsWith("-")) return;
                generalChannelId = args[++i];
            }
            else if (args[i].equals("--meme-channel") || args[i].equals("-m")) {
                if (args[i + 1].startsWith("--") || args[i + 1].startsWith("-")) return;
                memeChannelId = args[++i];
            }
            else if (args[i].equals("--log-channel") || args[i].equals("-l")) {
                if (args[i + 1].startsWith("--") || args[i + 1].startsWith("-")) return;
                logChannelId = args[++i];
            }
            else if (args[i].equals("--muted-role") || args[i].equals("-x")) {
                if (args[i + 1].startsWith("--") || args[i + 1].startsWith("-")) return;
                mutedRoleId = args[++i];
            }
        }

        if (token == null ||
            guildId == null ||
            generalChannelId == null ||
            memeChannelId == null) {
                logger.error("One or more of the configuration variables are null");
                System.exit(1);
        }

        JDABuilder builder = JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.GUILD_MESSAGES)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .addEventListeners(
                new ReadyEvent(),
                new WordFilter(),
                new UtilityCommand());

        try {
            jda = builder.build();
            logger.info("JDA instance successfully built!");
        } catch (LoginException e) {
            logger.info("JDA failed to authenticate");
            e.printStackTrace();
        }

        final ScheduledFuture<?> logPosterHandle = scheduler.scheduleAtFixedRate(
            new Runnable() {
                public void run() {
                    for (File f : new File("tmp/logs").listFiles()) {
                        if (f.getName().endsWith(".log")) {
                            jda.getTextChannelById(logChannelId).sendFile(f).queue();
                        }
                    }
                }
            }, 1, 1, TimeUnit.HOURS);
        scheduler.schedule(
            new Runnable() {
                public void run() { logPosterHandle.cancel(false); }
            }, 1, TimeUnit.DAYS);
    }
}
