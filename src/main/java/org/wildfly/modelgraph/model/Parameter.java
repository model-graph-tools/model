package org.wildfly.modelgraph.model;

import java.util.*;

import org.neo4j.driver.types.Node;

import static org.wildfly.modelgraph.model.ModelDescriptionConstants.*;

public class Parameter extends NamedModel {

    public static Parameter from(Node node) {
        Parameter parameter = new Parameter();
        mapId(node, parameter);
        mapName(node, parameter);
        mapParameter(node, parameter);

        return parameter;
    }

    static void mapParameter(Node node, Parameter parameter) {
        parameter.expressionAllowed = node.get(EXPRESSIONS_ALLOWED).isNull() ? null : node.get(EXPRESSIONS_ALLOWED)
                .asBoolean();
        parameter.nillable = node.get(NILLABLE).isNull() ? null : node.get(NILLABLE).asBoolean();
        parameter.required = node.get(REQUIRED).isNull() ? null : node.get(REQUIRED).asBoolean();
        parameter.max = node.get(MAX).isNull() ? null : node.get(MAX).asLong();
        parameter.min = node.get(MIN).isNull() ? null : node.get(MIN).asLong();
        parameter.maxLength = node.get(MAX_LENGTH).isNull() ? null : node.get(MAX_LENGTH).asLong();
        parameter.minLength = node.get(MIN_LENGTH).isNull() ? null : node.get(MIN_LENGTH).asLong();
        parameter.description = node.get(DESCRIPTION).asString(null);
        parameter.since = node.get(SINCE).asString(null);
        parameter.type = node.get(TYPE).asString(null);
        parameter.unit = node.get(UNIT).asString(null);
        parameter.valueType = node.get(VALUE_TYPE).asString(null);
    }

    // properties
    public Boolean expressionAllowed;
    public Boolean nillable;
    public Boolean required;
    public Long max;
    public Long maxLength;
    public Long min;
    public Long minLength;
    public String description;
    public String since;
    public String type;
    public String unit;
    public String valueType;
    public Deprecation deprecation;

    // relations
    public List<Parameter> parameters;
    public Set<String> alternatives;
    public Set<String> requires;
    public String capability;

    public Parameter() {
        // Required by JSON-B
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return name.equals(parameter.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    void addParameter(Parameter parameter) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        parameters.add(parameter);
    }

    void addAlternative(String alternative) {
        if (alternatives == null) {
            alternatives = new HashSet<>();
        }
        alternatives.add(alternative);
    }

    void addRequires(String require) {
        if (requires == null) {
            requires = new HashSet<>();
        }
        requires.add(require);
    }
}
