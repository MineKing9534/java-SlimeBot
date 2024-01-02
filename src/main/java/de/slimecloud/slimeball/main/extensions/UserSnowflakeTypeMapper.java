package de.slimecloud.slimeball.main.extensions;

import de.mineking.javautils.database.DatabaseManager;
import de.mineking.javautils.database.TypeMapper;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.statement.StatementContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class UserSnowflakeTypeMapper implements TypeMapper<Long, UserSnowflake> {
	@Override
	public boolean accepts(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field) {
		return UserSnowflake.class.isAssignableFrom(type);
	}

	@NotNull
	@Override
	public String getType(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field) {
		return "bigint";
	}

	@NotNull
	@Override
	public Argument createArgument(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable UserSnowflake value) {
		return new Argument() {
			@Override
			public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
				statement.setLong(position, value == null ? null : value.getIdLong());
			}

			@Override
			public String toString() {
				return Objects.toString(value);
			}
		};
	}

	@NotNull
	@Override
	public String string(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field f, @Nullable UserSnowflake value) {
		return value == null ? "null" : value.getId();
	}

	@Nullable
	@Override
	public Long extract(@NotNull ResultSet set, @NotNull String name, @NotNull Class<?> target) throws SQLException {
		return set.getLong(name);
	}

	@Nullable
	@Override
	public UserSnowflake parse(@NotNull DatabaseManager manager, @NotNull Class<?> type, @NotNull Field field, @Nullable Long value) {
		return value == null ? null : UserSnowflake.fromId(value);
	}
}
