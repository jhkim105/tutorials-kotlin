

## AB
```shell
ab -c 30 -n 300 "localhost:8080/users/blocking?delay=1000"
```
```shell
ab -c 30 -n 300 "localhost:8080/users/nonblocking?delay=1000"
```