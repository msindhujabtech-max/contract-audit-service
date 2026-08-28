# Databases & SQL — Interview Preparation

## SQL Fundamentals

### SQL Command Categories

| Category | Commands | Purpose |
|----------|----------|---------|
| DDL (Data Definition) | CREATE, ALTER, DROP, TRUNCATE | Define schema |
| DML (Data Manipulation) | SELECT, INSERT, UPDATE, DELETE | Manipulate data |
| DCL (Data Control) | GRANT, REVOKE | Permissions |
| TCL (Transaction Control) | COMMIT, ROLLBACK, SAVEPOINT | Transactions |

The quick way to remember these: DDL changes the *shape* of the database (tables, columns), DML changes the *data* inside it, DCL changes *who is allowed* to touch it, and TCL decides whether a batch of DML changes is *permanent or undone*. A single audit-save flow can touch several categories:

```sql
-- DDL: define the table once (schema)
CREATE TABLE audits (id NUMBER PRIMARY KEY, contract_id NUMBER, status VARCHAR2(20));

-- DCL: grant the app user permission to use it
GRANT SELECT, INSERT ON audits TO audit_app;

-- TCL + DML: insert a row, then make it permanent (or undo on error)
INSERT INTO audits VALUES (1, 100, 'PENDING');   -- DML
COMMIT;                                           -- TCL: now durable
```

### JOINs (very common interview topic)

```sql
-- INNER JOIN — only matching rows in both tables
SELECT o.id, c.name FROM orders o
INNER JOIN customers c ON o.customer_id = c.id;

-- LEFT JOIN — all from left, matching from right (nulls if no match)
SELECT c.name, o.id FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id;

-- RIGHT JOIN — all from right, matching from left
-- FULL OUTER JOIN — all rows from both
-- CROSS JOIN — Cartesian product (every combination)
-- SELF JOIN — table joined with itself (e.g., employee-manager)
```

```
INNER JOIN:        LEFT JOIN:         FULL OUTER:
   A ∩ B           A + (A ∩ B)        A ∪ B
```

### GROUP BY & Aggregate Functions

```sql
SELECT category, COUNT(*) AS total, AVG(price) AS avg_price
FROM products
GROUP BY category
HAVING COUNT(*) > 5      -- filter groups (WHERE filters rows)
ORDER BY avg_price DESC;
```

**WHERE vs HAVING:**
- WHERE filters rows BEFORE grouping
- HAVING filters groups AFTER grouping

### Subqueries

```sql
-- Find employees earning more than average
SELECT name, salary FROM employees
WHERE salary > (SELECT AVG(salary) FROM employees);

-- Correlated subquery (references outer query)
SELECT name FROM employees e
WHERE salary > (SELECT AVG(salary) FROM employees WHERE dept = e.dept);
```

### Window Functions (senior-level)

```sql
-- Rank employees by salary within each department
SELECT name, dept, salary,
       RANK() OVER (PARTITION BY dept ORDER BY salary DESC) AS rank,
       ROW_NUMBER() OVER (PARTITION BY dept ORDER BY salary DESC) AS row_num,
       DENSE_RANK() OVER (PARTITION BY dept ORDER BY salary DESC) AS dense_rank
FROM employees;
```

- `ROW_NUMBER` — unique sequential (1,2,3,4)
- `RANK` — gaps on ties (1,2,2,4)
- `DENSE_RANK` — no gaps on ties (1,2,2,3)

### Find Nth highest salary (classic question)

```sql
-- 2nd highest salary
SELECT MAX(salary) FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);

-- Nth highest using window function
SELECT salary FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
  FROM employees
) WHERE rnk = N;
```

### Find/Delete duplicates

```sql
-- Find duplicates
SELECT email, COUNT(*) FROM users
GROUP BY email HAVING COUNT(*) > 1;

-- Delete duplicates keeping lowest id
DELETE FROM users WHERE id NOT IN (
  SELECT MIN(id) FROM users GROUP BY email
);
```

---

## Indexing (performance interview topic)

An index is a data structure (usually B-tree) that speeds up lookups at the cost of slower writes and extra storage.

```sql
CREATE INDEX idx_customer_email ON customers(email);
CREATE UNIQUE INDEX idx_unique ON users(username);
CREATE INDEX idx_composite ON orders(customer_id, order_date);
```

### Types of indexes
- **B-tree** — default, good for equality and range queries
- **Hash** — fast equality, no range
- **Composite** — multiple columns (order matters — leftmost prefix rule)
- **Clustered** — determines physical row order (one per table)
- **Non-clustered** — separate structure pointing to rows

### When indexes help / hurt
- **Help:** WHERE, JOIN, ORDER BY on indexed columns
- **Hurt:** Too many indexes slow INSERT/UPDATE/DELETE
- Don't index low-cardinality columns (e.g., boolean)

---

## Transactions & ACID

**ACID** guarantees reliable transactions:
- **Atomicity** — all or nothing
- **Consistency** — valid state to valid state
- **Isolation** — concurrent transactions don't interfere
- **Durability** — committed data survives crashes

### Isolation Levels (concurrency issues)

| Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|-------|-----------|---------------------|--------------|
| READ UNCOMMITTED | Possible | Possible | Possible |
| READ COMMITTED | Prevented | Possible | Possible |
| REPEATABLE READ | Prevented | Prevented | Possible |
| SERIALIZABLE | Prevented | Prevented | Prevented |

Read the table top-to-bottom as "more isolation, but more locking and less concurrency." Most systems (including Postgres) default to READ COMMITTED, which is a good balance. You only raise the level when correctness demands it — for example, if two audit workers might both read a contract's balance and update it, REPEATABLE READ (or a `SELECT ... FOR UPDATE` lock) stops them from stepping on each other.

```sql
-- Force a stricter level for a sensitive read-modify-write
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
BEGIN;
  SELECT balance FROM contracts WHERE id = 100;  -- value won't change under me
  UPDATE contracts SET balance = balance - 50 WHERE id = 100;
COMMIT;
```

- **Dirty read** — reading uncommitted data
- **Non-repeatable read** — same query returns different rows (row changed)
- **Phantom read** — same query returns different number of rows (rows added/deleted)

---

## Normalization

Organizing data to reduce redundancy.

| Form | Rule |
|------|------|
| 1NF | Atomic values, no repeating groups |
| 2NF | 1NF + no partial dependency on composite key |
| 3NF | 2NF + no transitive dependency |
| BCNF | Stronger 3NF |

Concretely, each form removes a specific kind of redundancy. Say an `audits` table stored the auditor's name and department in every row — that repeats data and risks inconsistency (rename the department once, miss a row). 3NF fixes this by moving the auditor into its own table and referencing it by id:

```sql
-- NOT normalized: auditor_name + auditor_dept repeat on every audit row
audits(id, contract_id, auditor_name, auditor_dept)

-- 3NF: auditor details live once; audits point to them
auditors(id, name, dept)
audits(id, contract_id, auditor_id)   -- auditor_id is a foreign key
```

**Denormalization** — intentionally adding redundancy for read performance (common in reporting, microservices read models).

---

## Oracle DB (your primary — ATG projects)

Key Oracle-specific features:
```sql
-- Sequences (auto-increment)
CREATE SEQUENCE order_seq START WITH 1 INCREMENT BY 1;
SELECT order_seq.NEXTVAL FROM dual;

-- ROWNUM (limiting rows)
SELECT * FROM employees WHERE ROWNUM <= 10;

-- Oracle 12c+ pagination
SELECT * FROM employees ORDER BY salary
FETCH FIRST 10 ROWS ONLY;

-- MERGE (upsert)
MERGE INTO target t USING source s ON (t.id = s.id)
WHEN MATCHED THEN UPDATE SET t.name = s.name
WHEN NOT MATCHED THEN INSERT (id, name) VALUES (s.id, s.name);

-- Dual table — dummy table for expressions
SELECT SYSDATE FROM dual;
```

### PL/SQL basics
```sql
CREATE OR REPLACE PROCEDURE update_salary(p_id NUMBER, p_amt NUMBER) AS
BEGIN
    UPDATE employees SET salary = salary + p_amt WHERE id = p_id;
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN ROLLBACK;
END;
```

---

## MySQL (your resume)

Key differences from Oracle:
```sql
-- Auto-increment
CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50));

-- LIMIT for pagination
SELECT * FROM users LIMIT 10 OFFSET 20;

-- Storage engines: InnoDB (transactions, FK) vs MyISAM (faster reads, no FK)
```

---

## Liquibase (on your resume)

Database schema version control — track and apply DB changes like code.

```xml
<!-- changelog.xml -->
<changeSet id="1" author="sindhuja">
    <createTable tableName="products">
        <column name="id" type="BIGINT" autoIncrement="true">
            <constraints primaryKey="true"/>
        </column>
        <column name="name" type="VARCHAR(100)"/>
    </createTable>
</changeSet>
```

**Why Liquibase?**
- Version-controlled schema changes (in Git)
- Rollback support
- Works across DB vendors
- Runs automatically on app startup
- Each `changeSet` runs once, tracked in `DATABASECHANGELOG` table

**Liquibase vs Flyway:** Liquibase uses XML/YAML/JSON/SQL and supports rollback. Flyway is SQL-first and simpler.

---

## PostgreSQL + pgvector (your Gen AI project)

pgvector adds vector similarity search to Postgres — essential for RAG/AI.
```sql
CREATE EXTENSION vector;
CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    content TEXT,
    embedding vector(768)   -- 768-dimensional vector
);

-- Find 5 most similar documents (cosine distance)
SELECT content FROM documents
ORDER BY embedding <=> '[0.1, 0.2, ...]'
LIMIT 5;
```
Operators: `<=>` cosine distance, `<->` L2/Euclidean, `<#>` inner product.

---

## Common Database Interview Questions

**Q: Clustered vs Non-clustered index?**
> Clustered determines the physical order of rows (one per table, usually the primary key). Non-clustered is a separate structure with pointers to rows (many allowed).

**Q: DELETE vs TRUNCATE vs DROP?**
> DELETE removes rows (can filter with WHERE, logged, rollback-able). TRUNCATE removes all rows fast (no WHERE, minimal logging). DROP removes the entire table structure.

**Q: How do you optimize a slow query?**
> Analyze the execution plan (EXPLAIN), add appropriate indexes, avoid SELECT *, reduce joins, use pagination, check for full table scans, denormalize if needed.

**Q: What is a deadlock in a database?**
> Two transactions each hold a lock the other needs. The DB detects it and kills one transaction. Prevent by accessing tables in consistent order, keeping transactions short.

**Q: Primary key vs Unique key?**
> Primary key: one per table, not null, uniquely identifies rows. Unique key: multiple allowed, can have one null (in most DBs), enforces uniqueness.

**Q: What is connection pooling?**
> Reusing DB connections instead of opening/closing per request. HikariCP is the default in Spring Boot. Reduces latency and resource usage.
