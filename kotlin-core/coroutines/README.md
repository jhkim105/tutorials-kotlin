


```properties
-Dkotlinx.coroutines.debug
```
- 설정 전
    ```text
    14:52:43.117 [DefaultDispatcher-worker-1] INFO coroutines -- [parent] Hello. everyone
    14:52:43.119 [DefaultDispatcher-worker-2] INFO coroutines -- [child1] Hi there!
    14:52:43.119 [DefaultDispatcher-worker-2] INFO coroutines -- [child2] Hi there!
    14:52:43.319 [kotlinx.coroutines.DefaultExecutor] INFO coroutines -- [parent] Hi again
    ```
- 설정 후
    ```text
    14:51:47.964 [DefaultDispatcher-worker-1 @Greeting Coroutine#1] INFO coroutines -- [parent] Hello. everyone
    14:51:47.966 [DefaultDispatcher-worker-2 @Greeting Coroutine#2] INFO coroutines -- [child1] Hi there!
    14:51:47.967 [DefaultDispatcher-worker-3 @Child Greeting Coroutine#3] INFO coroutines -- [child2] Hi there!
    14:51:48.162 [kotlinx.coroutines.DefaultExecutor] INFO coroutines -- [parent] Hi again
     ```
  
