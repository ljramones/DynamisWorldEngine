package org.dynamisengine.worldengine.api.lifecycle;

import java.util.Objects;
import org.dynamisengine.core.exception.DynamisException;

/**
 * Exception thrown when a subsystem throws during a tick.
 */
public final class DynamisTickException extends DynamisException {
  private final String subsystemName;
  private final long tickNumber;

  /**
   * Creates a tick exception.
   *
   * @param subsystemName subsystem name
   * @param tickNumber tick number
   * @param cause root cause
   */
  public DynamisTickException(String subsystemName, long tickNumber, Throwable cause) {
    super(
        "Subsystem '"
            + Objects.requireNonNull(subsystemName, "subsystemName must not be null")
            + "' threw during tick "
            + tickNumber,
        cause);
    this.subsystemName = subsystemName;
    this.tickNumber = tickNumber;
  }

  /**
   * Returns the failing subsystem name.
   *
   * @return subsystem name
   */
  public String subsystemName() {
    return subsystemName;
  }

  /**
   * Returns the tick number when the error occurred.
   *
   * @return tick number
   */
  public long tickNumber() {
    return tickNumber;
  }

  @Override
  public boolean isRecoverable() {
    return true;
  }
}
