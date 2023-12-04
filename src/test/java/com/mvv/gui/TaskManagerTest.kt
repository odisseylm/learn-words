package com.mvv.gui

import com.mvv.gui.test.useAssertJSoftAssertions
import org.junit.jupiter.api.Test

import java.util.Collections.synchronizedList
import java.util.concurrent.atomic.AtomicInteger


class TaskManagerTest {

    @Test
    fun addTask() { useAssertJSoftAssertions {
        val v = AtomicInteger()

        TaskManager {}.use { tm ->
            tm.addTask("task 1") { v.incrementAndGet() }
            tm.addTask(task("task 2") { v.incrementAndGet() })

            tm.shutdownAndAwaitTasksCompletion()
        }

        assertThat(v.get()).isEqualTo(2)
    } }

    @Test
    fun handler() { useAssertJSoftAssertions {
        val v = AtomicInteger()
        val notifiedTasks: MutableList<String> = synchronizedList(mutableListOf())

        TaskManager(immediateRunner).use { tm ->

            val handler: (TaskEvent)->Unit = { notifiedTasks.add("${it.task.name} ${it.eventType.name} ${it.error?.message ?: ""}") }
            tm.addEventHandler(handler)

            tm.addTask("task 1") { v.incrementAndGet() }
            tm.addTask("task 2") { throw IllegalStateException("test error") }
            tm.addTask("task 3") { v.incrementAndGet() }

            tm.removeEventHandler(handler)
            tm.addTask("task 4") { v.incrementAndGet() }

            tm.shutdownAndAwaitTasksCompletion()
        }

        assertThat(v.get()).isEqualTo(3)
        assertThat(notifiedTasks).containsExactlyInAnyOrder(
            "task 1 TASK_ADDED ",
            "task 2 TASK_ADDED ",
            "task 3 TASK_ADDED ",
            "task 4 TASK_ADDED ",
            "task 1 TASK_STARTED ",
            "task 1 TASK_COMPLETED ",
            "task 1 TASK_REMOVED ",
            "task 2 TASK_STARTED ",
            "task 2 TASK_FAILED test error",
            "task 2 TASK_REMOVED ",
            "task 3 TASK_STARTED ",
            "task 3 TASK_COMPLETED ",
            "task 3 TASK_REMOVED ",
            "task 4 TASK_STARTED ",
            "task 4 TASK_COMPLETED ",
            "task 4 TASK_REMOVED ",
            )
    } }

}
