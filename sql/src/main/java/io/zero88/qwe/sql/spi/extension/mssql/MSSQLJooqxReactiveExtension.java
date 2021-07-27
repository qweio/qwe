package io.zero88.qwe.sql.spi.extension.mssql;

import io.vertx.mssqlclient.MSSQLPool;
import io.zero88.jooqx.spi.mssql.MSSQLErrorConverterProvider;
import io.zero88.jooqx.spi.mssql.MSSQLPoolProvider;
import io.zero88.qwe.sql.handler.JooqxExtension.JooqxReactiveExtension;

/**
 * QWE Jooqx reactive extension for {@code MSSQL}
 *
 * @see MSSQLPool
 * @see MSSQLPoolProvider
 * @see MSSQLErrorConverterProvider
 * @see JooqxReactiveExtension
 */
public class MSSQLJooqxReactiveExtension
    implements JooqxReactiveExtension<MSSQLPool>, MSSQLPoolProvider, MSSQLErrorConverterProvider {

    public static final MSSQLJooqxReactiveExtension INSTANCE = new MSSQLJooqxReactiveExtension();

}
