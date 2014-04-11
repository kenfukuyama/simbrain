/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.network.gui.dialogs;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.simbrain.network.core.Synapse;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.plot.histogram.HistogramModel;
import org.simbrain.plot.histogram.HistogramPanel;
import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.math.SimbrainMath;
import org.simbrain.util.randomizer.Randomizer;
import org.simbrain.util.randomizer.gui.RandomizerPanel;

/**
 * Panel for editing collections of synapses.
 *
 * @author Zach Tosi
 * @author Jeff Yoshimi
 *
 */
public class SynapseAdjustmentPanel extends JPanel {

    /** A reference to the parent network panel. */
    private final NetworkPanel networkPanel;

    /** Random source for randomizing inhibitory synapses. */
    private Randomizer inhibitoryRandomizer = new Randomizer();

    /** Random source for randomizing excitatory synapses. */
    private Randomizer excitatoryRandomizer = new Randomizer();

    /** Random source for randomizing all synapses. */
    private Randomizer allRandomizer = new Randomizer();

    /** Random source for perturbing. */
    private Randomizer perturber = new Randomizer();

    /** Current Mean label. */
    private JLabel meanLabel = new JLabel();

    /** Current Median label. */
    private JLabel medianLabel = new JLabel();

    /** Current Standard Deviation label. */
    private JLabel sdLabel = new JLabel();

    /** The total number of synapses. */
    private JLabel numSynsLabel = new JLabel();

    /** The number of excitatory synapses. */
    private JLabel numExSynsLabel = new JLabel();

    /** The number of inhibitory synapses. */
    private JLabel numInSynsLabel = new JLabel();

    /**
     * A combo box for selecting which kind of synapses should have their stats
     * displayed and/or what kind of display.
     */
    private JComboBox<String> synTypeSelector = new JComboBox<String>();

    /**
     * The options for the combo box, which determines which synapses are
     * displayed in the panel and filters which synapses are modified using the
     * panel. "All" and "I/E" overlay cover all the synapse, but display them
     * differently in the histogram (I/E overlay present them in different
     * colors).
     */
    {
        // TOOD: Use an enumeration or somehow remove dependencies on the text
        // in these items below.
        synTypeSelector.addItem("All");
        synTypeSelector.addItem("I/E Overlay");
        synTypeSelector.addItem("Excitatory Only");
        synTypeSelector.addItem("Inhibitory Only");
    }

    /**
     * A histogram plotting the strength of synapses over given intervals (bins)
     * against their frequency.
     */
    private final HistogramPanel histogramPanel = new HistogramPanel(
            new HistogramModel(2));

    /**
     * The histogram axis names.
     */
    {
        histogramPanel.setxAxisName("Synapse Strength");
        histogramPanel.setyAxisName("# of Synapses");
    }

    /**
     * A panel displaying basic statistics about the synapses, including: number
     * of synapses, number of inhibitory and excitatory synapses, and mean,
     * median, and standard deviation of the strengths of selected type of
     * synapses.
     */
    private JPanel statsPanel = new JPanel();

    /** A random panel for randomizing the synapse strengths. */
    private RandomizerPanel randomPanel = new RandomizerPanel();

    /** A random panel for randomizing perturbations to synapse strengths. */
    private RandomizerPanel perturberPanel = new RandomizerPanel();

    /**
     * Fills the fields of the random panels to default values.
     */
    {
        randomPanel.fillFieldValues(allRandomizer);
        perturberPanel.fillFieldValues(perturber);
        inhibitoryRandomizer.setUpperBound(0);
        excitatoryRandomizer.setLowerBound(0);
    }

    /** A button for committing random changes. */
    private JButton randomizeButton = new JButton("Apply");

    /** A button for committing perturbation changes. */
    private JButton perturbButton = new JButton("Apply");

    /**
     * A collection of the selected synaptic weights, such that the first row
     * represents excitatory weights and the 2nd row represents inhibitory
     * weights. All inhibitory weights are stored as their absolute value. Note
     * that this array is only used internally, to display stats and the
     * histogram.
     */
    private Number [][] weights = new Number[2][];

    private final List<Synapse> synapses;

    /**
     * Create a synapse adjustment panel with a specified list of synapses.
     *
     * @param networkPanel parent network panel
     * @param synapses synapses to represent in this panel
     */
    public SynapseAdjustmentPanel(final NetworkPanel networkPanel,
            final List<Synapse> synapses) {

        // Establish the parent panel.
        this.networkPanel = networkPanel;

        this.synapses = synapses;

        // Don't open if no synapses! */
        if (synapses.size() == 0) {
            return;
        }

        // Set Layout
        setLayout(new GridBagLayout());

        // Extract weight values in usable form by internal methods
        extractWeightValues(synapses);

        // Update the stats in the stats panel.
        updateStats();

        // Layout the panel.
        initializeLayout();

        // Update the histogram
        updateHistogram();

        // Add all action listeners for buttons unique to this panel.
        addActionListeners();
//
//        // Add network listener
//        networkPanel.getNetwork().addNetworkListener(networkListener);

    }

    /**
     * Initializes the layout of the panel.
     */
    private void initializeLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel synTypePanel = new JPanel();
        synTypePanel.setBorder(BorderFactory.createTitledBorder("Synapse"
                + " Stats"));
        synTypePanel.setLayout(new GridLayout(3, 2));
        synTypePanel.add(numSynsLabel);
        synTypePanel.add(meanLabel);
        synTypePanel.add(numExSynsLabel);
        synTypePanel.add(medianLabel);
        synTypePanel.add(numInSynsLabel);
        synTypePanel.add(sdLabel);

        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = HistogramPanel.GRID_WIDTH - 1;
        gbc.gridheight = 1;

        this.add(synTypePanel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = HistogramPanel.GRID_WIDTH - 1;

        this.add(synTypeSelector, gbc);

        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = HistogramPanel.GRID_WIDTH;
        gbc.gridheight = HistogramPanel.GRID_HEIGHT;
        gbc.gridy = 1;
        gbc.gridx = 0;

        this.add(histogramPanel, gbc);

        gbc.gridy += HistogramPanel.GRID_HEIGHT;
        gbc.gridheight = 1;

        JTabbedPane bottomPanel = new JTabbedPane();
        JPanel randTab = new JPanel();
        JPanel perturbTab = new JPanel();
        JPanel prunerTab = new JPanel();
        JPanel scalerTab = new JPanel();

        randTab.setLayout(new GridBagLayout());
        perturbTab.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;

        randTab.add(randomPanel, c);
        perturbTab.add(perturberPanel, c);
        scalerTab.add(new ScalerPanel(networkPanel), c);
        prunerTab.add(new PrunerPanel(networkPanel), c);

        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.insets = new Insets(5, 0, 5, 10);

        randTab.add(randomizeButton, c);
        perturbTab.add(perturbButton, c);

        bottomPanel.addTab("Randomizer", randTab);
        bottomPanel.addTab("Perturber", perturbTab);
        bottomPanel.addTab("Pruner", prunerTab);
        bottomPanel.addTab("Scaler", scalerTab);

        this.add(bottomPanel, gbc);

    }

    /**
     * Adds all the action listeners to the panel. Currently includes listeners
     * for: The perturb button, randomize button, and the synapse kind selector
     * combo box.
     */
    private void addActionListeners() {

        perturbButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                randomPanel.commitRandom(perturber);
                String type = (String) synTypeSelector.getSelectedItem();
                for (Synapse synapse : synapses) {
                    if (synapseIsAdjustable(type, synapse)) {
                        synapse.forceSetStrength(synapse.getStrength()
                                + perturber.getRandom());
                    }
                }
                fullUpdate();
                networkPanel.getNetwork().fireNetworkChanged();
            }
        });

        randomizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Commit appropriate randomizer to panel
                String polarity = (String) synTypeSelector.getSelectedItem();
                if (polarity.matches("Excitatory Only")) {
                    randomPanel.commitRandom(excitatoryRandomizer);
                } else if (polarity.matches("Inhibitory Only")) {
                    randomPanel.commitRandom(inhibitoryRandomizer);
                } else {
                    randomPanel.commitRandom(allRandomizer);
                }

                // Randomize synapses appropriately
                for (Synapse synapse : synapses) {
                    if (synapseIsAdjustable(polarity, synapse)) {
                        if (polarity.matches("Excitatory Only")) {
                            synapse.forceSetStrength(excitatoryRandomizer.getRandom());
                        } else if (polarity.matches("Inhibitory Only")) {
                            synapse.forceSetStrength(inhibitoryRandomizer.getRandom());
                        } else {
                            synapse.forceSetStrength(allRandomizer.getRandom());
                        }
                    }
                }
                fullUpdate();
                networkPanel.getNetwork().fireNetworkChanged();
            }
        });

        synTypeSelector.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Show stats and histogram only for selected type(s)...
                updateHistogram();
                updateStats();
                getParent().revalidate();
                getParent().repaint();
                if (((String) synTypeSelector.getSelectedItem()).matches("Excitatory Only")) {
                    randomPanel.fillFieldValues(excitatoryRandomizer);
                } else if (((String) synTypeSelector.getSelectedItem()).matches("Inhibitory Only")) {
                    randomPanel.fillFieldValues(inhibitoryRandomizer);
                } else {
                    randomPanel.fillFieldValues(allRandomizer);
                }
            }

        });

    }

//    /**
//     * Clean up after removing this panel.
//     */
//    public void removeListeners() {
//        networkPanel.getNetwork().removeNetworkListener(networkListener);
//    }

    // Disabled because of performance issues caused by feedback...
//    /**
//     * Updates synapse adjustment panel when network is updated.
//     */
//    private NetworkListener networkListener = new NetworkListener() {
//
//        @Override
//        public void networkChanged() {
//            extractWeightValues(synapses);
//            updateHistogram();
//            updateStats();
//            getParent().revalidate();
//            getParent().repaint();
//        }
//
//    };

    /**
     * Extracts weight values and organizes them by synapse type (inhibitory or
     * excitatory). Inhibitory values are represented by their absolute value.
     */
    private void extractWeightValues(List<Synapse> synapses) {

        int exWeights = 0;
        int inWeights = 0;

        // Inefficient but necessary due to lack of support for collections of
        // primitive types.
        for (Synapse s : synapses) {
            double w = s.getStrength();
            if (w > 0) {
                exWeights++;
            } else {
                inWeights++;
            }
        }

        weights[0] = new Number[exWeights];
        weights[1] = new Number[inWeights];
        exWeights = 0;
        inWeights = 0;

        if (weights[0].length != 0) {
            // Inefficient but necessary due to lack of support for collections of
            // primitive types.
            for (Synapse s : synapses) {
                double w = s.getStrength();
                if (w > 0) {
                    weights[0][exWeights++] = w;
                } else {
                    weights[1][inWeights++] = w;
                }
            }

        }

    }

    /**
     * Fully updates the histogram based on the status of the synapses in
     * question.
     */
    private void fullUpdate() {
        extractWeightValues(synapses);
        updateHistogram();
        updateStats();
        getParent().revalidate();
        getParent().repaint();
    }

    /**
     * Updates the histogram based on the selected synapses and selected
     * options. Can plot combined excitatory and absolute inhibitory, overlaid
     * excitatory/absolute inhibitory, only excitatory, or only inhibitory.
     * Histogram must be initialized prior to invocation. Red is used to
     * represent excitatory values, blue is used for inhibitory.
     */
    private void updateHistogram() {

        List<Number[]> data = new ArrayList<Number[]>();
        List<String> names = new ArrayList<String>();

        switch ((String) synTypeSelector.getSelectedItem()) {

            // The absolute value of all the weights are combined into a
            // single row.
            case "All": {
                // Send the histogram the excitatory and absolute inhibitory
                // synapse values as separate data series.
                Number [] hist1 = Arrays.copyOf(weights[0], weights[0].length);
                Number[] hist2 = Arrays.copyOf(weights[1], weights[1].length);
                // The names of both series
                names.add("Excitatory  ");
                names.add("Inhibitory ");
                // Use the default pallet
                SynapseAdjustmentPanel.this.histogramPanel
                .setColorPallet(HistogramPanel.DEFAULT_PALLET);
                data.add(hist1);
                data.add(hist2);
            }
            ;
            break;

            // The weights as they are stored is appropriate except that the
            // inhibitory values must be converted into non-negative values
            case "I/E Overlay": {
                // Send the histogram the excitatory and absolute inhibitory
                // synapse values as separate data series.
                Number [] hist1 = Arrays.copyOf(weights[0], weights[0].length);

                Number[] hist2 = Arrays.copyOf(weights[1], weights[1].length);
                for(int i = 0, n = hist2.length; i < n; i++) {
                    hist2[i] = Math.abs(hist2[i].doubleValue());
                }
                // The names of both series
                names.add("Excitatory  ");
                names.add("Inhibitory ");
                // Use the default pallet
                SynapseAdjustmentPanel.this.histogramPanel
                .setColorPallet(HistogramPanel.DEFAULT_PALLET);
                data.add(hist1);
                data.add(hist2);
            }
            ;
            break;

            // Data is a single row copy of first row of weights
            case "Excitatory Only": {
                // Send the histogram only excitatory weights as a single series
                Number[] hist = Arrays.copyOf(weights[0], weights[0].length);
                // Name the series
                names.add("Excitatory  ");
                names.add("Inhibitory ");
                // Use the default pallete
                SynapseAdjustmentPanel.this.histogramPanel
                .setColorPallet(HistogramPanel.DEFAULT_PALLET);
                data.add(hist);
                data.add(new Number [] { 0 });
            }
            ;
            break;

            // Data is a single row copy of second row of weights, negative
            // values are allowed here.
            case "Inhibitory Only": {
                // Send the histogram only inhibitory weights as a single series
                Number[] hist = Arrays.copyOf(weights[1], weights[1].length);

                // Name the series
                names.add("Excitatory  ");
                names.add("Inhibitory ");

                // Switch the red and blue positions of the custom pallet so the
                // histogram will plot this single series as blue (since it is
                // inhibitory) rather than the default red for the 1st series.
                Color[] pallet = Arrays.copyOf(HistogramPanel.DEFAULT_PALLET,
                        HistogramPanel.getDefaultNumDatasets());
                Color holder = pallet[0];
                pallet[0] = pallet[1];
                pallet[1] = holder;
                SynapseAdjustmentPanel.this.histogramPanel.setColorPallet(pallet);
                data.add(new Number[] { 0 });
                data.add(hist);
            }
            ;
            break;

            default: {
                throw new IllegalArgumentException("Invalid Synapse"
                        + " Selection.");
            }
        }

        // Send the histogram the new data and re-draw it.
        histogramPanel.getModel().resetData(data, names);

    }

    /**
     * Updates the values in the stats panel (number of synapses, excitatory
     * synapses, inhibitory synapses, and mean, median and standard deviation of
     * selected synapses. Extract data should be used prior to this.
     */
    private void updateStats() {

        // TODO: Error checking: ensure stats never has any length other than 3.

        // An array where the first element is the mean, the 2nd element is
        // the median, and the 3rd element is the standard deviation.
        double[] stats = getStats();

        meanLabel.setText("Mean: " + SimbrainMath.roundDouble(stats[0], 5));
        medianLabel.setText("Median " + SimbrainMath.roundDouble(stats[1], 5));
        sdLabel.setText("Std. Dev: " + SimbrainMath.roundDouble(stats[2], 5));

        int tot = weights[0].length + weights[1].length;
        numSynsLabel.setText("Synapses: " + Integer.toString(tot));
        numExSynsLabel.setText("Excitatory: "
                + Integer.toString(weights[0].length));
        numInSynsLabel.setText("Inhibitory: "
                + Integer.toString(weights[1].length));

        statsPanel.revalidate();
        statsPanel.repaint();

    }

    /**
     * Gets the basic statistics: mean, median, and standard deviation of the
     * synapse weights based on which group of synapses is selected.
     *
     * @return an An array where the first element is the mean, the 2nd element
     *         is the median, and the 3rd element is the standard deviation.
     */
    private double[] getStats() {

        double[] stats = new double[3];
        int tot = 0;
        double[] data = null;
        String type = (String) synTypeSelector.getSelectedItem();
        double runningVal = 0;

        if (weights[0].length == 0 || weights[1].length == 0) {
            return stats;
        }

        // Determine selected type(s) and collect data accordingly...
        if (type == "All") {
            tot = weights[0].length + weights[1].length;
            data = new double[tot];
            int c = 0;
            System.out.println("All happens");
            for (int i = 0; i < 2; i++) {
                for (int j = 0, m = weights[i].length; j < m; j++) {
                    double val = weights[i][j].doubleValue();
                    runningVal += val;
                    data[c] = val;
                    c++;
                }
            }
        } else if (type == "I/E Overlay") {
            tot = weights[0].length + weights[1].length;
            data = new double[tot];
            int c = 0;
            for (int i = 0; i < 2; i++) {
                for (int j = 0, m = weights[i].length; j < m; j++) {
                    double val = Math.abs(weights[i][j].doubleValue());
                    runningVal += val;
                    data[c] = val;
                    c++;
                }
            }
        }else if (type == "Excitatory Only" && weights[0].length != 0) {
            tot = weights[0].length;
            data = new double[tot];
            System.out.println("I happen");
            for (int j = 0; j < tot; j++) {
                double val = Math.abs(weights[0][j].doubleValue());
                runningVal += val;
                data[j] = val;
            }

        } else {
            if (weights[1].length != 0) {
                tot = weights[1].length;
                data = new double[tot];
                for (int j = 0; j < tot; j++) {
                    double val = weights[1][j].doubleValue();
                    runningVal += val;
                    data[j] = val;
                }
            }

        }

        if (data != null) {
            double mean = runningVal / tot;
            stats[0] = mean;

            Arrays.sort(data);
            double median = 0;
            if (tot % 2 == 0) {
                median = (data[tot / 2] + data[(tot / 2) - 1]) / 2;
            } else {
                median = data[(int) Math.floor(tot / 2)];
            }
            stats[1] = median;

            runningVal = 0;
            for (int i = 0; i < tot; i++) {
                runningVal += Math.pow((mean - data[i]), 2);
            }
            runningVal = runningVal / tot;
            double stdDev = Math.sqrt(runningVal);
            stats[2] = stdDev;
        }

        return stats;
    }

    /**
     * Panel for scaling synapses.
     */
    private class ScalerPanel extends LabelledItemPanel {

        /** Percentage to increase or decrease indicated synapses. */
        private JTextField tfIncreaseDecrease = new JTextField(".1");

        /** Button for increasing synapse strengths. */
        private JButton increaseButton = new JButton("Increase");

        /** Button for decreasing synapse strengths. */
        private JButton decreaseButton = new JButton("Decrease");

        /**
         * Construct the scaler panel.
         *
         * @param networkPanel parent network panel
         */
        public ScalerPanel(final NetworkPanel networkPanel) {
            addItem("Percent to change", tfIncreaseDecrease);
            addItem("Increase", increaseButton);
            increaseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    double amount = Double.parseDouble(tfIncreaseDecrease
                            .getText());
                    for (Synapse synapse : synapses) {
                        if (synapseIsAdjustable(
                                ((String) synTypeSelector.getSelectedItem()),
                                synapse)) {
                            synapse.forceSetStrength(synapse.getStrength()
                                    + synapse.getStrength() * amount);
                        }
                    }
                    fullUpdate();
                    networkPanel.getNetwork().fireNetworkChanged();
                }
            });

            addItem("Decrease", decreaseButton);
            decreaseButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    double amount = Double.parseDouble(tfIncreaseDecrease
                            .getText());
                    for (final Synapse synapse : synapses) {
                        if (synapseIsAdjustable(
                                ((String) synTypeSelector.getSelectedItem()),
                                synapse)) {
                            synapse.forceSetStrength(synapse.getStrength()
                                    - synapse.getStrength() * amount);
                        }
                    }
                    fullUpdate();
                    networkPanel.getNetwork().fireNetworkChanged();
                }
            });
        }
    }

    /**
     * Panel for pruning synapses.
     */
    private class PrunerPanel extends LabelledItemPanel {

        /**
         * Threshold. If synapse strength above absolute value of this value
         * prune the synapse when the prune button is pressed.
         */
        private final JTextField tfThreshold = new JTextField(".1");

        /**
         * Construct the panel.
         *
         * @param networkPanel reference to parent network panel.
         */
        public PrunerPanel(final NetworkPanel networkPanel) {
            JButton pruneButton = new JButton("Prune");
            addItem("Prune", pruneButton);
            addItem("Threshold", tfThreshold);

            pruneButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    double threshold = Double.parseDouble(tfThreshold.getText());
                    for (Synapse synapse : synapses) {
                        if (synapseIsAdjustable(
                                ((String) synTypeSelector.getSelectedItem()),
                                synapse)) {
                            if (Math.abs(synapse.getStrength()) < threshold) {
                                networkPanel.getNetwork()
                                .removeSynapse(synapse);
                            }
                        }
                    }
                    fullUpdate();
                    networkPanel.getNetwork().fireNetworkChanged();
                }
            });
        }
    }

    /**
     * Helper method to determine if a synapse can be modified given the current
     * selection of the synapse type selector.
     *
     * @param filterValue value of the synapse type selector
     * @param synapse the synapse to check
     * @return true if the synapse can be adjusted, false otherwise
     */
    private boolean synapseIsAdjustable(String filterValue,
            final Synapse synapse) {
        if (filterValue.equalsIgnoreCase("Excitatory Only")) {
            if (synapse.getStrength() < 0) {
                return false;
            }
        }
        if (filterValue.equalsIgnoreCase("Inhibitory Only")) {
            if (synapse.getStrength() > 0) {
                return false;
            }
        }
        return true;
    }

    private class SynapseWeight extends Number {

        private final Synapse s;

        public SynapseWeight(Synapse s) {
            this.s = s;
        }

        @Override
        public double doubleValue() {
            return s.getStrength();
        }

        @Override
        public float floatValue() {
            return (float) s.getStrength();
        }

        @Override
        public int intValue() {
            return (int) s.getStrength();
        }

        @Override
        public long longValue() {
            return (long) s.getStrength();
        }

    }

}
