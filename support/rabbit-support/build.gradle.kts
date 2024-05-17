val rabbitVersion: String by project

dependencies {
    implementation("com.rabbitmq:amqp-client:$rabbitVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
