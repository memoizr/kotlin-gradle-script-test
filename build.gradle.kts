import org.gradle.api.Task
import org.gradle.script.lang.kotlin.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

val kotlinVersion = "1.0.3"

class newTask(private val configuration: org.gradle.api.Task.() -> kotlin.Unit) : ReadOnlyProperty<KotlinBuildScript, Task> {
    private var task: Task? = null

    override fun getValue(thisRef: KotlinBuildScript, property: KProperty<*>): Task {
        return task ?: getTask(thisRef, property)
    }

    private fun getTask(thisRef: KotlinBuildScript, property: KProperty<*>): Task {
        val task = thisRef.task(property.name).doLast(configuration)
        this.task = task
        return task
    }
}

buildscript {
    repositories {
        jcenter()
        gradleScriptKotlin()
    }
    dependencies {
        classpath(kotlinModule("gradle-plugin"))
    }
}


repositories {
    jcenter()
    gradleScriptKotlin()
}

apply {
    plugin("kotlin")
}

dependencies {
    compile("org.codehaus.groovy:groovy-all:2.4.7")
    compile("org.slf4j:slf4j-api:1.7.10")
    compile("javax.inject:javax.inject:1")

    compile(kotlin("stdlib"))
    compile(kotlin("reflect"))
    compile(kotlin("compiler-embeddable"))

    testCompile(gradleTestKit())
    testCompile("junit:junit:4.12")
    testCompile("com.nhaarman:mockito-kotlin:0.5.2")
}

val noway by newTask {
    println("This is noway task!")
}

val amazingTask by newTask {
    println("this is amazing tasks!")
}

amazingTask.doFirst {
    println("last action")
    burrito.actions.forEach { it.execute(burrito) }
    println("boo is ${extra["boo"]}")
}

amazingTask.doLast {
    println("amazing task last action!!")
}

val burrito = task("burrito").doLast {
    println("I'm a burrito lol!")
    extra["boo"] = "Trololololloooo"
}

burrito.doLast {
    println("after the burrito, the hangover")
}

defaultTasks(Build_gradle::burrito, Build_gradle::amazingTask)

task("nope") {
    doLast {
        println("hello world")
    }
}

fun kotlin(module: String) = "org.jetbrains.kotlin:kotlin-$module:$kotlinVersion"

infix fun Task.doLast(task: org.gradle.api.Task.() -> kotlin.Unit) {
    this.doLast(task)
}

fun defaultTasks(vararg property: KProperty<Task>) {
    defaultTasks(*property.map { it.name }.toTypedArray())
}

javaClass.declaredMethods.forEach {
    if (it.returnType == Task::class.java && it.name.startsWith("get")) {
        it.invoke(this)
    }
}
