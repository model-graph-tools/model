package org.wildfly.modelgraph.model;

import org.neo4j.driver.types.Node;

import static org.wildfly.modelgraph.model.ModelDescriptionConstants.*;

public class Version extends Model {

    public static Version from(String value) {
        Version version = new Version();
        if (value != null && value.length() != 0) {
            try {
                String[] parts = value.split("\\.");
                if (parts.length == 3) {
                    version.major = Integer.parseInt(parts[0]);
                    version.minor = Integer.parseInt(parts[1]);
                    version.patch = Integer.parseInt(parts[2]);
                    version.ordinal = Version.ordinal(version.major, version.minor, version.patch);
                }
            } catch (NumberFormatException ignore) {
                // ignore
            }
        }
        return version;
    }

    public static Version from(Node node) {
        Version version = new Version();
        mapId(node, version);

        version.major = node.get(MAJOR).asInt(0);
        version.minor = node.get(MINOR).asInt(0);
        version.patch = node.get(PATCH).asInt(0);
        version.ordinal = Version.ordinal(version.major, version.minor, version.patch);

        return version;
    }

    private static int ordinal(int major, int minor, int patch) {
        int ordinal = 0;
        int[] numbers = new int[]{patch, minor, major};
        for (int i = 0; i < numbers.length; i++) {
            ordinal |= numbers[i] << i * 10;
        }
        return ordinal;
    }

    public int major;
    public int minor;
    public int patch;
    public int ordinal;

    public Version() {
        // Required by JSON-B
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }
}
