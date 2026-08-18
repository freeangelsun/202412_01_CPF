package io.swagger.v3.oas.annotations.media;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE,ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
public @interface Schema {
    String name() default ""; String description() default ""; RequiredMode requiredMode() default RequiredMode.AUTO;
    enum RequiredMode { AUTO, REQUIRED, NOT_REQUIRED }
}
