dependencies {
    api(project(":qwe-core"))
    api(VertxLibs.web)
    api(WebLibs.jaxrs)
    compileOnly(project(":micro"))
    compileOnly(VertxLibs.serviceDiscovery)

    testImplementation(VertxLibs.junit)
    testImplementation(testFixtures(project(":qwe-core")))
    testImplementation(project(":micro"))
    testImplementation(project(":http:client"))
}
