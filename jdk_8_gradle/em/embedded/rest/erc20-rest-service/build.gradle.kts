
repositories {
    mavenLocal()
    mavenCentral()
    maven( url ="https://jcenter.bintray.com")
}


plugins {
    `java-library`
}


configurations.named("implementation") {
    resolutionStrategy {
        failOnVersionConflict()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val EVOMASTER_VERSION = project.ext.get("EVOMASTER_VERSION")

dependencies{
    implementation("org.evomaster:evomaster-client-java-controller:$EVOMASTER_VERSION")
    implementation("org.evomaster:evomaster-client-java-dependencies:$EVOMASTER_VERSION")
    implementation(project(":cs:rest:erc20-rest-service"))
}