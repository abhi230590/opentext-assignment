import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskExecutorImpl implements Main.TaskExecutor {
    // Maximum number of concurrent tasks allowed
    private final int maxConcurrency;
    // Executor service for handling task execution
    private final ExecutorService executorService;
    // Queue to preserve task order
    private final BlockingQueue<Runnable> taskQueue;
    // Map to hold locks for each TaskGroup
    private final ConcurrentHashMap<UUID, Lock> groupLocks;

    // Constructor
    public TaskExecutorImpl(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        this.taskQueue = new LinkedBlockingQueue<>();
        this.executorService = new ThreadPoolExecutor(
                maxConcurrency,
                maxConcurrency,
                0L,
                TimeUnit.MILLISECONDS,
                taskQueue
        );
        this.groupLocks = new ConcurrentHashMap<>();
    }

    // Submit a new task to be executed
    @Override
    public <T> Future<T> submitTask(Main.Task<T> task) {
        // Retrieve or create a lock for the task's group
        Lock groupLock = groupLocks.computeIfAbsent(task.taskGroup().groupUUID(), k -> new ReentrantLock());
        // Create a FutureTask to execute the task with the group lock handling
        Callable<T> taskWithLock = () -> {
            groupLock.lock();
            try {
                return task.taskAction().call();
            } finally {
                groupLock.unlock();
            }
        };

        // Submit the wrapped task to the executor service
        FutureTask<T> futureTask = new FutureTask<>(taskWithLock);
        executorService.submit(futureTask);
        return futureTask;
    }

    // Shutdown the executor service gracefully
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
