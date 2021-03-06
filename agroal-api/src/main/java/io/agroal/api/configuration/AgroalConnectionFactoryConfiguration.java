// Copyright (C) 2017 Red Hat, Inc. and individual contributors as indicated by the @author tags.
// You may not use this file except in compliance with the Apache License, Version 2.0.

package io.agroal.api.configuration;

import java.security.Principal;
import java.sql.Connection;
import java.util.Collection;
import java.util.Properties;

/**
 * @author <a href="lbarreiro@redhat.com">Luis Barreiro</a>
 */
public interface AgroalConnectionFactoryConfiguration {

    boolean autoCommit();

    String jdbcUrl();

    String initialSql();

    String driverClassName();

    ClassLoaderProvider classLoaderProvider();

    TransactionIsolation jdbcTransactionIsolation();

    @Deprecated
    InterruptProtection interruptProtection();

    Principal principal();

    Collection<Object> credentials();

    Properties jdbcProperties();

    // --- //

    enum TransactionIsolation {
        UNDEFINED, NONE, READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE;

        public int level() {
            switch ( this ) {
                case READ_UNCOMMITTED:
                    return Connection.TRANSACTION_READ_UNCOMMITTED;
                case READ_COMMITTED:
                    return Connection.TRANSACTION_READ_COMMITTED;
                case REPEATABLE_READ:
                    return Connection.TRANSACTION_REPEATABLE_READ;
                case SERIALIZABLE:
                    return Connection.TRANSACTION_SERIALIZABLE;
                case NONE:
                    return Connection.TRANSACTION_NONE;
                default:
                    return -1;
            }
        }

        public boolean isDefined() {
            return this != UNDEFINED;
        }

    }
}
