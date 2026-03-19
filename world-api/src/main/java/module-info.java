module org.dynamisengine.worldengine.api {
    requires transitive org.dynamisengine.core;
    requires transitive org.dynamisengine.ecs.api;
    requires transitive org.dynamisengine.session.api;
    requires transitive org.dynamisengine.scenegraph.api;
    requires transitive org.dynamisengine.content.api;

    exports org.dynamisengine.worldengine.api;
    exports org.dynamisengine.worldengine.api.config;
    exports org.dynamisengine.worldengine.api.lifecycle;

    uses org.dynamisengine.worldengine.api.WorldEngine.WorldEngineHandleFactory;
}
