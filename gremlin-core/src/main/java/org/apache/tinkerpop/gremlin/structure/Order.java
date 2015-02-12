/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.tinkerpop.gremlin.structure;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public enum Order implements Comparator<Object> {

    incr {
        @Override
        public int compare(final Object first, final Object second) {
            return Comparator.<Comparable>naturalOrder().compare((Comparable) first, (Comparable) second);
        }

        @Override
        public Order opposite() {
            return decr;
        }
    }, decr {
        @Override
        public int compare(final Object first, final Object second) {
            return Comparator.<Comparable>reverseOrder().compare((Comparable) first, (Comparable) second);
        }

        @Override
        public Order opposite() {
            return incr;
        }
    }, keyIncr {
        @Override
        public int compare(final Object first, final Object second) {
            return Comparator.<Comparable>naturalOrder().compare(((Map.Entry<Comparable, ?>) first).getKey(), ((Map.Entry<Comparable, ?>) second).getKey());
        }

        @Override
        public Order opposite() {
            return keyDecr;
        }
    }, valueIncr {
        @Override
        public int compare(final Object first, final Object second) {
            return Comparator.<Comparable>naturalOrder().compare(((Map.Entry<?, Comparable>) first).getValue(), ((Map.Entry<?, Comparable>) second).getValue());
        }

        @Override
        public Order opposite() {
            return valueDecr;
        }
    }, keyDecr {
        @Override
        public int compare(final Object first, final Object second) {
            return Comparator.<Comparable>reverseOrder().compare(((Map.Entry<Comparable, ?>) first).getKey(), ((Map.Entry<Comparable, ?>) second).getKey());
        }

        @Override
        public Order opposite() {
            return keyIncr;
        }
    }, valueDecr {
        @Override
        public int compare(final Object first, final Object second) {
            return Comparator.<Comparable>reverseOrder().compare(((Map.Entry<?, Comparable>) first).getValue(), ((Map.Entry<?, Comparable>) second).getValue());
        }

        @Override
        public Order opposite() {
            return valueIncr;
        }
    }, shuffle {
        @Override
        public int compare(final Object first, final Object second) {
            return RANDOM.nextBoolean() ? -1 : 1;
        }

        @Override
        public Order opposite() {
            return shuffle;
        }
    };

    private static final Random RANDOM = new Random();

    /**
     * {@inheritDoc}
     */
    public abstract int compare(final Object first, final Object second);

    /**
     * Produce the opposite representation of the current {@code Order} enum.
     */
    public abstract Order opposite();
}