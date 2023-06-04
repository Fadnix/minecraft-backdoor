package fadnix.meow;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot extends JavaPlugin {

    private JDA jda;
    private MessageChannel channel;

    @Override
    public void onEnable() {
        getLogger().info("Plugin enabled.");

        try {
            String discordToken = "your bot's token";
            String channelId = "your channel id";

            jda = JDABuilder.createDefault(discordToken)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .build();
            jda.awaitReady();
            channel = jda.getTextChannelById(channelId);
            jda.addEventListener(new DiscordEventListener());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled.");
    }

    class DiscordEventListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getChannel() instanceof GuildMessageChannel && event.getChannel().equals(channel)) {
                String message = event.getMessage().getContentRaw();
                String[] args = message.split(" ");

                if (args.length >= 1 && args[0].equalsIgnoreCase("!exec")) {
                    ((MessageChannel) event.getChannel()).sendMessage("command has been executed").queue();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        sb.append(args[i]);
                        sb.append(" ");
                    }
                    String command = sb.toString().trim();

                    BukkitScheduler scheduler = Bukkit.getScheduler();
                    scheduler.runTask(DiscordBot.this, () -> {
                        boolean result = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                        if (!result) {
                            ((MessageChannel) event.getChannel()).sendMessage("Server command failed").queue();
                        }
                    });
                } else if (args.length >= 1 && args[0].equalsIgnoreCase("ping")) {
                    ((MessageChannel) event.getChannel()).sendMessage("Pong!").queue();
                }
            }
        }
    }
}
