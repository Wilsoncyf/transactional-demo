# Spring Boot 事务传播行为演示项目 (Transactional Propagation Demo)

这是一个使用 Spring Boot 构建的演示项目，旨在通过具体的代码示例来学习和探索 Spring 框架中 `@Transactional` 注解提供的不同事务传播级别 (Propagation Levels)。

本项目作为一个动手实践的学习工具，逐步展示各种传播行为的配置、执行逻辑以及它们在不同场景下的影响（特别是事务的合并、独立性、回滚行为）。

## 技术栈

* Java 17+
* Spring Boot 3.x
* Spring Data JPA
* H2 Database (内存数据库，方便测试)
* Maven
* Lombok

## 当前已覆盖特性/场景 (Features/Scenarios Covered)

* 基础的 Spring Boot 项目结构 (Controller, Service, Repository, Entity)。
* **`Propagation.REQUIRED` (默认传播级别) 演示:**
    * 内部事务方法加入已存在的外部事务 (`/scenario1/*` 接口)。
    * 在没有外部事务时，方法自己创建新事务 (`/scenario2/*` 接口)。
    * 清晰展示 `REQUIRED` 下“共同命运”（一起提交或一起回滚）的行为。
* 提供 RESTful API 端点 (`DemoController`) 用于触发不同测试场景。
* 启用 H2 数据库控制台，方便在运行时查看数据库状态。

## 如何运行

1.  **克隆仓库** (如果你还没克隆的话，对于协作者来说)：
    ```bash
    git clone [https://github.com/Wilsoncyf/transactional-demo.git](https://github.com/Wilsoncyf/transactional-demo.git)
    cd transactional-demo
    ```
2.  **运行应用:**
    * 使用 Maven Wrapper: `./mvnw spring-boot:run` (macOS/Linux) 或 `mvnw.cmd spring-boot:run` (Windows)
    * 或者直接在你的 IDE 中运行 `com.example.transactionaldemo.TransactionalDemoApplication` 类。
3.  应用启动后，默认监听在 `http://localhost:8080`。

## 测试端点 (Endpoints)

你可以使用 `curl` 或浏览器访问以下 GET 请求端点来触发和测试不同的事务场景：

* `GET http://localhost:8080/scenario1/success`
    * 测试: `REQUIRED` 加入，内部成功 -> 预期: 整体事务提交。
* `GET http://localhost:8080/scenario1/fail`
    * 测试: `REQUIRED` 加入，内部失败 -> 预期: 整体事务回滚。
* `GET http://localhost:8080/scenario2/success`
    * 测试: `REQUIRED` 创建新事务，内部成功 -> 预期: 内部事务提交。
* `GET http://localhost:8080/scenario2/fail`
    * 测试: `REQUIRED` 创建新事务，内部失败 -> 预期: 内部事务回滚。

## H2 数据库控制台

项目配置了 H2 内存数据库，并启用了 Web 控制台。

* **访问地址:** 应用启动后，访问 `http://localhost:8080/h2-console`
* **JDBC URL:** **重要！** 请检查应用启动时的控制台日志，找到类似 `Database available at 'jdbc:h2:mem:xxxxxxxx'` 的信息，将这个完整的 URL 填入 H2 控制台的 JDBC URL 输入框中。
* **用户名:** `sa`
* **密码:** (留空)

## 后续计划 (Next Steps)

本项目将逐步添加对其他 Spring 事务传播级别的演示代码和说明，例如：

* `Propagation.REQUIRES_NEW`
* `Propagation.NESTED`
* `Propagation.SUPPORTS`
* `Propagation.MANDATORY`
* `Propagation.NOT_SUPPORTED`
* `Propagation.NEVER`

欢迎关注更新！