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

public class EventListener extends ListenerAdapter {
    private final Dotenv channels = Dotenv.configure().load();
    String sourceID = channels.get("SOURCEID");
    String destID = channels.get("DESTID");

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

