package dev.tyoudm.assasin.check;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CheckInfo {
    String name();
    CheckType type();
    CheckCategory category();
    String description() default "";
    double maxVl() default 10.0;
    Severity severity() default Severity.MEDIUM;
    enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
}
