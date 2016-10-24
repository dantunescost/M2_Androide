/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package apache.commons.math3.optim.linear;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import apache.commons.math3.optim.OptimizationData;

/**
 * Class that represents a set of {@link LinearConstraint linear constraints}.
 *
 * @version $Id: LinearConstraintSet.java 1435539 2013-01-19 13:27:24Z tn $
 * @since 3.1
 */
public class LinearConstraintSet implements OptimizationData {
    /** Set of constraints. */
    private final Set<LinearConstraint> linearConstraints
        = new HashSet<LinearConstraint>();

    /**
     * Creates a set containing the given constraints.
     *
     * @param constraints Constraints.
     */
    public LinearConstraintSet(LinearConstraint... constraints) {
        for (LinearConstraint c : constraints) {
            linearConstraints.add(c);
        }
    }

    /**
     * Creates a set containing the given constraints.
     *
     * @param constraints Constraints.
     */
    public LinearConstraintSet(Collection<LinearConstraint> constraints) {
        linearConstraints.addAll(constraints);
    }

    /**
     * Gets the set of linear constraints.
     *
     * @return the constraints.
     */
    public Collection<LinearConstraint> getConstraints() {
        return Collections.unmodifiableSet(linearConstraints);
    }
}
