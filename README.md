# Nature's Compass

Nature's Compass is a Minecraft mod that allows you to search for a biome's location anywhere in the world and view information about it.

## Download

Downloads, installation instructions, and more information can be found on [Curse](https://minecraft.curseforge.com/projects/natures-compass).

## Develop

### Setup

Fork this repository, then clone via SSH:
```
git clone git@github.com:<you>/NaturesCompass.git
```

Or, clone via HTTPS:
```
git clone https://github.com/<you>/NaturesCompass.git
```

2. In the root of the repository, run:
```
gradlew eclipse
```

Or, if you plan to use IntelliJ, run:
```
gradlew idea
```

3. Run:
```
gradlew genEclipseRuns
```

Or, to use IntelliJ, run:
```
gradlew genIntellijRuns
```

4. Open the project's parent directory in your IDE and import the project as an existing Gradle project.

### Build

To build the project, configure `build.gradle` then run:
```
gradlew build
```

This will build a jar file in `build/libs`.

## License

This mod is available under the [Creative Commons Attribution-NonCommercial ShareAlike 4.0 International License](https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode).
