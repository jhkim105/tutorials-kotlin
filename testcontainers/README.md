

## Annotation

### @Testcontainers
- 이 어노테이션이 클래스에 선언되면, JUnit 5 의해 Testcontainers 라이프사이클 과 통합
- 내부적으로 JUnit이 @Container로 정의된 정적 필드를 자동으로 감지
- 
### @Container
- static(companion object)으로 선언된 필드에 붙일 경우 테스트 클래스 전체에서 한 번만 시작됨 (클래스 단위 컨테이너 공유)
- 인스턴스 변수에 사용하면 각 테스트 마다 새로 생성됨

## Refs
- https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/
