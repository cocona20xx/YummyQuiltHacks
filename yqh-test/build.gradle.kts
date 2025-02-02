plugins {
	java
	alias(libs.plugins.quilt.loom)
	`maven-publish`
}

val modVersion: String by project
val mavenGroup: String by project
val modId: String by project

base.archivesBaseName = modId
version = modVersion
group = mavenGroup

repositories {
	mavenLocal()
	mavenCentral()
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
	maven {
		name = "TerraformersMC"
		url = uri("https://maven.terraformersmc.com/")
	}

	maven {
		name = "Modrinth"
		url = uri("https://api.modrinth.com/maven")
		content {
			includeGroup("maven.modrinth")
		}
	}

	maven {
		name = "auoeke Maven"
		url = uri("https://maven.auoeke.net")
	}

	maven {
		name = "ENDERZOMBI102 Maven"
		url = uri("https://repsy.io/mvn/enderzombi102/mc")
	}
}


val modImplementationInclude by configurations.register("modImplementationInclude")

// All the dependencies are declared at gradle/libs.version.toml and referenced with "libs.<id>"
// See https://docs.gradle.org/current/userguide/platforms.html for information on how version catalogs work.
dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        mappings("org.quiltmc:quilt-mappings:${libs.versions.quilt.mappings.get()}:intermediary-v2")
        // officialMojangMappings() // Uncomment if you want to use Mojang mappings as your primary mappings, falling back on QM for parameters and Javadocs
    })
    modImplementation(libs.quilt.loader)

	modImplementation(files("$projectDir/build/libs/yqh-0.1.2.jar"))

	annotationProcessor("net.auoeke:uncheck:latest.release")

	add(sourceSets.main.get().getTaskName("mod", JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME), modImplementationInclude)
	add(net.fabricmc.loom.util.Constants.Configurations.INCLUDE, modImplementationInclude)
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("quilt.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	// Minecraft 1.18 (1.18-pre2) upwards uses Java 17.
	options.release.set(17)
}

java {
	// Still required by IDEs such as Eclipse and Visual Studio Code
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17

	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	// If this mod is going to be a library, then it should also generate Javadocs in order to aid with developement.
	// Uncomment this line to generate them.
	// withJavadocJar()
}

// If you plan to use a different file for the license, don't forget to change the file name here!
tasks.withType<AbstractArchiveTask> {
	from("LICENSE") {
		rename { "${it}_${modId}" }
	}
}

// Configure the maven publication
publishing {
	publications {}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
