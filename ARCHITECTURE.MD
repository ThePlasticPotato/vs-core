# VS Core Architecture Overview

## Dependency Graph (non-exhaustive)

```mermaid
  graph
 
     eureka-1.16["Eureka 1.16"] --> vs2-api
     vs2-impl["VS2 1.16"] --> |implementation|api-game
     vs2-impl --> vs2-api
     vs2-api["VS2 1.16 API"] --> |api|api-addon 
     
     subgraph vs-core["VS Core (You are here)"]
          
     api-addon["vs-core:api-addon"]
     api-game["vs-core:api-game"]
     api["vs-core:api"]
     impl["vs-core:impl"]
     
     api-addon & api-game --> |implementation|impl
     api-game --> |api|api-addon --> |api|api
     impl --> |api|api
     
     end
     
     impl --> physics-api[VS Physics API]
     impl --> krunch[Krunch]
     krunch --> physics-api
```

Note: `implementation` and `api` have the same meaning they do in gradle - `api`
dependencies are as part of the module's public API, `implementation`
dependencies are not


