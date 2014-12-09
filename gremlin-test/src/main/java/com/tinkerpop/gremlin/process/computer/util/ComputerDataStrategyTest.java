package com.tinkerpop.gremlin.process.computer.util;

import com.tinkerpop.gremlin.AbstractGremlinTest;
import com.tinkerpop.gremlin.process.AbstractGremlinProcessTest;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.VertexProperty;
import com.tinkerpop.gremlin.structure.strategy.StrategyWrappedGraph;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class ComputerDataStrategyTest extends AbstractGremlinTest {

    @Test
    public void shouldFilterHiddenProperties() {
        final StrategyWrappedGraph sg = new StrategyWrappedGraph(g);
        sg.getStrategy().setGraphStrategy(new ComputerDataStrategy("***"));

        final Vertex v = sg.addVertex("***hidden-guy", "X", "not-hidden-guy", "Y");
        final Iterator<VertexProperty<Object>> props = v.iterators().propertyIterator();
        final VertexProperty v1 = props.next();
        assertEquals("Y", v1.value());
        assertEquals("not-hidden-guy", v1.key());
        assertFalse(props.hasNext());
    }

    @Test
    public void shouldAccessHiddenProperties() {
        final StrategyWrappedGraph sg = new StrategyWrappedGraph(g);
        sg.getStrategy().setGraphStrategy(new ComputerDataStrategy("***"));

        final Vertex v = sg.addVertex("***hidden-guy", "X", "not-hidden-guy", "Y");
        final Iterator<VertexProperty<Object>> props = v.iterators().propertyIterator("***hidden-guy");
        final VertexProperty v1 = props.next();
        assertEquals("X", v1.value());
        assertEquals("***hidden-guy", v1.key());
        assertFalse(props.hasNext());
    }

    @Test
    public void shouldHideHiddenKeys() {
        final StrategyWrappedGraph sg = new StrategyWrappedGraph(g);
        sg.getStrategy().setGraphStrategy(new ComputerDataStrategy("***"));

        final Vertex v = sg.addVertex("***hidden-guy", "X", "not-hidden-guy", "Y");
        final Set<String> keys = v.keys();
        assertTrue(keys.contains("not-hidden-guy"));
        assertFalse(keys.contains("***hidden-guy"));
    }
}