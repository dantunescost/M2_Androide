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
package apache.commons.math3.stat.regression;

import apache.commons.math3.exception.MathIllegalArgumentException;
import apache.commons.math3.exception.util.Localizable;

/**
 * Exception thrown when a regression model is not correctly specified.
 *
 * @since 3.0
 * @version $Id: ModelSpecificationException.java 1244107 2012-02-14 16:17:55Z erans $
 */
public class ModelSpecificationException extends MathIllegalArgumentException {
    /** Serializable version Id. */
    private static final long serialVersionUID = 4206514456095401070L;

    /**
     * @param pattern message pattern describing the specification error.
     *
     * @param args arguments.
     */
    public ModelSpecificationException(Localizable pattern,
                                        Object ... args) {
        super(pattern, args);
    }
}
