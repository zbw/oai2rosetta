package actors;

import akka.actor.ActorSystem;

/**
 * Created by Ott Konstantin on 08.10.2014.
 */
public class RootActorSystem {
    static ActorSystem actorSystem = ActorSystem.create("zbwSubApp");
    private static RootActorSystem instance = new RootActorSystem();



    private RootActorSystem() {
    }

    public static RootActorSystem getInstance() {
        return instance;
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }
}
