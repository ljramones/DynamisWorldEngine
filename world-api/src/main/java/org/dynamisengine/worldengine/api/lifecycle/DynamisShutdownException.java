package org.dynamisengine.worldengine.api.lifecycle;

import java.util.Objects;
import org.dynamisengine.core.exception.DynamisException;

/**
 * Exception thrown when a subsystem throws during shutdown.
 */
public final class DynamisShutdownException extends DynamisException {
  /**
   * Creates a shutdown exception.
   *
   * @param subsystemName subsystem name
   * @param cause root cause
   */
  public DynamisShutdownException(String subsystemName, Throwable cause) {
    super(
        "Subsystem '"
            + Objects.requireNonNull(subsystemName, "subsystemName must not be null")
            + "' threw during shutdown",
        cause);
  }

  @Override
  public boolean isRecoverable() {
    return true;
  }
}
