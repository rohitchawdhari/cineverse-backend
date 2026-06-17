package com.cineverse.cineversebackend.config;

import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedMongoConfig {

    private TransitionWalker.ReachedState<RunningMongodProcess> running;

    @PostConstruct
    public void start() {
        try {
            System.out.println("[Embedded Mongo] Starting in-memory MongoDB on port 27017...");
            
            Net net = Net.builder()
                    .bindIp("127.0.0.1")
                    .port(27017)
                    .isIpv6(Network.localhostIsIPv6())
                    .build();

            Mongod mongod = Mongod.instance()
                    .withNet(Start.to(Net.class).initializedWith(net));

            // Start V6_0 or PRODUCTION version of MongoDB
            running = mongod.start(Version.Main.V6_0);
            
            System.out.println("[Embedded Mongo] Successfully started in-memory MongoDB on port 27017");
        } catch (Exception e) {
            System.err.println("[Embedded Mongo] Failed to start in-memory MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stop() {
        if (running != null) {
            try {
                running.close();
                System.out.println("[Embedded Mongo] Stopped in-memory MongoDB");
            } catch (Exception e) {
                System.err.println("[Embedded Mongo] Failed to stop in-memory MongoDB: " + e.getMessage());
            }
        }
    }
}
