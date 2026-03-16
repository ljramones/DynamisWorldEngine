package org.dynamisengine.worldengine.api.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisengine.core.exception.DynamisException;
import org.junit.jupiter.api.Test;

class LifecycleExceptionTest {
  @Test
  void initExceptionBehaviorMatchesContract() {
    RuntimeException cause = new RuntimeException("root");
    DynamisInitException exception =
        new DynamisInitException("Physics", "native context failed", cause);

    assertEquals(
        "Subsystem 'Physics' failed to initialize: native context failed", exception.getMessage());
    assertEquals("Physics", exception.subsystemName());
    assertEquals("dynamis-world-engine", exception.component());
    assertFalse(exception.isRecoverable());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void tickExceptionBehaviorMatchesContract() {
    RuntimeException cause = new RuntimeException("root");
    DynamisTickException exception = new DynamisTickException("Renderer", 42L, cause);

    assertEquals("Subsystem 'Renderer' threw during tick 42", exception.getMessage());
    assertEquals("Renderer", exception.subsystemName());
    assertEquals(42L, exception.tickNumber());
    assertTrue(exception.isRecoverable());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void shutdownExceptionBehaviorMatchesContract() {
    RuntimeException cause = new RuntimeException("root");
    DynamisShutdownException exception = new DynamisShutdownException("Audio", cause);

    assertEquals("Subsystem 'Audio' threw during shutdown", exception.getMessage());
    assertTrue(exception.isRecoverable());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void specializedExceptionsAreRuntimeAndDynamisExceptions() {
    DynamisException init = new DynamisInitException("A", "reason", null);
    assertInstanceOf(DynamisException.class, init);
    assertInstanceOf(RuntimeException.class, init);

    DynamisException tick = new DynamisTickException("B", 1L, null);
    assertInstanceOf(DynamisException.class, tick);
    assertInstanceOf(RuntimeException.class, tick);

    DynamisException shutdown = new DynamisShutdownException("C", null);
    assertInstanceOf(DynamisException.class, shutdown);
    assertInstanceOf(RuntimeException.class, shutdown);
  }
}
