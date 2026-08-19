# ID Generator Tutorial (TSID vs UUID)

Kotlin 및 Spring Boot 기반의 고유 식별자(ID) 생성기 예제 프로젝트입니다.
주로 **TSID (Time-Sorted Unique Identifier)** 와 **UUID (Universally Unique Identifier)** 의 특징, 사용법, 그리고 성능/특성 비교 벤치마크를 다룹니다.

---

## 📌 주요 식별자(ID) 생성 방식 비교

| 구분 | TSID (`TsidCreator.getTsid()`) | TSID Fast (`Tsid.fast()`) | UUID v4 (`UUID.randomUUID()`) |
| :--- | :--- | :--- | :--- |
| **길이 및 포맷** | 64-bit (`Long` 또는 13자리 Crockford Base32) | 64-bit (`Long` 또는 13자리 Crockford Base32) | 128-bit (36자리 Hex 문자열) |
| **시간순 정렬** | ✅ 지원 (단조 증가 보장) | ⚠️ 밀리초 정렬 (동일 ms 내 랜덤) | ❌ 미지원 (완전 랜덤) |
| **스레드 안전성** | ✅ 동기화(`synchronized`)로 충돌 방지 | ⚠️ Lock-free (경합 시 동일 ID 충돌 가능) | ✅ 안전 (SecureRandom 기반) |
| **DB 인덱스 효율** | 🚀 최상 (B-Tree 인덱스 단편화 최소화) | 🚀 최상 (B-Tree 인덱스 단편화 최소화) | ⚠️ 저하 (랜덤 삽입으로 인한 Page Split 유발) |
| **주요 권장 용도** | **RDBMS PK**, 분산 시스템 Unique Key | 분산 트레이싱 ID, 단기 로그/캐시 키 | 분산 환경 UUID 요구 표준 필드 |

---

## 🛠️ 주요 구현

### 1. TsidGenerator
`com.github.f4b6a3:tsid-creator` 라이브러리를 래핑하여 `String` 또는 `Long` 타입의 TSID를 반환합니다.

```kotlin
// String (13자리 문자열, e.g. "01ARZ3NDEKTSV")
val stringId: String = TsidGenerator.generator<String>()

// Long (64-bit 정수, e.g. 156485854611417088L)
val longId: Long = TsidGenerator.generator<Long>()
```

### 2. UuidGenerator
Java 표준 `UUID.randomUUID()`를 사용하여 36자리 UUID 문자열을 생성합니다.

```kotlin
val uuid: String = UuidGenerator.generate()
```

---

## 🧪 벤치마크 및 테스트

본 프로젝트에는 ID 생성기 간의 처리량, 레이턴시, 단조 증가성 및 중복 검증을 위한 벤치마크 테스트 코드가 포함되어 있습니다.

### 1. TSID vs UUID 성능 비교
- 테스트 파일: `PerformanceComparisonTest.kt`
- **단일/멀티스레드 처리량 (Throughput)** 비교
- **평균 및 P95 레이턴시 (Latency)** 측정
- 고부하 환경에서의 **유일성 (Uniqueness)** 검증

### 2. TsidCreator.getTsid() vs Tsid.fast() 심층 비교
- 테스트 파일: `TsidFastVsGetTsidBenchmarkTest.kt`

| 항목 | `TsidCreator.getTsid()` | `Tsid.fast()` |
| :--- | :--- | :--- |
| **내부 구현** | `TsidFactory` (`synchronized`) | `currentTimeMillis` + `ThreadLocalRandom` |
| **단조 증가(Monotonic)** | **보장** (동일 ms 내 시퀀스 증가) | **미보장** (랜덤 비트 사용) |
| **멀티스레드 성능** | 락 경합으로 인한 상대적 지연 | 락이 없어 처리량 극대화 |
| **중복(Collision)** | 100% 충돌 방지 | 멀티스레드 동시 생성 시 충돌 가능성 존재 |

---

## 🚀 테스트 실행 방법

```bash
# 전체 테스트 실행
./gradlew test

# 특정 벤치마크 테스트 실행
./gradlew test --tests jhkim105.idgenerator.TsidFastVsGetTsidBenchmarkTest
./gradlew test --tests jhkim105.idgenerator.PerformanceComparisonTest
```

---

## 📚 참고 링크
- [tsid-creator GitHub Repository](https://github.com/f4b6a3/tsid-creator)

