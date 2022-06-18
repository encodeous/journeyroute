# JourneyRoute

## `What is JourneyRoute?`

JourneyRoute is a path-finding mod for Minecraft. With JourneyRoute, you will never get lost down a mineshaft! As you travel in a world, it will build a network of all the places you have been, and is able to generate routes between locations. *The more you explore, the better the network will become, thus the paths that JourneyRoute generate improve over time!*

## `Usage`

To use JourneyRoute, simply copy the jar file into the mods folder. Make sure the following requirements are also met:

### Requirements

- Fabric Mod Loader
- Minecraft 1.18+
- JourneyMap installed

## `Is JourneyRoute Unfair?`

JourneyRoute only generates routes based on blocks that you have traversed on, thus it is not able discover paths for you. *Think of JourneyRoute as an assistant that helps players remember where they've been.* Despite this, however, JourneyRoute may be considered unfair on some multiplayer servers, and thus should only be used if it is allowed by server rules.

## `Research`

JourneyRoute would not have been not possible without the work of other researchers. The following papers and algorithms have been used to implement features of JourneyMap.

### Ramer-Douglas-Peucker Polyline Simplification

Source: <https://utpjournals.press/doi/10.3138/FM57-6770-U75U-7727>

JourneyRoute uses RDP polyline simplification to reduce the number of rendered vectors in the path preview. This drastically improves frame-rates and makes the rendered lines more visually appealing. A similar process is applied more aggressively for the 2d JourneyMap path preview.

### Chaikin's Corner-Cutting Algorithm (Polyline smoothing)

Source: <https://www.cs.unc.edu/~dm/UNC/COMP258/LECTURES/Chaikins-Algorithm.pdf>

JourneyRoute uses Chaikin's algorithm to present routes in a more visually appealing manner. This is used purely for aesthetic purposes. Through the use of this algorithm, routes rendered in-game have smooth curves that are more visible compared to jagged lines. By applying multiple iterations of Chaikin's algorithm, the desired smoothness / performance ratio is reached.

### A* Heuristic Minimum Cost Path Algorithm

Source: <https://ieeexplore.ieee.org/document/4082128>

JourneyRoute uses the A* algorithm to efficiently search through blocks stored by its internal data structures. By applying dynamic weighting based on a block's visit history, paths that are visited more frequently are prioritized. This algorithm is further enhanced to promote paths that decrease in elevation, and minimizes the amount of air-blocks traveled. By applying higher weighting to paths that increase in elevation without support blocks (i.e a path that is only traversable with flight), JourneyRoute promotes the traversability of routes without the necessity of flight.
