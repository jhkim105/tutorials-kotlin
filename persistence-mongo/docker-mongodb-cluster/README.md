
## Docker mongo cluster

### Pre-requisites
1. Generate keyFile
    ```shell
    openssl rand -base64 756 > mongodb.key
    chmod 400 mongodb.key
    ```

### Setup & Run
Simply run docker compose. The cluster initiation is handled automatically.

```shell
docker compose up
```

### Verification
- Connect to Primary
    ```shell
    docker exec -it mongo1 mongosh -u root -p rootpass
    ```

- Check Status
    ```javascript
    rs.status()
    ```

### Shutdown
```shell
docker compose down -v
```

### Host Configuration (Optional)
If you need local access via specific hostnames:
`/etc/hosts`
```text
127.0.0.1       mongo1
127.0.0.1       mongo2
127.0.0.1       mongo3
```