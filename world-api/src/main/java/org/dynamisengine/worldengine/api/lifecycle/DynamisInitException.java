package org.dynamisengine.worldengine.api.lifecycle;

import java.util.Objects;
import org.dynamisengine.core.exception.DynamisException;

/**
 * Exception thrown when a subsystem fails to initialize.
 */
public final class DynamisInitException extends DynamisException {
  private final String subsystemName;

  /**
   * Creates an initialization exception.
   *
   * @param subsystemName subsystem name
   * @param reason failure reason
   * @param cause root cause
   */
  public DynamisInitException(String subsystemName, String reason, Throwable cause) {
    super(
        "Subsystem '"
            + Objects.requireNonNull(subsystemName, "subsystemName must not be null")
            + "' failed to initialize: "
            + Objects.requireNonNull(reason, "reason must not be null"),
        cause);
    this.subsystemName = subsystemName;
  }

  /**
   * Returns the failing subsystem name.
   *
   * @return subsystem name
   */
  public String subsystemName() {
    return subsystemName;
  }

  @Override
  public String component() {
    return "dynamis-world-engine";
  }

  @Override
  public boolean isRecoverable() {
    return false;
  }
}
