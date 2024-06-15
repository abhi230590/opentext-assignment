import java.util.UUID;
import java.util.concurrent.*;

public class Main {
    // Enumeration of task types
    public enum TaskType {
        READ,
        WRITE,
    }

    // Task Executor Interface
    public interface TaskExecutor {
        <T> Future<T> submitTask(Task<T> task);
    }

    // Representation of a Task
    public record Task<T>(
            UUID taskUUID,
            TaskGroup taskGroup,
            TaskType taskType,
            Callable<T> taskAction
    ) {
        public Task {
            if (taskUUID == null || taskGroup == null || taskType == null || taskAction == null) {
                throw new IllegalArgumentException("All parameters must not be null");
            }
        }
    }

    // Representation of a Task Group
    public record TaskGroup(
            UUID groupUUID
    ) {
        public TaskGroup {
            if (groupUUID == null) {
                throw new IllegalArgumentException("Group UUID must not be null");
            }
        }
    }

    // Implementation of the TaskExecutor interface
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Example usage of the TaskExecutor
        TaskExecutor taskExecutor = new TaskExecutorImpl(5);

        TaskGroup group1 = new TaskGroup(UUID.randomUUID());
        TaskGroup group2 = new TaskGroup(UUID.randomUUID());

        // Creating sample tasks
        Task<Integer> task1 = new Task<>(UUID.randomUUID(), group1, TaskType.READ, () -> {
            Thread.sleep(2000);
            return 1;
        });
        Task<Integer> task2 = new Task<>(UUID.randomUUID(), group1, TaskType.WRITE, () -> {
            Thread.sleep(1000);
            return 2;
        });
        Task<Integer> task3 = new Task<>(UUID.randomUUID(), group2, TaskType.READ, () -> {
            Thread.sleep(500);
            return 3;
        });
        Task<Integer> task4 = new Task<>(UUID.randomUUID(), group1, TaskType.WRITE, () -> {
            Thread.sleep(1000);
            return 4;
        });
        Task<Integer> task5 = new Task<>(UUID.randomUUID(), group2, TaskType.READ, () -> {
            Thread.sleep(500);
            return 5;
        });

        // Submitting tasks
        Future<Integer> future1 = taskExecutor.submitTask(task1);
        Future<Integer> future2 = taskExecutor.submitTask(task2);
        Future<Integer> future3 = taskExecutor.submitTask(task3);
        Future<Integer> future4 = taskExecutor.submitTask(task4);
        Future<Integer> future5 = taskExecutor.submitTask(task5);

        // Retrieving results
        System.out.println("Task 1 result: " + future1.get());
        System.out.println("Task 2 result: " + future2.get());
        System.out.println("Task 3 result: " + future3.get());
        System.out.println("Task 4 result: " + future4.get());
        System.out.println("Task 5 result: " + future5.get());

        // Shutdown the executor service
        ((TaskExecutorImpl) taskExecutor).shutdown();
    }
}
