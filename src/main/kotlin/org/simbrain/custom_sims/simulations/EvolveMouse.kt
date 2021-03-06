package org.simbrain.custom_sims.simulations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.simbrain.custom_sims.addNetworkComponent
import org.simbrain.custom_sims.addOdorWorldComponent
import org.simbrain.custom_sims.couplingManager
import org.simbrain.custom_sims.newSim
import org.simbrain.network.core.Synapse
import org.simbrain.network.neuron_update_rules.LinearRule
import org.simbrain.network.neuron_update_rules.interfaces.BiasedUpdateRule
import org.simbrain.network.util.activations
import org.simbrain.util.format
import org.simbrain.util.geneticalgorithms.*
import org.simbrain.util.point
import org.simbrain.util.widgets.ProgressWindow
import org.simbrain.workspace.Workspace
import org.simbrain.world.odorworld.entities.EntityType
import kotlin.math.abs

val evolveMouse = newSim {

    val mainScope = MainScope()

    fun createEvolution(): Evaluator {
        val environmentBuilder = environmentBuilder(1) {

            val inputs = chromosome(3) {
                nodeGene()
            }

            val hiddens = chromosome(8) {
                nodeGene()
            }

            val outputs = chromosome(3) {
                nodeGene {
                    updateRule.let {
                        if (it is LinearRule) {
                            it.lowerBound = 0.0
                        }
                    }
                }
            }

            val connections = chromosome<Synapse, ConnectionGene>()

            val evolutionWorkspace = Workspace()

            val networkComponent = evolutionWorkspace { addNetworkComponent("Network") }
            val network = networkComponent.network

            val odorworldComponent = evolutionWorkspace { addOdorWorldComponent("Odor World") }
            val odorworld = odorworldComponent.world

            val sensors = chromosome(3) {
                objectSensorGene {
                    setObjectType(EntityType.SWISS)
                    theta = it * 2 * Math.PI / 3
                    radius = 32.0
                    decayFunction.dispersion = 200.0
                }
            }

            val straightMovement = chromosome(
                straightMovementGene()
            )

            val turning = chromosome(
                turningGene { direction = -1.0 },
                turningGene { direction = 1.0 }
            )

            val mouse = odorworld.addEntity(EntityType.MOUSE).apply {
                setCenterLocation(50.0, 200.0)
            }

            fun createCheese() = odorworld.addEntity(EntityType.SWISS).apply {
                setCenterLocation(
                    random.nextDouble(100.0, 300.0),
                    random.nextDouble(0.0, 300.0)
                )
            }

            val cheeses = List(3) { createCheese() }

            onBuild { pretty ->
                network {
                    if (pretty) {
                        +inputs.asGroup {
                            label = "Input"
                            location = point(0, 100)
                        }
                        +hiddens
                        +outputs.asGroup {
                            label = "Output"
                            location = point(0, -100)
                        }
                    } else {
                        +inputs
                        +hiddens
                        +outputs
                    }
                    +connections
                }
                mouse {
                    +sensors
                    +straightMovement
                    +turning
                }
                evolutionWorkspace {
                    couplingManager.apply {
                        val (straightNeuron, leftNeuron, rightNeuron) = outputs.products
                        val (straightConsumer) = straightMovement.products
                        val (left, right) = turning.products

                        sensors.products couple inputs.products
                        straightNeuron couple straightConsumer
                        leftNeuron couple left
                        rightNeuron couple right
                    }
                }

                cheeses.forEach { cheese ->
                    cheese.onCollide {
                        cheese.setCenterLocation(
                            random.nextDouble(100.0, 300.0),
                            random.nextDouble(0.0, 300.0)
                        )
                    }
                }
            }

            onMutate {
                hiddens.forEach {
                    it.mutate {
                        updateRule.let {
                            if (it is BiasedUpdateRule) it.bias += random.nextDouble(-0.2, 0.2)
                        }
                    }
                }
                connections.forEach {
                    it.mutate {
                        strength += random.nextDouble(-0.2, 0.2)
                    }
                }
                val source = (inputs.genes + hiddens.genes).let {
                    val index = random.nextInt(0, it.size)
                    it[index]
                }
                val target = (outputs.genes + hiddens.genes).let {
                    val index = random.nextInt(0, it.size)
                    it[index]
                }
                connections.genes.add(connectionGene(source, target) {
                    strength = random.nextDouble(-0.2, 0.2)
                })
            }

            onEval {
                var score = 0.0

                // cheeses.forEach {
                //     it.onCollide { other ->
                //         if (other === mouse) {
                //             score += 1
                //         }
                //     }
                // }

                evolutionWorkspace.apply {
                    repeat(100) {
                        simpleIterate()
                        val energy = abs(outputs.products.activations.sum()) + 5
                        score += energy / 1000
                    }
                }

                // val partial = cheeses.map { cheese -> 100 - mouse.getRadiusTo(cheese) }
                //     .maxOf { it }
                //     .let { if (it < 0) 0.0 else it } / 100

                score
            }

            onPeek {
                workspace.openFromZipData(evolutionWorkspace.zipData)
            }

        }

        return evaluator(environmentBuilder) {
            populationSize = 100
            eliminationRatio = 0.5
            runUntil { generation == 50 || fitness > 250 }
        }
    }

    mainScope.launch {

        workspace.clearWorkspace()

        val progressWindow = ProgressWindow(200)

        launch(Dispatchers.Default) {

            val generations = createEvolution().start().onEachGenerationBest { generation ->
                progressWindow.progressBar.value = generation
                progressWindow.fitnessScore.text = "Fitness: ${fitness.format(2)}"
            }
            val (best, fitness) = generations.best

            println("Winning fitness $fitness after generation ${generations.finalGenerationNumber}")

            best.prettyBuild().peek()

            progressWindow.close()
        }

    }

}