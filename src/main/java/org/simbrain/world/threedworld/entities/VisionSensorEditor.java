package org.simbrain.world.threedworld.entities;

import org.simbrain.world.threedworld.ThreeDImagePanel;

import javax.swing.*;
import java.awt.*;

/**
 * VisionSensorEditor provides a GUI panel for editing the properties of a VisionSensor.
 *
 * @author Tim Shea
 */
public class VisionSensorEditor extends SensorEditor {
    private VisionSensor sensor;
    private JFormattedTextField widthField = new JFormattedTextField(EditorDialog.integerFormat);
    private JFormattedTextField heightField = new JFormattedTextField(EditorDialog.integerFormat);
    private JFormattedTextField headOffsetXField = new JFormattedTextField(EditorDialog.floatFormat);
    private JFormattedTextField headOffsetYField = new JFormattedTextField(EditorDialog.floatFormat);
    private JFormattedTextField headOffsetZField = new JFormattedTextField(EditorDialog.floatFormat);
    private JComboBox<VisionSensor.FilterType> filterComboBox = new JComboBox<>();
    private ThreeDImagePanel previewPanel = new ThreeDImagePanel();

    {
        widthField.setColumns(5);
        heightField.setColumns(5);
        headOffsetXField.setColumns(6);
        headOffsetYField.setColumns(6);
        headOffsetZField.setColumns(6);
        filterComboBox.addItem(VisionSensor.FilterType.COLOR);
        filterComboBox.addItem(VisionSensor.FilterType.GRAY);
        filterComboBox.addItem(VisionSensor.FilterType.THRESHOLD);
        previewPanel.setPreferredSize(new Dimension(100, 100));
    }

    /**
     * Construct a new VisionSensorEditor.
     *
     * @param sensor The sensor to edit.
     */
    public VisionSensorEditor(VisionSensor sensor) {
        super(sensor.getAgent(), sensor);
        this.sensor = sensor;
        previewPanel.setImageSource(sensor.getSource());
    }

    @Override
    public JComponent layoutFields() {
        JComponent sensorComponent = super.layoutFields();

        getPanel().add(new JLabel("Resolution"));
        getPanel().add(widthField, "split 2");
        getPanel().add(heightField, "wrap");

        getPanel().add(new JLabel("Head Offset"));
        getPanel().add(headOffsetXField, "split 3");
        getPanel().add(headOffsetYField);
        getPanel().add(headOffsetZField, "wrap");

        getPanel().add(new JLabel("Mode"));
        getPanel().add(filterComboBox, "wrap");

        getPanel().add(previewPanel, "east, gaptop 4px, gapright 4px, gapbottom 4px, gapleft 4px");

        return sensorComponent;
    }

    @Override
    public void readValues() {
        widthField.setValue(sensor.getWidth());
        heightField.setValue(sensor.getHeight());
        headOffsetXField.setValue(sensor.getHeadOffset().x);
        headOffsetYField.setValue(sensor.getHeadOffset().y);
        headOffsetZField.setValue(sensor.getHeadOffset().z);
        filterComboBox.setSelectedItem(sensor.getFilterType());
    }

    @Override
    public void writeValues() {
        sensor.resize(((Number) widthField.getValue()).intValue(), ((Number) heightField.getValue()).intValue());
        sensor.getHeadOffset().x = ((Number) headOffsetXField.getValue()).floatValue();
        sensor.getHeadOffset().y = ((Number) headOffsetYField.getValue()).floatValue();
        sensor.getHeadOffset().z = ((Number) headOffsetZField.getValue()).floatValue();
        sensor.setFilterType((VisionSensor.FilterType) filterComboBox.getSelectedItem());
    }

    @Override
    public void close() {
        previewPanel.destroy();
    }
}