package com.mvv.gui

import com.mvv.gui.javafx.setToolTip
import com.mvv.gui.util.ifNullOrBlank
import javafx.application.Platform
import javafx.event.Event
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.event.EventType
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.*
import javafx.scene.paint.Color
import java.time.Duration
import java.util.Collections.synchronizedList
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


private val log = mu.KotlinLogging.logger {}


interface Task {
    val name: String
    val details: String?
    val task: ()->Unit
}

private class TaskImpl(
    override val name: String,
    override val details: String?,
    override val task: ()->Unit,
    ) : Task

fun task(name: String, task: () -> Unit): Task =
    TaskImpl(name, name, task)

open class TaskEvent
    /**
     * Construct a new `ActionEvent` with the specified event source and target.
     * If the source or target is set to `null`, it is replaced by the
     * `NULL_SOURCE_TARGET` value. All ActionEvents have their type set to
     * `ACTION`.
     *
     * @param source    the event source which sent the event
     * @param target    the event target to associate with the event
     */(source: Any, target: EventTarget?, eventType: EventType<TaskEvent>, val task: Task, val tasks: List<Task>, val error: Throwable? = null) : Event(source, target, eventType) {

    override fun copyFor(newSource: Any, newTarget: EventTarget): TaskEvent =
        super.copyFor(newSource, newTarget) as TaskEvent

    @Suppress("UNCHECKED_CAST")
    override fun getEventType(): EventType<out TaskEvent> =
        super.getEventType() as EventType<out TaskEvent>

    companion object {

        val TASK_ADDED     = EventType<TaskEvent>(ANY, "TASK_ADDED")
        val TASK_STARTED   = EventType<TaskEvent>(ANY, "TASK_STARTED")
        val TASK_COMPLETED = EventType<TaskEvent>(ANY, "TASK_COMPLETED")
        val TASK_FAILED    = EventType<TaskEvent>(ANY, "TASK_FAILED")
        val TASK_REMOVED   = EventType<TaskEvent>(ANY, "TASK_REMOVED")
    }
}


typealias TaskEventHandler = EventHandler<TaskEvent>

val javaFxRunner: (()->Unit)->Unit = { Platform.runLater(it) }
val immediateRunner: (()->Unit)->Unit = { it() }

class TaskManager (
    val fireEventRunner: (()->Unit)->Unit,

    ) : AutoCloseable {
    private val tasks: MutableList<Task> = synchronizedList(mutableListOf())
    private val internalExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private val listeners = CopyOnWriteArrayList<TaskEventHandler>()

    override fun close() { internalExecutor.shutdown() }
    fun shutdownAndAwaitTasksCompletion(timeout: Duration = Duration.ofSeconds(30)) {
        internalExecutor.shutdown()
        internalExecutor.awaitTermination(timeout.seconds, TimeUnit.SECONDS)
    }

    fun addEventHandler(h: TaskEventHandler) { listeners.add(h) }
    fun removeEventHandler(h: TaskEventHandler) { listeners.remove(h) }
    private fun fireEvent(event: TaskEvent) =
        fireEventRunner { listeners.forEach { it.handle(event) } }
    private fun fireEvent(eventType: EventType<TaskEvent>, task: Task, error: Throwable? = null) =
        fireEvent(TaskEvent(this, null, eventType, task, tasksCopy(), error))

    private fun tasksCopy(): List<Task> = this.tasks.toTypedArray().toList() // toArray() is thread-safe

    fun addTask(task: Task) = addTask(task, internalExecutor)

    fun addTask(task: Task, executor: ExecutorService) {
        tasks.add(task)
        fireEvent(TaskEvent.TASK_ADDED, task)

        executor.submit {
            taskStarted(task)
            try {
                task.task()
                taskCompleted(task)
            }
            catch (ex: Throwable) {
                log.error(ex) { "Task [${task.name}] is failed." }
                taskCompleted(task, ex)
            }
        }
    }

    private fun taskStarted(task: Task) = fireEvent(TaskEvent.TASK_STARTED, task)

    private fun taskCompleted(task: Task, error: Throwable? = null) {
        val eventType = if (error == null) TaskEvent.TASK_COMPLETED else TaskEvent.TASK_FAILED
        fireEvent(eventType, task, error)

        tasks.remove(task)
        fireEvent(TaskEvent.TASK_REMOVED, task)
    }
}


fun TaskManager.addTask(name: String, executor: ExecutorService, task: ()->Unit) =
    this.addTask(task(name, task), executor)
fun TaskManager.addTask(name: String, task: ()->Unit) =
    this.addTask(task(name, task))


fun TaskManager.createProgressBar(): Region {

    val progressBar = ProgressBar(ProgressIndicator.INDETERMINATE_PROGRESS)
    progressBar.styleClass.add("TaskManagerProgressBar")

    val label = Label()
    label.styleClass.add("TaskManagerProgressLabel")
    label.background = Background(BackgroundFill(Color.TRANSPARENT, null, null))

    val stackPane = StackPane(progressBar, label).also {
        it.styleClass.add("TaskManagerProgressContainer")
        it.maxWidth = 500.0 // could be put to CSS

        progressBar.maxWidth = 1000.0
        progressBar.maxHeight = 50.0
    }

    this.addEventHandler { ev ->
        val tasks = ev.tasks

        val noTasks = tasks.isEmpty()
        val toolTip = tasks.joinToString("\n") { task -> task.details.ifNullOrBlank { task.name } }
        val labelText = if (noTasks) "" else "${tasks.size} task(s): " + tasks.joinToString("; ") { it.name }

        Platform.runLater {
            progressBar.isVisible = !noTasks
            label.text = labelText
            label.setToolTip(toolTip, null, false)
        }
    }

    return stackPane
}
