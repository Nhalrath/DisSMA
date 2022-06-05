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
    private static String[] opts;

    private static JDA jda;

    private static String guildId;
    private static String generalChannelId;
    private static String memeChannelId;
    private static String logChannelId;
    private static String mutedRoleId;
    private static boolean enforceStrict = true;

    public static String getGuildId() { return guildId; }
    public static String getGeneralChannelId() { return generalChannelId; }
    public static String getMemeChannelId() { return memeChannelId; }
    public static String getLogChannelId() { return logChannelId; }
    public static String getMutedRoleId() { return mutedRoleId; }

    private static String getOption(String key, String longOpt, String shortOpt) {
        for (int i = 0; i < opts.length; i++) {
            if (opts[i].equals(longOpt) || opts[i].equals(shortOpt)) {
                i++;
                if (!(opts[i].startsWith("--") || opts[i].startsWith("-")))
                    return opts[i];
            }
        }
        return System.getenv(key) != null ? System.getenv(key) : null;
    }

    public static void main(String[] args) {
        opts = args;
        String token;

        token = getOption("TOKEN", "--token", "-t");
        guildId = getOption("GUILD", "--guild", "-s");
        generalChannelId = getOption("CH_GENERAL", "--channel-general", "-g");
        memeChannelId = getOption("CH_MEME", "-channel-meme", "-m");
        logChannelId = getOption("CH_LOG", "--channel-logs", "-l");
        mutedRoleId = getOption("RL_MUTE", "--role-mute", "-x");
        enforceStrict =
            Boolean.parseBoolean(getOption("OPT_ENFORCE_STRICT", "--enforce-strict", "-d"));

        if (token == null ||
            guildId == null ||
            generalChannelId == null ||
            memeChannelId == null ||
            logChannelId == null ||
            mutedRoleId == null) {
                logger.error("One or more of the configuration variables are null");
                System.exit(1);
        }

        JDABuilder builder = JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.GUILD_MESSAGES)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .addEventListeners(
                new ReadyEvent(),
                new WordFilter(enforceStrict),
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
            }, 24, TimeUnit.HOURS);
    }
}
