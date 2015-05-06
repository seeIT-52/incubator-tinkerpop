/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.traversal.strategy.optimization;

import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.LambdaHolder;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeOtherVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PathStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This strategy looks for <code>.outE().inV()</code>, <code>.inE().outV()</code> and <code>.bothE().otherV()</code>
 * and replaces these step sequences with <code>.out()</code>, <code>.in()</code> or <code>.both()</code> respectively.
 * The strategy won't modify the traversal if:
 * <ul>
 * <li>the edge step is labeled</li>
 * <li>the traversal contains a <code>path</code> step</li>
 * <li>the traversal contains a lambda step</li>
 * </ul>
 * <p/>
 *
 * @author Daniel Kuppitz (http://gremlin.guru)
 */
public final class IncidentToAdjacentStrategy extends AbstractTraversalStrategy<TraversalStrategy.OptimizationStrategy>
        implements TraversalStrategy.OptimizationStrategy {

    private static final IncidentToAdjacentStrategy INSTANCE = new IncidentToAdjacentStrategy();

    private IncidentToAdjacentStrategy() {
    }

    @Override
    public void apply(Traversal.Admin<?, ?> traversal) {
        final int size = traversal.getSteps().size() - 1;
        boolean isOptimizable = true;
        final Collection<Pair<VertexStep, Step>> stepsToReplace = new ArrayList<>();
        Step prev = null;
        for (int i = 0; i <= size && isOptimizable; i++) {
            final Step curr = traversal.getSteps().get(i);
            if (isOptimizable(prev, curr)) {
                stepsToReplace.add(Pair.with((VertexStep) prev, curr));
            }
            isOptimizable = !(curr instanceof PathStep) && !(curr instanceof LambdaHolder);
            prev = curr;
        }
        if (isOptimizable && !stepsToReplace.isEmpty()) {
            for (final Pair<VertexStep, Step> steps : stepsToReplace) {
                optimizeSteps(traversal, steps.getValue0(), steps.getValue1());
            }
        }
    }

    /**
     * Checks whether a given step is optimizable or not.
     *
     * @param step1 an edge-emitting step
     * @param step2 a vertex-emitting step
     * @return <code>true</code> if step1 is not labeled and emits edges and step2 emits vertices,
     * otherwise <code>false</code>
     */
    private static boolean isOptimizable(final Step step1, final Step step2) {
        if (step1 instanceof VertexStep && Edge.class.equals(((VertexStep) step1).getReturnClass()) && step1.getLabels().isEmpty()) {
            final Direction step1Dir = ((VertexStep) step1).getDirection();
            if (step1Dir.equals(Direction.BOTH)) {
                return step2 instanceof EdgeOtherVertexStep;
            }
            return step2 instanceof EdgeVertexStep && ((EdgeVertexStep) step2).getDirection().equals(step1Dir.opposite());
        }
        return false;
    }

    /**
     * Optimizes the given edge-emitting step and the vertex-emitting step by replacing them with a single
     * vertex-emitting step.
     *
     * @param traversal the traversal that holds the given steps
     * @param step1     the edge-emitting step to replace
     * @param step2     the vertex-emitting step to replace
     */
    private static void optimizeSteps(final Traversal.Admin traversal, final VertexStep step1, final Step step2) {
        final Step newStep = new VertexStep(traversal, Vertex.class, step1.getDirection(), step1.getEdgeLabels());
        TraversalHelper.replaceStep(step1, newStep, traversal);
        traversal.removeStep(step2);
    }

    public static IncidentToAdjacentStrategy instance() {
        return INSTANCE;
    }
}