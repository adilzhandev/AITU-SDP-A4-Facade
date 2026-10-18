# Assignment 4 — Facade Pattern

A Java 17 implementation of Option B: Online Order Processing. The project uses
only the Java standard library.

## Run

With JDK 17 and Make installed:

```sh
make run
make test
```

Without Make, run these commands from the project root:

```sh
mkdir -p build/classes
find src/main/java -name '*.java' > build/sources.txt
javac --release 17 -d build/classes @build/sources.txt
java -cp build/classes shop.Main
```

The terminal commands above use a POSIX shell. In IntelliJ IDEA, open the project,
select JDK 17, mark `src/main/java` as Sources Root and `src/test/java` as Test
Sources Root, then run `shop.Main` or `shop.OrderFacadeTest`.

## Design

`Main` creates and calls only `OrderFacade`, using `Customer` and `Order` as data
objects. The facade exposes two operations: `placeOrder(cart, customer)` and
`cancelOrder(orderId)`.

| Subsystem | Responsibility |
| --- | --- |
| `InventoryService` | Validate carts, calculate totals, reserve and release stock |
| `PaymentProcessor` | Debit customer balances and refund recorded payments |
| `ShippingService` | Register delivery addresses and cancel shipments |
| `NotificationService` | Validate and store customer notification messages |

An order reserves stock, takes payment, registers a shipment, and records a
notification. If payment, shipping, or notification fails, the facade cancels the
shipment, refunds the payment, and releases the stock. The order is stored only
after all steps succeed. Cancellation restores the stock and balance; repeated
cancellation has no additional effect.

The public constructor accepts product prices, stock quantities, and customer
balances. Amounts are stored as whole cents. The subsystem classes are public,
can be used directly, and have no reference to the facade.

## Demo

The console demonstration places an order, cancels it, rejects a payment with
insufficient funds, and successfully buys the same item afterward. The last step
requires both cancellation and payment rollback to have restored the state.

Payments, shipments, and notifications are simulated in memory. No real payment,
delivery, or email is sent. Data is reset on each run. The example is intended
for a single-threaded console application.

## Tests

`make test` runs 12 checks for successful orders, payment/shipping/notification
failure rollback, atomic reservations, cancellation, invalid inputs, immutable
cart snapshots, and direct use of the subsystems. A failed check exits with an
error; no external test framework is required.

## Structure

```text
src/main/java/shop/
  Main.java
  OrderFacade.java
  model/
    Customer.java
    Order.java
  service/
    InventoryService.java
    PaymentProcessor.java
    ShippingService.java
    NotificationService.java
src/test/java/shop/
  OrderFacadeTest.java
```

## Report and UML

- [Report in PDF](docs/Assignment_4_Facade_Report.pdf)
- [Editable Word report](docs/Assignment_4_Facade_Report.docx)
- [UML class diagram in PNG](docs/uml-class-diagram.png)
- [UML class diagram in SVG](docs/uml-class-diagram.svg)
- [Editable PlantUML source](docs/uml-class-diagram.puml)

The report covers the design, six Clean Code principles with annotated source
excerpts, test results, and the responsibilities hidden by the facade. The UML
shows only class names and their relationships, including the client, facade,
four services, and data records. Fields and methods are omitted for readability.
