package com.sellerstanner.vipcollector.listeners;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

/**
 * EventListener class listens for Discord message events and handles specific commands
 * related to reposting messages containing "@VIP".
 *
 * Key Features:
 * - Listens for the `!collect_vip` command in text channels.
 * - Fetches messages containing "@VIP" from a source channel posted on the current day.
 * - Reposts collected messages to a designated target channel.
 * - Handles errors gracefully, such as invalid channel IDs or message fetch issues.
 *
 * Configuration:
 * - The source and target channel IDs are loaded from a .env file.
 *
 * Dependencies:
 * - Requires the JDA (Java Discord API) library.
 * - Uses Dotenv for managing environment variables.
 *
 * @author Tanner Sellers
 */

public class EventListener extends ListenerAdapter {
    private final Dotenv channels = Dotenv.configure().load();
    String sourceID = channels.get("SOURCEID");
    String destID = channels.get("DESTID");

    /**
     * Handles the reception of messages in Discord text channels.
     *
     * @param event The MessageReceivedEvent triggered by Discord.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ensure the message comes from a text channel
        if (event.isFromType(ChannelType.TEXT)) {
            TextChannel sourceChannel = event.getJDA().getTextChannelById(sourceID);
            TextChannel targetChannel = event.getJDA().getTextChannelById(destID);

            if (sourceChannel == null || targetChannel == null) {
                System.out.println("Error: Source or Target channel not found.");
                return;
            }

            // Command to manually trigger reposting @VIP messages
            if (event.getMessage().getContentRaw().equalsIgnoreCase("!collect_vip")) {
                repostVIPMessages(sourceChannel, targetChannel, event);
            }
        }
    }

    /**
     * Collects and reposts messages containing "@VIP" from the source channel to the target channel.
     *
     * Steps:
     * - Fetches up to 100 recent messages from the source channel.
     * - Filters messages containing "@VIP" and posted on the current day.
     * - Reposts the filtered messages to the target channel.
     *
     * @param sourceChannel The channel to collect messages from.
     * @param targetChannel The channel to repost messages to.
     * @param event The original MessageReceivedEvent for context and error handling.
     */
    private void repostVIPMessages(TextChannel sourceChannel, TextChannel targetChannel, MessageReceivedEvent event) {
        List<Message> vipMessages = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Fetch messages from the source channel
        sourceChannel.getIterableHistory()
                .takeAsync(100)  // Limit to 100 messages at a time
                .thenAccept(messages -> {
                    // Iterate over fetched messages
                    for (Message message : messages) {
                        // Check if the message contains @VIP and is from today
                        if (message.getTimeCreated().toLocalDate().equals(today) && message.getContentRaw().contains("@VIP")) {
                            if (!vipMessages.contains(message)) {
                                vipMessages.add(message);
                            }
                        }
                    }

                    vipMessages.sort(Comparator.comparing(Message::getTimeCreated));

                    // Check if there are any VIP messages to repost
                    if (vipMessages.isEmpty()) {
                        event.getChannel().sendMessage("No VIP messages found for today.").queue();
                    } else {
                        // Repost all collected messages
                        for (Message vipMessage : vipMessages) {
                            targetChannel.sendMessage(vipMessage.getContentRaw()).queue();
                        }
                    }

                })
                .exceptionally(ex -> {
                    event.getChannel().sendMessage("An error occurred while fetching messages.").queue();
                    return null;
                });
    }
}

