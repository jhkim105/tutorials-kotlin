# 벤치마크 환경 튜닝 및 트러블슈팅 가이드

## 1. 파일 디스크립터 (`ulimit`)
동시 연결 수 5,000 ~ 10,000 이상을 측정할 때는 서버와 부하기(`loadgen`) 모두 소켓 한도를 올려야 합니다.

```bash
ulimit -n 65535
```

- macOS에서 `ulimit -n 65535`가 실패하면 `sudo launchctl limit maxfiles 65536 200000`으로 상한을 먼저 올립니다.
- Docker 컨테이너는 호스트 데몬 설정을 따르므로 필요 시 `compose.yml`에 `ulimits.nofile`을 지정합니다.

## 2. Ephemeral Port 한도
로컬 단일 호스트에서 부하기가 `localhost`로 수만 개의 TCP 연결을 열면 Ephemeral Port가 고갈될 수 있습니다.
- macOS 확인: `sysctl net.inet.ip.portrange.first net.inet.ip.portrange.last`
- Linux 확인: `sysctl net.ipv4.ip_local_port_range` (권장: `1024 65535`)
- `TIME_WAIT` 소켓이 많이 쌓이면 연속 실행 사이에 10~30초 간격을 두거나 Linux에서 `net.ipv4.tcp_tw_reuse=1`을 고려합니다.

## 3. Virtual Thread Pinning 확인 (구성 B, C2)
Java 21에서 `synchronized` 블록 내부 블로킹 호출 시 캐리어 스레드가 고정(pinning)될 수 있습니다.
- JVM 옵션 `-Djdk.tracePinnedThreads=short` 또는 JFR 이벤트 `jdk.VirtualThreadPinned`로 확인합니다.

## 4. 로컬 macOS Docker 결과 해석
- macOS Docker Desktop은 Linux VM 위에서 동작하므로 네트워크 브리지/CPU 스케줄링 편차가 존재합니다.
- 모든 구성(A~E)은 동일한 조건(`cpus=2`, `memory=1g`, `-Xmx512m`)에서 측정하므로 **구성 간 상대 비교용**으로 해석하고, 절대 처리량 검증이 필요하면 Linux 서버에서 동일한 `./scripts/run-benchmark.sh`를 재실행합니다.
