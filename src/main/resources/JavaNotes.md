## Java notes
1. Functional Interfaces - Interfaces with 1 abstract method. It can have other default and concrete methods.e.g., There are 2 ways to add implementations for such interfaces using concrete classes and using lambdas
```java
@FunctionalInterface
public interface TransactionMedium {
    void pay(TransactionDetail transactionDetail, OrderDetails orderDetail);
}

public class DebitCard implements TransactionMedium {
    void pay(TransactionDetail transactionDetail, OrderDetails orderDetail) {
        // actual implementation
    }
}

public class ExampleClass {
    public static void main(String[] args) {
        
        // Approach 1 (using implements)
        TransactionMedium debitCard = new DebitCard();
        debitCard.pay(transactionDetail,orderDetail);
        
        // Approach 2 - Without Lambdas, anonymous class implementation
        TransactionMedium transactionMedium = new TransactionMedium() {
            @Override
            public void pay(TransactionDetail transactionDetail, OrderDetails orderDetail) {
                // actual implementation
            }
        };
        
        // Approach 3 - With Lambdas (least verbose)
        TransactionMedium transactionMedium = (transactionDetail, orderDetail) -> {
            // actual implementation
        };
        
        // usage
        transactionMedium.pay(transactionDetail,orderDetail);
    }
}
```
2. Types of in-built functional interface ->
   - Consumer<T> : void accept(T t) : accepts a parameter but produces nothing
   - Supplier<T> : T get() : accepts nothing but produces a result
   - Function<T,R> : R apply(T t) : accepts T and returns R
   - Predicate<T> : boolean test(T t) : accepts T and returns boolean
```java
import java.util.UUID;
import java.util.function.Supplier;

public class ExampleClass {
    public static void main(String[] args) {
        Consumer<String> checkIfNumberOrName = (String key) -> {
            if (key.chars().allMatch(Character::isLetter)) {
                System.out.println("This is a name");
            } else {
                System.out.println("This is a number");
            }
        };

        // usage
        checkIfNumberOrName.accept("Hello");
        checkIfNumberOrName.accept("12345");

        Supplier<String> generateRandomNumber = () -> UUID.randomUUID().toString();
        
        // usage
        System.out.println(generateRandomNumber());
    }
}
```