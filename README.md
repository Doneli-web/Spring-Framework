### Project Structur :
```
    MonFramework/
    │
    ├── src/
    │   ├── framework/
    │   │   ├── annotation/
    │   │   │   ├── Controller.java
    │   │   │   ├── Get.java
    │   │   │   ├── Post.java
    │   │   │   ├── RequestParam.java
    │   │   │   └── ...
    │   │   │
    │   │   ├── core/
    │   │   │   ├── FrontController.java
    │   │   │   ├── Dispatcher.java
    │   │   │   ├── Route.java
    │   │   │   ├── RouteRegistry.java
    │   │   │   └── ...
    │   │   │
    │   │   ├── reflection/
    │   │   │   ├── ClassScanner.java
    │   │   │   ├── AnnotationProcessor.java
    │   │   │   └── ...
    │   │   │
    │   │   ├── modelview/
    │   │   │   ├── ModelView.java
    │   │   │   └── ...
    │   │   │
    │   │   ├── exception/
    │   │   │   ├── RouteNotFoundException.java
    │   │   │   ├── FrameworkException.java
    │   │   │   └── ...
    │   │   │
    │   │   └── util/
    │   │       ├── JsonUtil.java
    │   │       ├── StringUtil.java
    │   │       └── ...
    │   │
    │   └── test/
    │       ├── controller/
    │       │   └── UserController.java
    │       └── model/
    │
    ├── lib/
    │   ├── jakarta.servlet-api.jar
    │   └── ...
    │
    ├── bin/
    │
    ├── docs/
    │   ├── UML/
    │   ├── specifications/
    │   └── examples/
    │
    ├── build/
    │
    ├── README.md
    │
    └── framework.jar
```

### Compilation :
```
    javac -cp "lib/servlet-api.jar" -d bin src/main/java/framework/servlet/*.java src/main/java/framework/annotation/*.java src/main/java/framework/reflection/*.java src/main/java/framework/exception/*.java src/main/java/framework/listener/*.java src/main/java/framework/route/*.java
    jar cf framework.jar -C bin .


    javac -cp "lib\*" -d WEB-INF\classes src\main\java\controller\*.java
    jar cvf FrameworkMVC.war .
```