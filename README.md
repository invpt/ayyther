# ayyther
A floating islands world generator plugin for Bukkit/Spigot/Paper 1.15.2.
It is slower than default generation, since it must sample noise for many blocks in each chunk. There is no way to greatly improve performance other than completely changing how islands are generated. (Maybe using 3D Voronoi?)
However, the TPS never dropped below 20 while I was testing it -- chunks just don't generate as fast.

# Features
- Ore generation
- Default Minecraft decorators
- Some biomes
- Generates debris on islands that are covered by other islands

# Features that may someday be added
- Biome generation using Voronoi tessellation, which would allow a much greater variety of biomes
- Larger bodies of water
- Structures
