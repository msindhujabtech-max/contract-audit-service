# Oracle ATG Web Commerce — Interview Preparation

This is your deepest expertise (10+ years across Boeing, Rogers/Vodafone, Nike, AEO/Express). Expect detailed questions.

## What is Oracle ATG?

Oracle ATG Web Commerce (Art Technology Group) is an enterprise e-commerce platform built on Java. It provides a component-based framework (Nucleus) for building scalable online commerce applications.

---

## Core ATG Modules (on your resume)

| Module | Full Name | Purpose |
|--------|-----------|---------|
| **DCS** | Dynamo Commerce Suite | Commerce features — catalog, cart, orders, pricing, promotions |
| **DPS** | Dynamo Personalization Server | User profiles, targeting, personalization |
| **DAS** | Dynamo Application Server | Core framework (Nucleus, repositories) |
| **ACC** | ATG Control Center | Desktop admin tool (legacy) |
| **BCC** | Business Control Center | Web-based business user tool (catalog, promotions, publishing) |
| **FUL** | Fulfillment | Order fulfillment, shipping |
| **REST** | ATG REST | RESTful web services layer |

**In plain English:** ATG is not one monolith — it is a stack of layered modules where each higher one depends on the one below it. *DAS* is the engine room (Nucleus and repositories); *DPS* adds "who is this user" (profiles and targeting); and *DCS* adds "what are they buying" (catalog, cart, orders). *BCC* and *ACC* are the admin tools business users log into, while *REST* exposes it all to modern front ends. When you start an ATG app you list these modules in the assembly order, cheapest layer first:

```bash
# Assemble an EAR that stacks the modules you need, in dependency order
runAssembler myStore.ear -m DAS DPS DCS BCC REST
```

So a storefront that shows personalized product recommendations touches DAS (framework) + DPS (profile targeting) + DCS (catalog) together, which is why they are almost always deployed as a set.

---

## Nucleus (the heart of ATG)

Nucleus is ATG's IoC container (like Spring's ApplicationContext). It manages components defined in `.properties` files.

### Component definition
```properties
# /atg/commerce/order/MyOrderTools.properties
$class=com.mycompany.commerce.order.MyOrderTools
$scope=global
orderManager=/atg/commerce/order/OrderManager
maxItems=100
```

### Scopes
- **global** — one instance (singleton)
- **session** — one per user session
- **request** — one per request
- **prototype** — new instance each time

> **Interview point:** "Nucleus is ATG's dependency injection container — components are wired via `.properties` files, similar to Spring beans but pre-dating Spring's popularity."

---

## Repositories (ATG's ORM)

ATG uses a repository abstraction over the database — the **GSA (Generic SQL Adapter)**.

### Repository concepts
- **Repository** — collection of item descriptors (like a database)
- **Item Descriptor** — defines an item type (like a table/entity)
- **RepositoryItem** — an instance (like a row)
- **Repository Definition File** — XML mapping items to DB tables

```xml
<!-- Repository definition -->
<item-descriptor name="product">
    <table name="dcs_product" type="primary" id-column-name="product_id">
        <property name="displayName" column-name="display_name" data-type="string"/>
        <property name="price" column-name="price" data-type="double"/>
    </table>
</item-descriptor>
```

### Querying a repository (RQL — Repository Query Language)
```java
RepositoryView view = repository.getView("product");
RqlStatement stmt = RqlStatement.parseRqlStatement("price > ?0");
Object[] params = { 100.0 };
RepositoryItem[] items = stmt.executeQuery(view, params);
```

### Standard commerce repositories
- **ProductCatalog** — products, categories, SKUs
- **OrderRepository** — orders, order items
- **ProfileAdapterRepository** — user profiles
- **PriceLists** — pricing
- **ClaimableRepository** — coupons/promotions

---

## Droplets (ATG's view components)

Droplets are reusable server-side components that generate dynamic content in JSP pages (like custom JSP tags).

### Out-of-the-box droplets
| Droplet | Purpose |
|---------|---------|
| **ForEach** | Iterate over a collection |
| **RQLQueryForEach** | Query repository and iterate |
| **Switch** | Conditional rendering |
| **Range** | Paginate a collection |
| **ItemLookupDroplet** | Look up an item by ID |
| **IsEmpty** | Check if collection/value is empty |

**In plain English:** These come with ATG, so you almost never write loops or `if`-statements in Java for display logic — you drop one of these tags into the JSP and it handles iteration, lookup, or conditional rendering for you. `ForEach` walks a collection you already have, `RQLQueryForEach` runs a repository query *and* loops the results, and `ItemLookupDroplet` fetches a single item by its ID.

For example, showing every SKU in a product uses the OOTB `ForEach` droplet — no custom Java needed:

```jsp
<dsp:droplet name="/atg/dynamo/droplet/ForEach">
    <dsp:param name="array" param="product.childSKUs"/>
    <dsp:oparam name="output">
        SKU: <dsp:valueof param="element.displayName"/><br/>
    </dsp:oparam>
    <dsp:oparam name="empty">No SKUs available.</dsp:oparam>
</dsp:droplet>
```

Here `array` is the input, the `output` oparam renders once per element (exposed as `element`), and the `empty` oparam covers the no-results case — the same pattern you would otherwise hand-code.

### Custom droplet
```java
public class DiscountDroplet extends DynamoServlet {
    public void service(DynamoHttpServletRequest req,
                        DynamoHttpServletResponse res)
            throws ServletException, IOException {
        // Read input parameter
        Double price = (Double) req.getObjectParameter("price");

        // Business logic
        double discounted = price * 0.9;

        // Set output parameter
        req.setParameter("discountedPrice", discounted);

        // Render the "output" oparam
        req.serviceLocalParameter("output", req, res);
    }
}
```
```jsp
<dsp:droplet name="DiscountDroplet">
    <dsp:param name="price" value="100"/>
    <dsp:oparam name="output">
        Discounted: <dsp:valueof param="discountedPrice"/>
    </dsp:oparam>
</dsp:droplet>
```

---

## Form Handlers (ATG's form processing)

Handle form submissions, validation, and actions. Session or request scoped.

### Common form handlers
- **ProfileFormHandler** — login, registration, profile update
- **CartModifierFormHandler** — add/remove/update cart items
- **ShippingGroupFormHandler** — shipping details
- **PaymentGroupFormHandler** — payment details
- **ExpressCheckoutFormHandler** — one-page checkout

### Custom form handler
```java
public class ContactFormHandler extends GenericFormHandler {
    private String email;
    // getters/setters

    public boolean handleSubmit(DynamoHttpServletRequest req,
                               DynamoHttpServletResponse res) {
        if (email == null || !email.contains("@")) {
            addFormException(new DropletFormException("Invalid email", "email"));
            return true;
        }
        // process...
        return true;
    }
}
```
```jsp
<dsp:form action="page.jsp" method="post">
    <dsp:input bean="ContactFormHandler.email" type="text"/>
    <dsp:input bean="ContactFormHandler.submit" type="submit" value="Send"/>
</dsp:form>
```

---

## Pipelines (ATG's chain of processors)

Pipelines process complex operations as a chain of processor stages. Heavily used in checkout/order processing.

### Common pipelines
- **commitOrder pipeline** — validates, processes payment, saves order
- **processOrder pipeline** — order processing steps
- **updateOrder / repriceOrder** — pricing chains

### Pipeline concept
```
Order submitted → [Validate] → [Reserve Inventory] → [Authorize Payment]
                → [Create Order] → [Send Confirmation]
Each is a "processor" that returns a code to continue or stop.
```

> **Your resume:** "Integrated custom payment managers and transaction groups by adding specialized processing chains and pipelines" — be ready to explain adding a custom processor to the commitOrder pipeline.

```java
public class InventoryCheckProcessor implements PipelineProcessor {
    public int runProcess(Object param, PipelineResult result) {
        Order order = (Order) param;
        // check inventory
        if (inStock) return SUCCESS;  // continue pipeline
        else { result.addError("OUT_OF_STOCK"); return STOP_CHAIN; }
    }
    public int[] getRetCodes() { return new int[]{SUCCESS, STOP_CHAIN}; }
}
```

---

## Scenarios & Slots (personalization — DPS)

- **Scenarios** — event-driven marketing rules (e.g., "if user adds >$100 to cart, show free shipping banner")
- **Slots** — placeholders on pages filled dynamically by scenarios/targeters
- **Targeters** — rules to show content based on profile

**In plain English:** These three work as a team to personalize a page without a developer redeploying code. A *slot* is an empty "content hole" a designer places on the page (say a hero banner spot). A *scenario* is an event-driven rule that reacts to what the shopper does. A *targeter* is the profile-based rule that decides which content actually fills the slot. So the slot is the *where*, the scenario is the *when*, and the targeter is the *for whom*.

For example, a free-shipping promotion wires up like this:

```
Slot:      "homepageBannerSlot"  (placed on the home page by BCC)
Scenario:  WHEN shopper adds items AND cartTotal > $100
           THEN add "FreeShippingBanner" content item to homepageBannerSlot
Targeter:  Only show it if profile.securityStatus = "logged in"
```

A business user configures all of this in the BCC — the shopper crossing $100 triggers the scenario, which pushes the banner into the slot, subject to the targeter's profile check.

---

## Commerce Concepts

### Order state machine
```
INCOMPLETE → SUBMITTED → PROCESSING → SHIPPED → COMPLETE
```

### Key commerce objects
- **Order** — contains commerce items, shipping groups, payment groups
- **CommerceItem** — a product in the cart
- **ShippingGroup** — how/where items ship
- **PaymentGroup** — how the order is paid
- **PricingEngine** — calculates prices, applies promotions

### Promotions & Pricing
- **PMDL (Promotion Markup Language)** — defines discount rules
- **Qualifier** — conditions for a promotion
- **Pricing calculators** — item, order, shipping, tax

**In plain English:** A promotion in ATG is really two parts: a *qualifier* (the "if" — who or what earns the discount) and a discount rule (the "then" — what changes). *PMDL* is the little rule language business users build in the BCC to express both without writing Java. The *PricingEngine* then runs a series of *pricing calculators* in order — item price, then order-level discounts, then shipping, then tax — each one adjusting the running total.

For example, a "buy 2, get 10% off" promotion reads like this in PMDL terms:

```
Qualifier (if):   order contains >= 2 items in category "Shoes"
Discount (then):  apply 10% off the qualifying items
```

At checkout the PricingEngine fires the item calculator first (applies the 10% to matching shoes), then the order calculator (any cart-wide coupons), then shipping and tax calculators — so the order in which calculators run directly determines the final price the customer sees.

---

## ATG REST (your resource — REST integration)

ATG's REST layer exposes commerce functionality as RESTful services (ATG 11+ REST MVC, or older ReST module).
```
GET  /rest/model/atg/commerce/order/OrderHolder
POST /rest/model/atg/commerce/order/purchase/CartModifierActor/addItemToOrder
```
> **Your resume:** "Built from scratch end-to-end ATG REST Web Services within the DCS module to completely handle shopping cart services."

---

## ATG + Endeca (your Photon projects)

Endeca is Oracle's search & guided navigation engine, integrated with ATG for:
- Faceted search / guided navigation
- Search results, auto-suggest
- **Cartridges** — reusable UI/content modules
- **MDEX engine** — the search index

> **Your resume:** "Built page-specific e-commerce templates using Oracle Endeca cartridges."

---

## ATG + Elasticsearch (your Nike project)

You replaced/augmented search with Elasticsearch + Drools:
> "Introduced an Elasticsearch service module to re-architect application search with search rules using the Drools analytics engine."

- **Elasticsearch** — distributed search engine (inverted index)
- **Drools** — business rules engine (search boosting rules, business logic)

---

## Common ATG Interview Questions

**Q: What is Nucleus?**
> ATG's dependency injection container. It instantiates and wires components defined in `.properties` files, managing their lifecycle and scope — conceptually similar to Spring's IoC container.

**Q: Droplet vs Form Handler?**
> A droplet generates dynamic content for display (view logic). A form handler processes form submissions and user actions (controller logic).

**Q: How does an ATG repository work?**
> The GSA (Generic SQL Adapter) maps repository items to database tables via XML definition files. You query using RQL, and ATG handles caching and SQL generation.

**Q: How do you extend out-of-the-box ATG functionality?**
> Extend the OOTB component class, override methods, and point the component's `$class` to your subclass in the `.properties` file. Layer configuration using CONFIGPATH.

**Q: What is the commitOrder pipeline?**
> A chain of processors that runs when an order is submitted — validating the order, authorizing payment, updating inventory, and persisting the order. You can insert custom processors.

**Q: How does ATG caching work?**
> Repositories cache items (item cache, query cache). Cache modes: simple, locked, distributed. Proper cache invalidation is critical for consistency in a cluster.

**Q: How do you handle a cluster in ATG?**
> Multiple ATG instances share a database. Use distributed caching (GSA invalidation via JMS), session replication, and a load balancer. Lock manager coordinates writes.

**Q: What versions of ATG have you worked with?**
> ATG 11.3.1 (per your resume) — mention the shift toward Oracle Commerce Cloud (OCC) and modern platforms like Commerce Tools for modernization context.
