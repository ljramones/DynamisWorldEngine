module org.dynamisengine.worldengine.runtime {
    requires transitive org.dynamisengine.worldengine.api;
    requires org.dynamisengine.core;
    requires org.dynamisengine.ecs.core;
    requires org.dynamisengine.vectrix;
    requires org.dynamisengine.scenegraph.core;
    requires org.dynamisengine.session.runtime;
    requires org.dynamisengine.session.core;
    requires org.dynamisengine.content.runtime;
    requires org.dynamisengine.content.core;

    exports org.dynamisengine.worldengine.runtime;
    exports org.dynamisengine.worldengine.runtime.projection;
    exports org.dynamisengine.worldengine.runtime.projection.components;
    exports org.dynamisengine.worldengine.runtime.session;

    provides org.dynamisengine.worldengine.api.WorldEngine.WorldEngineHandleFactory
        with org.dynamisengine.worldengine.runtime.DefaultWorldEngineHandleFactory;
}
