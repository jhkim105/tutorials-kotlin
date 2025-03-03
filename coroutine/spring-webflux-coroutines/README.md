

```shell
ab -c 100 -n 10000 "localhost:8080/blocking?delay=1000"
```

```shell
ab -c 100 -n 10000 "localhost:8080/coroutine?delay=1000"
```