

## Docker mongo cluster

### keyFile 생성
```shell
openssl rand -base64 756 > mongodb.key
chmod 400 mongodb.key
```

### 클러스터 구성 script

```shell
#!/bin/sh

echo "Initiating Replica Set..."
mongosh --host mongo1:27017 -u root -p rootpass <<EOF
rs.initiate({
  _id: "rs1",
  members: [
    { _id: 0, host: "mongo1:27017" },
    { _id: 1, host: "mongo2:27018" },
    { _id: 2, host: "mongo3:27019" }
  ]
})

rs.status()

EOF
```

### host 설정
로컬 접속 위한 host 설정
/etc/hosts
```text
127.0.0.1       mongo1
127.0.0.1       mongo2
127.0.0.1       mongo3
```

- Up
```shell
docker compose up
```

- 접속
```shell
docker exec -it mongo1 /bin/bash
mongo -u root -p rootpass

```
- 상태 확인
```shell
rs.status()
```

- Down
```shell
docker compose down -v
```

- rs 구성
```shell
# 접속
mongosh -u root -p 
# 초기화
rs.initiate({_id:"rs1", members:[{_id: 0, host:"mongo1:27017"},{_id: 1, host: "mongod:27018"},{_id: 2, host: "mongo3:27019"}]})
# 확인
rs.status()
```