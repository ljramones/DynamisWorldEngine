module org.dynamisengine.worldengine.samples {
    requires transitive org.dynamisengine.worldengine.runtime;
    requires org.dynamisengine.core;
    requires org.dynamisengine.window.api;
    requires org.dynamisengine.window.test;
    requires org.vectrix;
    requires engine.api;
    requires content.api;
    requires content.core;
    requires content.runtime;
    requires input.api;
    requires input.core;
    requires input.runtime;

    exports org.dynamisengine.worldengine.samples;
    exports org.dynamisengine.worldengine.samples.codecs;
    exports org.dynamisengine.worldengine.samples.content;
    exports org.dynamisengine.worldengine.samples.content.upload;
    exports org.dynamisengine.worldengine.samples.input;
    exports org.dynamisengine.worldengine.samples.render;
    exports org.dynamisengine.worldengine.samples.window;
}
