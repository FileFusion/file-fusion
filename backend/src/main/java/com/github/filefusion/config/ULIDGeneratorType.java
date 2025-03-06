package com.github.filefusion.config;

import com.github.filefusion.util.ULID;
import org.hibernate.annotations.IdGeneratorType;
import org.hibernate.annotations.ValueGenerationType;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.EnumSet;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.generator.EventTypeSets.INSERT_ONLY;

/**
 * ULIDGeneratorType
 *
 * @author hackyo
 * @since 2022/4/1
 */
public class ULIDGeneratorType implements BeforeExecutionGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        return ULID.randomULID();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return INSERT_ONLY;
    }

    @IdGeneratorType(ULIDGeneratorType.class)
    @ValueGenerationType(generatedBy = ULIDGeneratorType.class)
    @Retention(RUNTIME)
    @Target({FIELD, METHOD})
    public @interface ULIDGenerator {
    }

}
