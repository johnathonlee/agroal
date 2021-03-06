// Copyright (C) 2017 Red Hat, Inc. and individual contributors as indicated by the @author tags.
// You may not use this file except in compliance with the Apache License, Version 2.0.

package io.agroal.api.configuration.supplier;

import io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation;
import io.agroal.api.configuration.AgroalConnectionPoolConfiguration.PreFillMode;
import io.agroal.api.configuration.AgroalDataSourceConfiguration;
import io.agroal.api.configuration.AgroalDataSourceConfiguration.DataSourceImplementation;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="lbarreiro@redhat.com">Luis Barreiro</a>
 */
public class AgroalPropertiesReader implements Supplier<AgroalDataSourceConfiguration> {

    public static final String IMPLEMENTATION = "implementation";
    public static final String JNDI_NAME = "jndiName";
    public static final String METRICS_ENABLED = "metricsEnabled";
    public static final String XA = "xa";

    // --- //

    public static final String MIN_SIZE = "minSize";
    public static final String MAX_SIZE = "maxSize";
    public static final String INITIAL_SIZE = "initialSize";
    public static final String PRE_FILL_MODE = "preFillMode";
    public static final String ACQUISITION_TIMEOUT = "acquisitionTimeout";
    public static final String ACQUISITION_TIMEOUT_MS = "acquisitionTimeout_ms";
    public static final String ACQUISITION_TIMEOUT_S = "acquisitionTimeout_s";
    public static final String ACQUISITION_TIMEOUT_M = "acquisitionTimeout_m";
    public static final String VALIDATION_TIMEOUT = "validationTimeout";
    public static final String VALIDATION_TIMEOUT_MS = "validationTimeout_ms";
    public static final String VALIDATION_TIMEOUT_S = "validationTimeout_s";
    public static final String VALIDATION_TIMEOUT_M = "validationTimeout_m";
    public static final String LEAK_TIMEOUT = "leakTimeout";
    public static final String LEAK_TIMEOUT_MS = "leakTimeout_ms";
    public static final String LEAK_TIMEOUT_S = "leakTimeout_s";
    public static final String LEAK_TIMEOUT_M = "leakTimeout_m";
    public static final String REAP_TIMEOUT = "reapTimeout";
    public static final String REAP_TIMEOUT_MS = "reapTimeout_ms";
    public static final String REAP_TIMEOUT_S = "reapTimeout_s";
    public static final String REAP_TIMEOUT_M = "reapTimeout_m";

    // --- //

    public static final String JDBC_URL = "jdbcUrl";
    public static final String AUTO_COMMIT = "autoCommit";
    public static final String INITIAL_SQL = "initialSQL";
    public static final String DRIVER_CLASS_NAME = "driverClassName";
    public static final String TRANSACTION_ISOLATION = "jdbcTransactionIsolation";
    public static final String PRINCIPAL = "principal";
    public static final String CREDENTIAL = "credential";
    public static final String JDBC_PROPERTIES = "jdbcProperties";

    // --- //

    private final String prefix;

    private final AgroalDataSourceConfigurationSupplier dataSourceSupplier;

    public AgroalPropertiesReader() {
        this( "" );
    }

    public AgroalPropertiesReader(String prefix) {
        this.prefix = prefix;
        this.dataSourceSupplier = new AgroalDataSourceConfigurationSupplier();
    }

    @Override
    public AgroalDataSourceConfiguration get() {
        return dataSourceSupplier.get();
    }

    public AgroalDataSourceConfigurationSupplier modify() {
        return dataSourceSupplier;
    }

    // --- //

    public AgroalPropertiesReader readProperties(Path path) throws IOException {
        return readProperties( path.toFile() );
    }

    public AgroalPropertiesReader readProperties(String filename) throws IOException {
        return readProperties( new File( filename ) );
    }

    public AgroalPropertiesReader readProperties(File file) throws IOException {
        try ( InputStream inputStream = new FileInputStream( file ) ) {
            Properties properties = new Properties();
            properties.load( inputStream );
            return readProperties( properties );
        }
    }

    @SuppressWarnings( "unchecked" )
    public AgroalPropertiesReader readProperties(Properties properties) {
        return readProperties( (Map) properties );
    }

    public AgroalPropertiesReader readProperties(Map<String, String> properties) {
        AgroalConnectionPoolConfigurationSupplier connectionPoolSupplier = new AgroalConnectionPoolConfigurationSupplier();
        AgroalConnectionFactoryConfigurationSupplier connectionFactorySupplier = new AgroalConnectionFactoryConfigurationSupplier();

        apply( dataSourceSupplier::dataSourceImplementation, DataSourceImplementation::valueOf, properties, IMPLEMENTATION );
        apply( dataSourceSupplier::jndiName, Function.identity(), properties, JNDI_NAME );
        apply( dataSourceSupplier::metricsEnabled, Boolean::parseBoolean, properties, METRICS_ENABLED );
        apply( dataSourceSupplier::xa, Boolean::parseBoolean, properties, XA );

        apply( connectionPoolSupplier::minSize, Integer::parseInt, properties, MIN_SIZE );
        apply( connectionPoolSupplier::maxSize, Integer::parseInt, properties, MAX_SIZE );
        apply( connectionPoolSupplier::initialSize, Integer::parseInt, properties, INITIAL_SIZE );
        apply( connectionPoolSupplier::preFillMode, PreFillMode::valueOf, properties, PRE_FILL_MODE );

        apply( connectionPoolSupplier::acquisitionTimeout, Duration::parse, properties, ACQUISITION_TIMEOUT );
        apply( connectionPoolSupplier::acquisitionTimeout, this::parseDurationMs, properties, ACQUISITION_TIMEOUT_MS );
        apply( connectionPoolSupplier::acquisitionTimeout, this::parseDurationS, properties, ACQUISITION_TIMEOUT_S );
        apply( connectionPoolSupplier::acquisitionTimeout, this::parseDurationM, properties, ACQUISITION_TIMEOUT_M );
        apply( connectionPoolSupplier::validationTimeout, Duration::parse, properties, VALIDATION_TIMEOUT );

        apply( connectionPoolSupplier::validationTimeout, this::parseDurationMs, properties, VALIDATION_TIMEOUT_MS );
        apply( connectionPoolSupplier::validationTimeout, this::parseDurationS, properties, VALIDATION_TIMEOUT_S );
        apply( connectionPoolSupplier::validationTimeout, this::parseDurationM, properties, VALIDATION_TIMEOUT_M );
        apply( connectionPoolSupplier::leakTimeout, Duration::parse, properties, LEAK_TIMEOUT );

        apply( connectionPoolSupplier::leakTimeout, this::parseDurationMs, properties, LEAK_TIMEOUT_MS );
        apply( connectionPoolSupplier::leakTimeout, this::parseDurationS, properties, LEAK_TIMEOUT_S );
        apply( connectionPoolSupplier::leakTimeout, this::parseDurationM, properties, LEAK_TIMEOUT_M );
        apply( connectionPoolSupplier::reapTimeout, Duration::parse, properties, REAP_TIMEOUT );

        apply( connectionPoolSupplier::reapTimeout, this::parseDurationMs, properties, REAP_TIMEOUT_MS );
        apply( connectionPoolSupplier::reapTimeout, this::parseDurationS, properties, REAP_TIMEOUT_S );
        apply( connectionPoolSupplier::reapTimeout, this::parseDurationM, properties, REAP_TIMEOUT_M );

        apply( connectionFactorySupplier::jdbcUrl, Function.identity(), properties, JDBC_URL );
        apply( connectionFactorySupplier::autoCommit, Boolean::parseBoolean, properties, AUTO_COMMIT );
        apply( connectionFactorySupplier::initialSql, Function.identity(), properties, INITIAL_SQL );
        apply( connectionFactorySupplier::driverClassName, Function.identity(), properties, DRIVER_CLASS_NAME );
        apply( connectionFactorySupplier::jdbcTransactionIsolation, TransactionIsolation::valueOf, properties, TRANSACTION_ISOLATION );
        apply( connectionFactorySupplier::principal, NamePrincipal::new, properties, PRINCIPAL );
        apply( connectionFactorySupplier::credential, SimplePassword::new, properties, CREDENTIAL );
        applyJdbcProperties( connectionFactorySupplier::jdbcProperty, properties, JDBC_PROPERTIES );

        dataSourceSupplier.connectionPoolConfiguration( connectionPoolSupplier.connectionFactoryConfiguration( connectionFactorySupplier ) );
        return this;
    }

    private <T> void apply(Consumer<T> consumer, Function<String, T> converter, Map<String, String> properties, String key) {
        String value = properties.get( prefix + key );
        if ( value != null ) {
            consumer.accept( converter.apply( value ) );
        }
    }

    private void applyJdbcProperties(BiConsumer<String, String> consumer, Map<String, String> properties, String key) {
        String propertiesArray = properties.get( prefix + key );
        if ( propertiesArray != null && !propertiesArray.isEmpty() ) {
            for ( String property : propertiesArray.split( ";" ) ) {
                String[] keyValue = property.split( "=" );
                consumer.accept( keyValue[0], keyValue[1] );
            }
        }
    }

    private Duration parseDurationMs(String value) {
        return Duration.ofMillis( Long.parseLong( value ) );
    }

    private Duration parseDurationS(String value) {
        return Duration.ofSeconds( Long.parseLong( value ) );
    }

    private Duration parseDurationM(String value) {
        return Duration.ofMinutes( Long.parseLong( value ) );
    }
}
