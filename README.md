# Cutscene API
A Minecraft mod for making cutscenes using existing game mechanics

This mod is for Fabric/Quilt/NeoForge 1.20.6/1.21.1.

**Requires [Fabric API](https://modrinth.com/mod/fabric-api) on Fabric.**
**Requires [Quilted Fabric API](https://modrinth.com/mod/qsl) on Quilt.**

## Features
- Cutscenes with a customizable length, path and camera rotation
- Camera path and rotation can be specified using:
  - Bezier curves
  - Catmull-Rom splines
  - lines with a lot of easing types
  - constant points
  - your own stuff that you can add with code
  - more
- Path rotation in three dimensions
- Custom transitions into and from cutscenes
- Various on-screen effects
- More coming in the future

## Documentation
Not ready yet. Until I get my hands on this, you can check out the
[releases](https://github.com/thewinnt/Cutscene-API-Forge/releases)
page for some documentation. I usually write it out pretty well.

## Building
Run `./gradlew build` in your terminal of choice (without `./` in Windows CMD).

Output files will be in `fabric/build/libs` and `neoforge/build/libs`.

On NeoForge, you will have to remove `cutscene_api.common.refmap.json` and `cutscene_api.neoforge.refmap.json`
from the built JAR file manually in order for the mod to work. You can use any archiver for this.