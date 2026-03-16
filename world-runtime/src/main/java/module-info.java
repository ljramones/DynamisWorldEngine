module org.dynamisengine.worldengine.runtime {
    requires transitive org.dynamisengine.worldengine.api;
    requires org.dynamisengine.core;
    requires org.dynamisengine.ecs.core;
    requires org.vectrix;
    requires session.runtime;
    requires session.core;
    requires scene.core;
    requires content.runtime;
    requires content.core;

    exports org.dynamisengine.worldengine.runtime;
    exports org.dynamisengine.worldengine.runtime.projection;
    exports org.dynamisengine.worldengine.runtime.projection.components;
    exports org.dynamisengine.worldengine.runtime.session;
}
