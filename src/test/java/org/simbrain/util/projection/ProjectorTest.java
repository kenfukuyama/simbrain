package org.simbrain.util.projection;

import org.junit.Test;
import org.nd4j.linalg.dataset.DataSet;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ProjectorTest {

    @Test
    public void basicTest() {

        Projector proj = new Projector(5);
        proj.setUseColorManager(false);
        proj.addDatapoint(new DataPoint(new double[]{1,0,0,0,0}));
        proj.addDatapoint(new DataPoint(new double[]{0,1,0,0,0}));
        proj.addDatapoint(new DataPoint(new double[]{0,0,1,0,0}));
        proj.addDatapoint(new DataPoint(new double[]{0,0,0,1,0}));
        proj.addDatapoint(new DataPoint(new double[]{0,0,0,1,1}));

        // TODO: Below fails when calling no-arg constructor
        // Be sure the projector has 5 dimensions
        assertEquals(5, proj.getDimensions());

    }
}