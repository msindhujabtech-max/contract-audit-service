# Security — Interview Preparation

Security questions are expected for a lead. This covers web security, Spring Security, and secure coding.

## Authentication vs Authorization (recap)

- **Authentication** — WHO you are (login). Fails → 401.
- **Authorization** — WHAT you can do (permissions). Fails → 403.

## OWASP Top 10 (know the major ones)

| Risk | What it is | Defense |
|------|-----------|---------|
| Injection (SQL/NoSQL) | Malicious input runs as code | Parameterized queries, ORM |
| Broken Authentication | Weak login/session handling | MFA, secure tokens, lockout |
| Sensitive Data Exposure | Unencrypted data | TLS, encrypt at rest, hash passwords |
| XXE | Malicious XML entities | Disable external entities |
| Broken Access Control | Users access others' data | Enforce authz on every request |
| Security Misconfiguration | Defaults, verbose errors | Harden config, hide stack traces |
| XSS | Inject scripts into pages | Escape output, CSP header |
| Insecure Deserialization | Malicious serialized objects | Validate, avoid native deserialization |
| Vulnerable Components | Old libraries with CVEs | Scan dependencies (OWASP, Snyk) |
| Insufficient Logging | Can't detect breaches | Log security events, monitor |

## SQL Injection

**Attack:** input like `' OR '1'='1` alters the query.
```java
// VULNERABLE — string concatenation
String sql = "SELECT * FROM users WHERE name = '" + input + "'";

// SAFE — parameterized query (input is data, never code)
PreparedStatement ps = conn.prepareStatement(
    "SELECT * FROM users WHERE name = ?");
ps.setString(1, input);
```
> With JPA/Hibernate and Spring Data, you use parameter binding, which prevents this by default.

## XSS (Cross-Site Scripting)

**Attack:** attacker injects `<script>` that runs in another user's browser.
**Defense:** escape/encode output, use a Content-Security-Policy header, sanitize HTML input. Modern frameworks (React) auto-escape by default.

## CSRF (Cross-Site Request Forgery)

**Attack:** a malicious site tricks a logged-in user's browser into submitting a request.
**Defense:** CSRF tokens (Spring Security includes this), SameSite cookies. Note: stateless JWT APIs are less susceptible since they don't rely on cookies.

## HTTPS / TLS

Encrypts data in transit so it can't be read or tampered with.
- **TLS handshake:** client and server agree on keys; server proves identity with a certificate.
- Always use HTTPS for anything with credentials or sensitive data.

## Password Storage

Never store plaintext. **Hash** with a slow, salted algorithm.
```java
// Spring Security BCrypt
PasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode(rawPassword);       // store this
boolean ok = encoder.matches(rawPassword, hash); // verify at login
```
- **Salt** — random per-password value prevents rainbow-table attacks.
- **bcrypt/scrypt/argon2** — deliberately slow to resist brute force. Never MD5/SHA-1 for passwords.

## JWT (JSON Web Token) Deep Dive

Structure: `header.payload.signature` (base64).
```
Header:    { "alg": "HS256", "typ": "JWT" }
Payload:   { "sub": "user123", "role": "ADMIN", "exp": 1699999999 }
Signature: HMAC(header + payload, secret)   // proves it wasn't tampered
```
- The server **signs** the token; it only needs to verify the signature (stateless).
- Store minimal data; it's readable (base64), just not forgeable without the secret.
- Downsides: hard to revoke early → keep expiry short + use refresh tokens.

## OAuth2 (authorization framework)

Lets an app access resources on a user's behalf without sharing passwords ("Login with Google").
**Roles:** Resource Owner (user), Client (app), Authorization Server (issues tokens), Resource Server (holds data).

**Authorization Code flow (most common):**
```
1. App redirects user to Auth Server login
2. User approves → Auth Server returns a code
3. App exchanges code (+ secret) for an access token
4. App calls Resource Server with the access token
```
**OAuth2 vs OIDC:** OAuth2 is for authorization (access). OpenID Connect (OIDC) adds authentication (identity) on top with an ID token.

## Spring Security Architecture

```
Request → Filter Chain → Authentication → Authorization → Controller
```
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/public/**").permitAll()
              .requestMatchers("/admin/**").hasRole("ADMIN")
              .anyRequest().authenticated())
          .oauth2ResourceServer(oauth -> oauth.jwt());  // validate JWTs
        return http.build();
    }
}
```
- **Method-level security:** `@PreAuthorize("hasRole('ADMIN')")` on service methods.
- **Filter chain:** each filter handles one concern (auth, CSRF, CORS).

## Secure Coding Practices

- Validate ALL input (never trust the client).
- Principle of least privilege (minimal permissions).
- Don't leak details in errors (no stack traces to users).
- Keep secrets out of code — use Vault / Key Vault / env vars (as you did with the Gmail password via env var).
- Scan dependencies for known CVEs.
- Rate limit and add authentication to every endpoint.

## Common Security Interview Questions

**Q: How do you prevent SQL injection?**
> Always use parameterized queries / prepared statements (or an ORM that binds parameters). Never concatenate user input into SQL. Validate and sanitize input as defense in depth.

**Q: How do you store passwords?**
> Hash with a slow, salted algorithm like bcrypt (Spring's BCryptPasswordEncoder). Never store plaintext or use fast hashes like MD5/SHA-1.

**Q: JWT vs session — which and why?**
> JWT for stateless, horizontally-scaled APIs (server just verifies the signature). Sessions when you need easy revocation. JWT downside: hard to revoke early — mitigate with short expiry + refresh tokens.

**Q: How do you secure microservices?**
> Authenticate at the API Gateway (JWT/OAuth2), service-to-service auth (mTLS or signed tokens), secrets in a vault, network policies, and least-privilege access. Encrypt in transit (TLS) and at rest.

**Q: What is CORS?**
> Cross-Origin Resource Sharing — a browser security feature controlling which origins can call your API. Configure allowed origins/methods on the server (you had a CorsConfig in the analyser).

**Q: How do you handle secrets in a CI/CD pipeline?**
> Store them in a secrets manager (Vault, GitHub Secrets, Key Vault), inject at runtime as env vars, never commit them to Git, and rotate regularly.
