


## Multi-Project Builds
The Gradle community has two standards for multi-project build structures:
- Multi-Project Builds using buildSrc - where buildSrc is a subproject-like directory at the Gradle project root containing all the build logic.
- Composite Builds - a build that includes other builds where build-logic is a build directory at the Gradle project root containing reusable build logic.


### Sharing Build Logic using buildSrc
- https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html
- https://github.com/gradle/gradle/tree/master/platforms/documentation/docs/src/snippets/multiproject/buildSrc

buildSrc 는 가장 먼저 컴파일 된다.  
순서: buildSrc -> settings.gradle.kts -> build.gradle.kts

buildSrc 에서는
- 공통 플러그인 정의
- 의존성 버전 관리
- 빌드 스크립트 유틸리티 함수
- 플러그인 확장(custom)

```text
├── buildSrc
│   ├── src
│   │   └──main
│   │      └──kotlin
│   │         └──java-common-conventions.gradle.kts  
│   └── build.gradle.kts
├── api
│   ├── src/
│   └── build.gradle.kts            
├── services
│   ├── src/
│   └── build.gradle.kts            
├── shared
│   ├── src/
│   └── build.gradle.kts            
└── settings.gradle.kts
```

### Composite Builds (build-logic)
- https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html#using_a_composite_build_named_build_logic
```text
.
├── build-logic/                   
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── src/main/kotlin
│       └── java-common-conventions.gradle.kts
├── api/
│   └── build.gradle.kts           
├── services/
│   └── build.gradle.kts           
├── shared/
│   └── build.gradle.kts           
└── settings.gradle.kts    
```

Avoid cross-project configuration using subprojects and allprojects



## Refs
- https://docs.gradle.org/current/userguide/multi_project_builds.html
