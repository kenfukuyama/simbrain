/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/Documentation/docs/SimbrainDocs.html#Credits
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
package org.simnet.interfaces;

import java.util.ArrayList;

/**
 * <b>Group</b> a group of neurons, synapses, and networks which are separately contained in the
 * main network hierarchy but to which additinal rules should be applied.  Group elements are references
 * Extending this class and overriding the update function gives it functoinality.
 *
 * This extends Network not because these should behave like networks but mainly to take advantage of utility
 * methods associated with the Network class.
 */
public abstract class Group {

    /** Network delegate which serves here as a utilty class to keep track of references to network objects. */
    protected RootNetwork referenceNetwork = new RootNetwork();

    /** Reference to the network this group is a part of. */
    private RootNetwork parent;

    /** Whether this Group should be active or not. */
    private boolean isOn = true;

    /**
     * Construct a model group with a reference to its root network.
     *
     * @param net reference to root network.
     */
    public Group(final RootNetwork net) {
        parent = net;
    }

    /**
     * True if the group contains the specified neuron.
     *
     * @param n neuron to check for.
     * @return true if the group contains this neuron, false otherwise
     */
    public boolean containsNeuron(final Neuron n) {
        return this.getFlatNeuronList().contains(n);
    }

    /**
     * Turn the group on or off.  When off, the group update function
     * should not be called.
     */
    public void toggleOnOff() {
        if (isOn) {
            isOn = false;
        } else {
            isOn = true;
        }
    }

    /**
     * @return the isOn
     */
    public boolean isOn() {
        return isOn;
    }

    /**
     * True if this group has no neurons, weights, or networks.
     * @return
     */
    public boolean isEmpty() {
        boolean neuronsGone = referenceNetwork.getNeuronList().isEmpty();
        boolean weightsGone = referenceNetwork.getWeightList().isEmpty();
        boolean networksGone = referenceNetwork.getNetworkList().isEmpty();
        return (neuronsGone && weightsGone && networksGone);
    }
    /**
     * @see Object
     */
    public String toString() {
        String ret =  new String();
        ret += ("Group with " + this.getNeuronList().size() + " neuron(s),");
        ret += (" " + this.getWeightList().size() + " synapse(s),");
        ret += ("and " + this.getNetworkList().size() + " network(s).");
        return ret;
    }

    /**
     * Adds a list of network elements to this network.
     *
     * @param toAdd list of objects to add.
     */
    public void addObjectReferences(final ArrayList<Object> toAdd) {

        // To avoid adding networks as well as their children
        //  No doubt there is a better way to do this!
        ArrayList<Object> possibleOverlaps = new ArrayList<Object>();

        // Add the networks
        for (final Object object : toAdd) {
            if (object instanceof Network) {
                final Network net = (Network) object;
                referenceNetwork.getNetworkList().add(net);
                possibleOverlaps.addAll(net.getFlatNeuronList());
                possibleOverlaps.addAll(net.getFlatSynapseList());
            }
        }

        // Add those children not contained in any network
        for (final Object object : toAdd) {
            if (object instanceof Neuron) {
                if (!possibleOverlaps.contains(object)) {
                    final Neuron neuron = (Neuron) object;
                    referenceNetwork.getNeuronList().add(neuron);
                }
            } else if (object instanceof Synapse) {
                if (!possibleOverlaps.contains(object)) {
                    final Synapse synapse = (Synapse) object;
                    referenceNetwork.getWeightList().add(synapse);
                }
            }
        }
    }
    /**
     * @param toDelete
     * @see org.simnet.interfaces.Network#deleteNetwork(org.simnet.interfaces.Network)
     */
    public void deleteNetwork(Network toDelete) {
        // Just remove the reference; don't do all the other bookkeeping
        //  The main network will handle that
        referenceNetwork.getNetworkList().remove(toDelete);
        parent.fireGroupChanged(this, this);
    }

    /**
     * @param toDelete
     * @see org.simnet.interfaces.Network#deleteNeuron(org.simnet.interfaces.Neuron)
     */
    public void deleteNeuron(Neuron toDelete) {
        // Just remove the reference; don't do all the other bookkeeping
        //  The main network will handle that
        referenceNetwork.getNeuronList().remove(toDelete);
        parent.fireGroupChanged(this, this);
    }

    /**
     * @param toDelete
     * @see org.simnet.interfaces.Network#deleteWeight(org.simnet.interfaces.Synapse)
     */
    public void deleteWeight(Synapse toDelete) {
        // Just remove the reference; don't do all the other bookkeeping
        //  The main network will handle that        
        referenceNetwork.getWeightList().remove(toDelete);
        parent.fireGroupChanged(this, this);
    }

    /**
     * @return
     * @see org.simnet.interfaces.Network#getFlatNetworkList()
     */
    public ArrayList getFlatNetworkList() {
        return referenceNetwork.getFlatNetworkList();
    }

    /**
     * @return
     * @see org.simnet.interfaces.Network#getFlatNeuronList()
     */
    public ArrayList<Neuron> getFlatNeuronList() {
        return referenceNetwork.getFlatNeuronList();
    }

    /**
     * @return
     * @see org.simnet.interfaces.Network#getFlatSynapseList()
     */
    public ArrayList<Synapse> getFlatSynapseList() {
        return referenceNetwork.getFlatSynapseList();
    }

    /**
     * @return a list of networks
     * @see org.simnet.interfaces.Network#getNetworkList()
     */
    public ArrayList<Network> getNetworkList() {
        return referenceNetwork.getNetworkList();
    }

    /**
     * @return a list of neurons
     * @see org.simnet.interfaces.Network#getNeuronList()
     */
    public ArrayList<Neuron> getNeuronList() {
        return referenceNetwork.getNeuronList();
    }

    /**
     * @return a list of weights
     * @see org.simnet.interfaces.Network#getWeightList()
     */
    public ArrayList<Synapse> getWeightList() {
        return referenceNetwork.getWeightList();
    }

    /**
     * @see org.simnet.interfaces.RootNetwork#update()
     */
    public void update() {
        referenceNetwork.update();
    }

    /**
     * @see org.simnet.interfaces.Network#updateAllNetworks()
     */
    public void updateAllNetworks() {
        referenceNetwork.updateAllNetworks();
    }

    /**
     * @see org.simnet.interfaces.Network#updateAllNeurons()
     */
    public void updateAllNeurons() {
        referenceNetwork.updateAllNeurons();
    }

    /**
     * @see org.simnet.interfaces.Network#updateAllWeights()
     */
    public void updateAllWeights() {
        referenceNetwork.updateAllWeights();
    }

    /**
     * @see org.simnet.interfaces.RootNetwork#updateRootNetwork()
     */
    public void updateRootNetwork() {
        referenceNetwork.updateRootNetwork();
    }

    /**
     * @return the parent
     */
    public RootNetwork getParent() {
        return parent;
    }
}