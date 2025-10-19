

## restdocs-api-spec 적용하기

build.gradle.kts
```
plugins {
    id("com.epages.restdocs-api-spec") version "0.15.3"
}

openapi3 {
    this.setServer("https://localhost:8080") 
    title = "My API"
    description = "My API description"
    version = "0.1.0"
    format = "yaml" // or json
}

dependencies {
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.19.4")
}
```

## Refs
- https://github.com/ePages-de/restdocs-api-spec

