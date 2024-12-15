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

    public VIPcollector() throws LoginException {
        config = Dotenv.configure().ignoreIfMissing().load();
        String token = config.get("TOKEN");

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token,
                EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT));
        builder.setStatus(OnlineStatus.ONLINE);
        shardManager = builder.build();

        shardManager.addEventListener(new EventListener());
    }

    public Dotenv getConfig() {
        return config;
    }

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
