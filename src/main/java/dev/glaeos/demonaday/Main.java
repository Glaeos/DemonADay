package dev.glaeos.demonaday;

import discord4j.core.DiscordClient;
import reactor.core.publisher.Mono;

public class Main {
    public static void main(String[] args) {
        DiscordClient.create(Env.TOKEN).withGateway(client -> Mono.empty()).block();
    }
}