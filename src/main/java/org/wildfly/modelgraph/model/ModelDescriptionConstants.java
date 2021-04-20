/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.wildfly.modelgraph.model;

/** String constants frequently used in model descriptions and DMR operations. */
public interface ModelDescriptionConstants {

    // KEEP THESE IN ALPHABETICAL ORDER!
    String ACCESS_TYPE = "access-type";
    String ADDRESS = "address";
    String ALIAS = "alias";
    String ATTRIBUTE_GROUP = "attribute-group";

    String CHILD_DESCRIPTIONS = "child-descriptions";

    String DEFAULT = "default";
    String DESCRIPTION = "description";

    String EXPRESSIONS_ALLOWED = "expressions-allowed";

    String GLOBAL = "global";

    String IDENTIFIER = "identifier";

    String MANAGEMENT_VERSION = "management-version";
    String MAX = "max";
    String MAX_LENGTH = "max-length";
    String MAJOR = "major";
    String MIN = "min";
    String MIN_LENGTH = "min-length";
    String MINOR = "minor";

    String NAME = "name";
    String NILLABLE = "nillable";

    String PATCH = "patch";
    String PRODUCT_NAME = "product-name";
    String PRODUCT_VERSION = "product-version";

    String READ_ONLY = "read-only";
    String REASON = "reason";
    String REQUIRED = "required";
    String RESTART_REQUIRED = "restart-required";
    String RETURN_VALUE = "return-value";
    String RUNTIME_ONLY = "runtime-only";

    String SINCE = "since";
    String SINGLETON = "singleton";
    String STORAGE = "storage";

    String TYPE = "type";

    String UNIT = "unit";

    String VALUE_TYPE = "value-type";
}

