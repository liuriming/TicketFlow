package com.ticketflow.integration;

import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.notification.service.NotificationPublisher;
import com.ticketflow.system.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 登录鉴权集成测试。
 *
 * <p>使用 Testcontainers 启动 MySQL、Redis 和 RabbitMQ，验证数据库初始化、登录 token 写入 Redis、
 * 携带 token 获取当前用户信息的关键链路。</p>
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("ticketflow")
            .withUsername("ticketflow")
            .withPassword("ticketflow123");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "ticketflow123");

    @Container
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.0-management"));

    @Container
    static final GenericContainer<?> MINIO = new GenericContainer<>(DockerImageName.parse("minio/minio:RELEASE.2025-04-22T22-12-26Z"))
            .withEnv("MINIO_ROOT_USER", "ticketflow")
            .withEnv("MINIO_ROOT_PASSWORD", "ticketflow123")
            .withCommand("server", "/data")
            .withExposedPorts(9000);

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private NotificationPublisher notificationPublisher;

    private String token;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "ticketflow123");
        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
        registry.add("minio.endpoint", () -> "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000));
        registry.add("minio.access-key", () -> "ticketflow");
        registry.add("minio.secret-key", () -> "ticketflow123");
        registry.add("minio.bucket", () -> "ticketflow");
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @BeforeEach
    void login() {
        token = loginAndToken();
    }

    @Test
    @SuppressWarnings("unchecked")
    void 管理员登录后可以携带Token获取当前用户信息() {
        ResponseEntity<Map> meResponse = restTemplate.exchange(
                url("/api/auth/me"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );

        assertThat(meResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> meBody = meResponse.getBody();
        assertThat(meBody).isNotNull();
        assertThat(meBody.get("code")).isEqualTo(0);
        Map<String, Object> meData = (Map<String, Object>) meBody.get("data");
        assertThat(meData.get("username")).isEqualTo("admin");
    }

    @Test
    void 登录Token会写入Redis缓存() {
        Boolean exists = redisTemplate.hasKey("ticketflow:login:" + token);

        assertThat(exists).isTrue();
    }

    @Test
    void 当前用户权限快照会写入Redis缓存() {
        restTemplate.exchange(
                url("/api/auth/me"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );

        Boolean exists = redisTemplate.hasKey("ticketflow:permission:user:1");
        assertThat(exists).isTrue();
    }

    @Test
    void 接口权限会阻止员工访问系统管理接口并允许管理员访问() {
        String employeeToken = loginAndToken("employee", "123456");

        ResponseEntity<Map> forbiddenResponse = restTemplate.exchange(
                url("/api/system/users"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(employeeToken)),
                Map.class
        );
        assertThat(forbiddenResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(forbiddenResponse.getBody()).isNotNull();
        assertThat(forbiddenResponse.getBody().get("code")).isEqualTo(403);

        ResponseEntity<Map> adminResponse = restTemplate.exchange(
                url("/api/system/users"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );
        assertThat(adminResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(adminResponse.getBody()).isNotNull();
        assertThat(adminResponse.getBody().get("code")).isEqualTo(0);
    }

    @Test
    void 员工提交工单后自动派给工程师并完成闭环() {
        String employeeToken = loginAndToken("employee", "123456");
        String engineerToken = loginAndToken("ops_engineer", "123456");

        Map<String, Object> created = postData("/api/tickets", Map.of(
                "title", "集成测试工单",
                "description", "验证工单闭环",
                "categoryId", 1,
                "priority", "MEDIUM"
        ), employeeToken);
        Long ticketId = numberValue(created.get("id")).longValue();
        assertThat(created.get("status")).isEqualTo("PENDING_ACCEPT");
        assertThat(numberValue(created.get("assigneeId")).longValue()).isEqualTo(3L);

        ResponseEntity<Map> forbiddenTransfer = postRaw("/api/tickets/" + ticketId + "/transfer", Map.of(
                "assigneeId", 1,
                "reason", "员工尝试转派"
        ), employeeToken);
        assertThat(forbiddenTransfer.getBody()).isNotNull();
        assertThat(forbiddenTransfer.getBody().get("code")).isEqualTo(403);

        Map<String, Object> accepted = postData("/api/tickets/" + ticketId + "/accept", Map.of(), engineerToken);
        assertThat(accepted.get("status")).isEqualTo("PROCESSING");

        Map<String, Object> processed = postData("/api/tickets/" + ticketId + "/process", Map.of(
                "result", "集成测试处理完成"
        ), engineerToken);
        assertThat(processed.get("status")).isEqualTo("PENDING_CONFIRM");

        Map<String, Object> closed = postData("/api/tickets/" + ticketId + "/confirm-close", Map.of(
                "remark", "集成测试确认关闭"
        ), employeeToken);
        assertThat(closed.get("status")).isEqualTo("CLOSED");
    }

    @Test
    @SuppressWarnings("unchecked")
    void 附件可以上传并按业务对象查询() {
        Map<String, Object> ticket = postData("/api/tickets", Map.of(
                "title", "附件集成测试工单",
                "description", "验证附件业务权限",
                "categoryId", 1,
                "priority", "LOW"
        ));
        Long ticketId = numberValue(ticket.get("id")).longValue();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource("集成测试日志内容".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "integration.log";
            }
        });

        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Map> uploadResponse = restTemplate.postForEntity(
                url("/api/attachments/upload?businessType=TICKET&businessId=" + ticketId),
                new HttpEntity<>(body, headers),
                Map.class
        );

        assertThat(uploadResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> uploadData = data(uploadResponse);
        assertThat(uploadData.get("originalName")).isEqualTo("integration.log");

        ResponseEntity<Map> listResponse = restTemplate.exchange(
                url("/api/attachments?businessType=TICKET&businessId=" + ticketId),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) data(listResponse);
        assertThat(attachments).anySatisfy(attachment -> assertThat(attachment.get("originalName")).isEqualTo("integration.log"));

        String employeeToken = loginAndToken("employee", "123456");
        ResponseEntity<Map> forbiddenListResponse = restTemplate.exchange(
                url("/api/attachments?businessType=TICKET&businessId=" + ticketId),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(employeeToken)),
                Map.class
        );
        assertThat(forbiddenListResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(forbiddenListResponse.getBody()).isNotNull();
        assertThat(forbiddenListResponse.getBody().get("code")).isEqualTo(403);
    }

    @Test
    void RabbitMQ通知事件可以异步生成站内信() throws Exception {
        notificationPublisher.publish(new NotificationEvent(
                1L,
                "集成测试通知",
                "RabbitMQ 通知链路验证",
                "INTEGRATION_TEST",
                90001L
        ));

        Map<String, Object> message = waitForMessage("INTEGRATION_TEST", 90001L);

        assertThat(message).isNotNull();
        assertThat(message.get("title")).isEqualTo("集成测试通知");
    }

    @SuppressWarnings("unchecked")
    private String loginAndToken() {
        return loginAndToken("admin", "123456");
    }

    @SuppressWarnings("unchecked")
    private String loginAndToken(String username, String password) {
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                url("/api/auth/login"),
                new LoginRequest(username, password),
                Map.class
        );

        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> loginData = data(loginResponse);
        String loginToken = (String) loginData.get("token");
        assertThat(loginToken).isNotBlank();
        return loginToken;
    }

    private HttpHeaders authHeaders() {
        return authHeaders(token);
    }

    private HttpHeaders authHeaders(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        return headers;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postData(String path, Object body) {
        return postData(path, body, token);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postData(String path, Object body, String bearerToken) {
        ResponseEntity<Map> response = postRaw(path, body, bearerToken);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return data(response);
    }

    private ResponseEntity<Map> postRaw(String path, Object body, String bearerToken) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url(path),
                new HttpEntity<>(body, authHeaders(bearerToken)),
                Map.class
        );
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return response;
    }

    @SuppressWarnings("unchecked")
    private <T> T data(ResponseEntity<Map> response) {
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo(0);
        return (T) body.get("data");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> waitForMessage(String businessType, Long businessId) throws Exception {
        for (int i = 0; i < 20; i++) {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url("/api/messages?pageNo=1&pageSize=20"),
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders()),
                    Map.class
            );
            Map<String, Object> page = data(response);
            List<Map<String, Object>> records = (List<Map<String, Object>>) page.get("records");
            Map<String, Object> message = records.stream()
                    .filter(record -> businessType.equals(record.get("businessType"))
                            && businessId.equals(numberValue(record.get("businessId")).longValue()))
                    .findFirst()
                    .orElse(null);
            if (message != null) {
                return message;
            }
            Thread.sleep(200L);
        }
        return null;
    }

    private Number numberValue(Object value) {
        assertThat(value).isInstanceOf(Number.class);
        return (Number) value;
    }

    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }
}
