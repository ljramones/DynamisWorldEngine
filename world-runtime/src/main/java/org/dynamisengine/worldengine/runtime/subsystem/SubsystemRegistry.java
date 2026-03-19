package org.dynamisengine.worldengine.runtime.subsystem;

import java.util.*;

/**
 * Registry of engine subsystems with dependency-ordered resolution.
 *
 * Subsystems are registered by name. Dependency ordering uses topological sort.
 * Fails fast on missing dependencies or cycles.
 */
public final class SubsystemRegistry {

    private final Map<String, WorldSubsystem> subsystems = new LinkedHashMap<>();

    /** Register a subsystem. Throws if name already registered. */
    public void register(WorldSubsystem subsystem) {
        Objects.requireNonNull(subsystem, "subsystem");
        String name = subsystem.name();
        if (subsystems.containsKey(name)) {
            throw new IllegalStateException("Subsystem already registered: " + name);
        }
        subsystems.put(name, subsystem);
    }

    /** Get a subsystem by name, or null if not registered. */
    public WorldSubsystem get(String name) {
        return subsystems.get(name);
    }

    /** All registered subsystem names. */
    public Set<String> names() {
        return Set.copyOf(subsystems.keySet());
    }

    /** Number of registered subsystems. */
    public int size() {
        return subsystems.size();
    }

    /**
     * Returns subsystems in dependency order (dependencies first).
     * Throws if a dependency is missing or a cycle is detected.
     */
    public List<WorldSubsystem> ordered() {
        return topologicalSort();
    }

    /**
     * Returns subsystems in reverse dependency order (dependents first).
     * Used for shutdown sequencing.
     */
    public List<WorldSubsystem> reverseOrdered() {
        List<WorldSubsystem> ordered = topologicalSort();
        List<WorldSubsystem> reversed = new ArrayList<>(ordered);
        Collections.reverse(reversed);
        return reversed;
    }

    // -- Topological sort (Kahn's algorithm) ----------------------------------

    private List<WorldSubsystem> topologicalSort() {
        // Build adjacency: for each subsystem, which others depend on it?
        Map<String, Set<String>> dependents = new LinkedHashMap<>();
        Map<String, Integer> inDegree = new LinkedHashMap<>();

        for (String name : subsystems.keySet()) {
            dependents.put(name, new LinkedHashSet<>());
            inDegree.put(name, 0);
        }

        for (WorldSubsystem sub : subsystems.values()) {
            for (String dep : sub.dependencies()) {
                if (!subsystems.containsKey(dep)) {
                    throw new IllegalStateException(
                            "Subsystem '" + sub.name() + "' depends on '" + dep +
                            "' which is not registered");
                }
                dependents.get(dep).add(sub.name());
                inDegree.merge(sub.name(), 1, Integer::sum);
            }
        }

        // Kahn's: start with nodes that have no dependencies
        Queue<String> queue = new ArrayDeque<>();
        for (var entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<WorldSubsystem> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(subsystems.get(current));
            for (String dependent : dependents.get(current)) {
                int remaining = inDegree.get(dependent) - 1;
                inDegree.put(dependent, remaining);
                if (remaining == 0) {
                    queue.add(dependent);
                }
            }
        }

        if (result.size() != subsystems.size()) {
            throw new IllegalStateException(
                    "Dependency cycle detected among subsystems. Registered: " +
                    subsystems.keySet() + ", resolved: " +
                    result.stream().map(WorldSubsystem::name).toList());
        }

        return result;
    }
}
