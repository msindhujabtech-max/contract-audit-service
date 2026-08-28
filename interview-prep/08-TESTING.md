# Testing — Interview Preparation

## Testing Pyramid

```
        /\
       /  \      E2E Tests (few, slow, expensive)
      /____\
     /      \    Integration Tests (some)
    /________\
   /          \  Unit Tests (many, fast, cheap)
  /____________\
```
Write many unit tests, fewer integration tests, minimal E2E tests.

---

## JUnit 5 (your resume)

### Structure
```java
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    private Calculator calc;

    @BeforeEach          // runs before each test
    void setup() { calc = new Calculator(); }

    @Test
    void testAdd() {
        assertEquals(5, calc.add(2, 3));
    }

    @Test
    void testDivideByZero() {
        assertThrows(ArithmeticException.class, () -> calc.divide(1, 0));
    }

    @AfterEach void cleanup() { }   // after each test
    @BeforeAll static void initAll() { }  // once before all
    @AfterAll static void tearDown() { }   // once after all
}
```

### JUnit 5 annotations
| Annotation | Purpose |
|-----------|---------|
| `@Test` | Marks a test method |
| `@BeforeEach` / `@AfterEach` | Setup/cleanup per test |
| `@BeforeAll` / `@AfterAll` | Once for all tests (static) |
| `@Disabled` | Skip a test |
| `@ParameterizedTest` | Run with multiple inputs |
| `@DisplayName` | Readable test name |

### Parameterized tests
```java
@ParameterizedTest
@ValueSource(ints = {2, 4, 6, 8})
void testEven(int number) {
    assertTrue(number % 2 == 0);
}

@ParameterizedTest
@CsvSource({"2,3,5", "10,20,30"})
void testAdd(int a, int b, int expected) {
    assertEquals(expected, calc.add(a, b));
}
```

### Common assertions
```java
assertEquals(expected, actual);
assertTrue(condition);
assertNotNull(object);
assertThrows(Exception.class, () -> method());
assertAll(                       // group assertions
    () -> assertEquals(1, x),
    () -> assertEquals(2, y)
);
```

---

## Mockito (your resume)

Mocking framework — create fake objects to isolate the unit under test.

### Why mock?
Test a service without hitting the real database, external API, etc. Fast, isolated, deterministic.

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PaymentGateway paymentGateway;   // fake dependency

    @InjectMocks
    private OrderService orderService;        // real class, mocks injected

    @Test
    void testPlaceOrder() {
        // Arrange — define mock behavior
        when(paymentGateway.charge(100.0)).thenReturn(true);

        // Act
        boolean result = orderService.placeOrder(100.0);

        // Assert
        assertTrue(result);
        verify(paymentGateway).charge(100.0);        // verify called
        verify(paymentGateway, times(1)).charge(anyDouble());
    }
}
```

### Key Mockito methods
| Method | Purpose |
|--------|---------|
| `mock(Class)` | Create a mock |
| `when(...).thenReturn(...)` | Stub a return value |
| `when(...).thenThrow(...)` | Stub an exception |
| `verify(mock).method()` | Verify a call happened |
| `verify(mock, times(n))` | Verify call count |
| `any()`, `anyInt()`, `eq()` | Argument matchers |
| `@Mock` | Create mock via annotation |
| `@InjectMocks` | Inject mocks into the tested object |
| `@Spy` | Partial mock (real methods unless stubbed) |

### Argument captor
```java
ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
verify(repository).save(captor.capture());
assertEquals("PENDING", captor.getValue().getStatus());
```

---

## PowerMock / PowerMockito (your resume)

Extends Mockito to mock things Mockito can't:
- Static methods
- Private methods
- Final classes/methods
- Constructors

```java
@RunWith(PowerMockRunner.class)
@PrepareForTest(Utils.class)
public class ServiceTest {
    @Test
    public void testStaticMethod() {
        PowerMockito.mockStatic(Utils.class);
        when(Utils.getConfig()).thenReturn("test-value");
        // ...
    }
}
```
> **Note:** Modern Mockito (3.4+) can mock statics with `mockStatic()`, reducing the need for PowerMock. Good to mention you're aware of this evolution.

---

## Spring Boot Testing

### @SpringBootTest (integration test)
```java
@SpringBootTest
class ApplicationTests {
    @Autowired
    private OrderService orderService;

    @Test
    void contextLoads() {
        assertNotNull(orderService);
    }
}
```

### @WebMvcTest (test only web layer)
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testGetUser() throws Exception {
        when(userService.findById(1L)).thenReturn(new User("Alice"));

        mockMvc.perform(get("/api/users/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Alice"));
    }
}
```

### @DataJpaTest (test repository layer)
```java
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository repository;

    @Test
    void testFindByEmail() {
        repository.save(new User("a@test.com"));
        assertNotNull(repository.findByEmail("a@test.com"));
    }
}
```

### Testing WebFlux (your reactive code)
```java
@Test
void testMono() {
    Mono<String> result = service.getData();
    StepVerifier.create(result)
        .expectNext("expected")
        .verifyComplete();
}
```

---

## Test Coverage & Quality

- **Code coverage** — % of code executed by tests (JaCoCo). Aim 70-80%, but coverage ≠ quality.
- **SonarQube** — analyzes code for bugs, vulnerabilities, code smells, and coverage gaps.

---

## TDD (Test-Driven Development)

Write the test first, then code to pass it.
```
Red (failing test) → Green (make it pass) → Refactor → repeat
```

---

## Common Testing Interview Questions

**Q: Unit vs Integration test?**
> Unit tests a single class in isolation (mocks dependencies). Integration tests multiple components together (real DB, real Spring context).

**Q: @Mock vs @MockBean?**
> `@Mock` (Mockito) — plain mock for unit tests. `@MockBean` (Spring) — replaces a bean in the Spring context for integration tests.

**Q: How do you test a private method?**
> Ideally, test it indirectly through public methods. If necessary, use reflection or PowerMock, but a need to test privates often signals a design issue.

**Q: What makes a good unit test?**
> Fast, isolated, repeatable, self-validating, one logical assertion per concept. Follows AAA: Arrange, Act, Assert.

**Q: How do you test code that calls an external API?**
> Mock the HTTP client, or use WireMock to stub responses, or a Testcontainers-based integration test for realism.

**Q: What is a flaky test?**
> A test that passes/fails inconsistently — often due to timing, shared state, or external dependencies. Fix by removing nondeterminism.
