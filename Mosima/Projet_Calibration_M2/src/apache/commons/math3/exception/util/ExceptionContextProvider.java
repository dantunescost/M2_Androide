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
package apache.commons.math3.exception.util;

/**
 * Interface for accessing the context data structure stored in Commons Math
 * exceptions.
 *
 * @version $Id: ExceptionContextProvider.java 1364388 2012-07-22 18:16:43Z tn $
 */
public interface ExceptionContextProvider {
    /**
     * Gets a reference to the "rich context" data structure that allows to
     * customize error messages and store key, value pairs in exceptions.
     *
     * @return a reference to the exception context.
     */
    ExceptionContext getContext();

}
