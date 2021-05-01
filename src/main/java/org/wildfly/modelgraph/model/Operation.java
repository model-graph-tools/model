package org.wildfly.modelgraph.model;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.driver.types.Node;

import static org.wildfly.modelgraph.model.ModelDescriptionConstants.*;

@SuppressWarnings("WeakerAccess")
public class Operation extends NamedModel {

    public static Operation from(Node node) {
        Operation operation = new Operation();
        mapId(node, operation);
        mapName(node, operation);

        operation.global = node.get(GLOBAL).isNull() ? null : node.get(GLOBAL).asBoolean();
        operation.readOnly = node.get(READ_ONLY).isNull() ? null : node.get(READ_ONLY).asBoolean();
        operation.runtimeOnly = node.get(RUNTIME_ONLY).isNull() ? null : node.get(RUNTIME_ONLY).asBoolean();
        operation.description = node.get(DESCRIPTION).asString(null);
        operation.returnValue = node.get(RETURN_VALUE).asString(null);
        operation.valueType = node.get(VALUE_TYPE).asString(null);

        return operation;
    }

    // properties
    public Boolean global;
    public Boolean readOnly;
    public Boolean runtimeOnly;
    public String description;
    public String returnValue;
    public String valueType;
    public Deprecation deprecation;

    // relations
    public Set<Parameter> parameters;
    public String providedBy;

    public Operation() {
        // Required by JSON-B
    }

    void addParameter(Parameter parameter) {
        if (parameters == null) {
            parameters = new HashSet<>();
        }
        parameters.add(parameter);
    }
}
