## AB
```shell
ab -c 4 -n 10000 "localhost:8080/messages/publish"
```

```shell
ab -c 4 -n 10000 "localhost:8080/messages/publish_with_key"
```