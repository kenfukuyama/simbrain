package org.simbrain.world.imageworld;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.simbrain.workspace.AttributeContainer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.imageworld.serialization.BufferedImageConverter;
import org.simbrain.world.imageworld.serialization.CouplingArrayConverter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageAlbumComponent extends WorkspaceComponent {

    /**
     * The image world this component displays.
     */
    private ImageAlbumWorld world;

    /**
     * Open a saved ImageWorldComponent from an XML input stream.
     *
     * @param input  The input stream to read.
     * @param name   The name of the new world component.
     * @param format The format of the input stream. Should be xml.
     * @return A deserialized ImageWorldComponent.
     */
    public static ImageAlbumComponent open(InputStream input, String name, String format) {
        ImageAlbumWorld world = (ImageAlbumWorld) getXStream().fromXML(input);
        return new ImageAlbumComponent(name, world);
    }

    /**
     * Default constructor
     */
    public ImageAlbumComponent() {
        super("");
        this.world = new ImageAlbumWorld();
    }


    /**
     * Create named component.
     */
    public ImageAlbumComponent(String name) {
        super(name);
        this.world = new ImageAlbumWorld();
    }

    @Override
    public List<AttributeContainer> getAttributeContainers() {
        List<AttributeContainer> containers = new ArrayList<>();
        containers.add(world.getCurrentSensorMatrix());
        return containers;
    }

    @Override
    public AttributeContainer getObjectFromKey(String objectKey) {
        // for (ImageSource source : getWorld().getImageSources()) {
        //     if (objectKey.equals(source.getClass().getSimpleName())) {
        //         return source;
        //     }
        // }
        // for (SensorMatrix sensor : getWorld().getSensorMatrices()) {
        //     if (objectKey.equals(sensor.getName())) {
        //         return sensor;
        //     }
        // }
        return null;
    }

    /**
     * Create an xstream from this class.
     */
    public static XStream getXStream() {
        XStream stream = new XStream(new DomDriver());
        stream.registerConverter(new BufferedImageConverter());
        stream.registerConverter(new CouplingArrayConverter());
        return stream;
    }

    @Override
    public void save(OutputStream output, String format) {
        getXStream().toXML(getWorld(), output);
    }

    @Override
    protected void closing() {
    }

    /**
     * Deserialize an ImageAlbumComponent.
     *
     * @param name name of component
     * @param world the deserialized world
     */
    public ImageAlbumComponent(String name, ImageAlbumWorld world) {
        super(name);
        this.world = world;
    }

    public ImageAlbumWorld getWorld() {
        return world;
    }
}
