/**
 * This class initializes a Discord bot using the JDA (Java Discord API) library.
 * It loads the bot configuration, including the authentication token, from a .env file
 * using the Dotenv library.
 *
 * Key Features:
 * - Configures the bot with gateway intents to handle guild messages and message content.
 * - Sets the bot's status to "Online".
 * - Registers an event listener to handle incoming Discord events.
 * - Handles invalid bot tokens gracefully by displaying an error message.
 *
 * @author Tanner Sellers
 */

package com.sellerstanner.vipcollector;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import javax.security.auth.login.LoginException;
import com.sellerstanner.vipcollector.listeners.EventListener;

import java.util.EnumSet;

public class VIPcollector {

    private final Dotenv config;
    private final ShardManager shardManager;

    /**
     * Constructs the VIPcollector bot instance.
     * - Loads the bot token from a .env file.
     * - Configures the bot's gateway intents and online status.
     * - Registers the EventListener for handling Discord events.
     *
     * @throws LoginException if the provided bot token is invalid.
     */

    public VIPcollector() throws LoginException {
        config = Dotenv.configure().ignoreIfMissing().load();
        String token = config.get("TOKEN");

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token,
                EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT));
        builder.setStatus(OnlineStatus.ONLINE);
        shardManager = builder.build();

        shardManager.addEventListener(new EventListener());
    }

    /**
     * Retrieves the configuration loaded from the .env file.
     *
     * @return Dotenv instance containing the configuration.
     */
    public Dotenv getConfig() {
        return config;
    }

    /**
     * Retrieves the ShardManager instance managing the bot's shards.
     *
     * @return ShardManager instance.
     */
    public ShardManager getShardManager() {
        return shardManager;
    }

    public static void main(String[] args) {
        try {
            VIPcollector bot = new VIPcollector();
        } catch(LoginException e) {
            System.out.println("ERROR: Provided bot token is invalid");
        }
    }
}
