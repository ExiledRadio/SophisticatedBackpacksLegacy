# Sophisticated Backpacks Legacy

An **unofficial** port of [Sophisticated Backpacks](https://github.com/P3pp3rF1y/SophisticatedBackpacks) to Minecraft 1.12.2 (Forge 14.23.5).

## Credits and license

Sophisticated Backpacks is by **P3pp3rF1y**. This port is based on the original's `1.16.x` branch at commit
[`c650e8a`](https://github.com/P3pp3rF1y/SophisticatedBackpacks/tree/c650e8a4248351b85537b285a0cc8b5f15a4a4f5)
(version 3.15.20, December 2022), which was released under the GNU General Public License v3.

This port is a modified version of that work and is also licensed under the **GNU GPL v3**; see [LICENSE](LICENSE).
Textures, models and translations come from the original unless noted otherwise. Files that were ported carry
a header noting the modification.

This project isn't affiliated with or supported by P3pp3rF1y. Please don't report issues with this port to
the original mod.

## Differences from the original

- Curios support is replaced by Baubles (body slot).
- The top backpack is a Nether Star backpack. The original 1.20's copper and netherite backpacks (GPL source, 1.20.x branch at bafaeb5) are listed and craftable only when another mod adds copper or netherite.
- Stonecutter, Smoking and Blasting upgrades aren't included (those blocks don't exist in 1.12.2).

See [PLAN.md](PLAN.md) for the full porting plan.

## Building

```
gradlew build
```

The jar is written to `build/libs/`. Soft-dependency jars (Baubles, JEI) go in `deps/` and are not committed.
