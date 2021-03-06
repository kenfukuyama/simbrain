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
package org.simbrain.network.gui.actions.synapse;

import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.gui.actions.ConditionallyEnabledAction;
import org.simbrain.network.gui.dialogs.group.SynapseGroupDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Creates a synapse group connecting two neuron groups.
 */
public final class AddSynapseGroupAction extends ConditionallyEnabledAction {

    /**
     * Network panel.
     */
    private final NetworkPanel networkPanel;

    /**
     * Create a new neuron action with the specified network panel.
     *
     * @param networkPanel network panel, must not be null
     */
    public AddSynapseGroupAction(final NetworkPanel networkPanel) {
        super(networkPanel, "Connect Neuron Groups with Synapse Group...", EnablingCondition.SOURCE_AND_TARGET_NEURON_GROUPS);

        if (networkPanel == null) {
            throw new IllegalArgumentException("networkPanel must not be null");
        }

        this.networkPanel = networkPanel;
        putValue(SHORT_DESCRIPTION, "Connect source and target neuron groups with a synpase group");

    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        networkPanel.connectSelectedModels();
    }

}
