package org.dynamisengine.worldengine.runtime.subsystem;

import org.dynamisengine.worldengine.api.WorldContext;
import org.dynamisengine.worldengine.api.lifecycle.DynamisInitException;
import org.dynamisengine.worldengine.api.lifecycle.DynamisShutdownException;
import org.dynamisengine.worldengine.api.lifecycle.DynamisTickException;
import org.dynamisengine.worldengine.api.telemetry.SubsystemHealth;
import org.dynamisengine.worldengine.api.telemetry.SubsystemTelemetry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Coordinates lifecycle and telemetry across registered subsystems.
 *
 * Drives the deterministic lifecycle sequence:
 * {@code initializeAll → startAll → tickAll (per frame) → stopAll → shutdownAll}
 *
 * All operations respect dependency ordering from the registry.
 * Failure policy:
 * <ul>
 *   <li>Init failure: subsystem marked FAULTED, exception propagates → engine FAULTED</li>
 *   <li>Recoverable tick failure: logged, subsystem continues</li>
 *   <li>Non-recoverable tick failure: subsystem marked FAULTED, exception propagates</li>
 *   <li>Shutdown failure: logged, shutdown continues for remaining subsystems</li>
 * </ul>
 */
public final class SubsystemCoordinator {

    private static final System.Logger LOG =
            System.getLogger(SubsystemCoordinator.class.getName());

    private final SubsystemRegistry registry;
    private List<WorldSubsystem> initOrder;
    private List<WorldSubsystem> shutdownOrder;

    public SubsystemCoordinator(SubsystemRegistry registry) {
        this.registry = registry;
    }

    /**
     * Initialize all subsystems in dependency order.
     * Throws DynamisInitException if any subsystem fails.
     */
    public void initializeAll(WorldContext context) throws DynamisInitException {
        initOrder = registry.ordered();
        shutdownOrder = registry.reverseOrdered();

        for (WorldSubsystem sub : initOrder) {
            LOG.log(System.Logger.Level.INFO, "Initializing subsystem: {0}", sub.name());
            sub.initialize(context);
        }
    }

    /**
     * Start all subsystems (after initialization) in dependency order.
     */
    public void startAll() throws DynamisInitException {
        for (WorldSubsystem sub : initOrder) {
            LOG.log(System.Logger.Level.DEBUG, "Starting subsystem: {0}", sub.name());
            sub.start();
        }
    }

    /**
     * Tick all subsystems in dependency order.
     * Recoverable exceptions are logged; non-recoverable propagate.
     */
    public void tickAll(long tick, float deltaSeconds) throws DynamisTickException {
        for (WorldSubsystem sub : initOrder) {
            try {
                sub.tick(tick, deltaSeconds);
            } catch (DynamisTickException e) {
                if (e.isRecoverable()) {
                    LOG.log(System.Logger.Level.WARNING,
                            "Recoverable tick error in {0}: {1}", sub.name(), e.getMessage());
                } else {
                    throw e; // propagate non-recoverable
                }
            }
        }
    }

    /**
     * Stop all subsystems in reverse dependency order.
     * Errors are logged but don't prevent other subsystems from stopping.
     */
    public void stopAll() {
        for (WorldSubsystem sub : shutdownOrder) {
            try {
                LOG.log(System.Logger.Level.DEBUG, "Stopping subsystem: {0}", sub.name());
                sub.stop();
            } catch (DynamisShutdownException e) {
                LOG.log(System.Logger.Level.WARNING,
                        "Error stopping {0}: {1}", sub.name(), e.getMessage());
            } catch (Throwable t) {
                LOG.log(System.Logger.Level.WARNING,
                        "Unexpected error stopping " + sub.name() + ": " + t.getMessage(), t);
            }
        }
    }

    /**
     * Shutdown all subsystems in reverse dependency order.
     * Errors are logged but don't prevent other subsystems from shutting down.
     */
    public void shutdownAll() {
        for (WorldSubsystem sub : shutdownOrder) {
            try {
                LOG.log(System.Logger.Level.DEBUG, "Shutting down subsystem: {0}", sub.name());
                sub.shutdown();
            } catch (DynamisShutdownException e) {
                LOG.log(System.Logger.Level.WARNING,
                        "Error shutting down {0}: {1}", sub.name(), e.getMessage());
            } catch (Throwable t) {
                LOG.log(System.Logger.Level.WARNING,
                        "Unexpected error shutting down " + sub.name() + ": " + t.getMessage(), t);
            }
        }
    }

    /**
     * Capture health + telemetry from all subsystems.
     * Returns a map suitable for WorldTelemetrySnapshot.
     */
    public Map<String, SubsystemTelemetry> telemetrySnapshot() {
        Map<String, SubsystemTelemetry> result = new LinkedHashMap<>();
        for (WorldSubsystem sub : initOrder != null ? initOrder : List.<WorldSubsystem>of()) {
            SubsystemHealth health = sub.health();
            Object detail = sub.captureTelemetry().orElse(null);
            result.put(sub.name(), SubsystemTelemetry.of(health, detail));
        }
        return result;
    }
}
