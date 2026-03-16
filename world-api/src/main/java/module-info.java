module org.dynamisengine.worldengine.api {
    requires transitive org.dynamisengine.core;
    requires transitive org.dynamisengine.ecs.api;
    requires transitive session.api;
    requires transitive scene.api;
    requires transitive content.api;

    exports org.dynamisengine.worldengine.api;
    exports org.dynamisengine.worldengine.api.config;
    exports org.dynamisengine.worldengine.api.lifecycle;
}
