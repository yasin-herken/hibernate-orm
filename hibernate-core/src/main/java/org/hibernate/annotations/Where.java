/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a restriction written in native SQL to add to the generated
 * SQL when querying an entity or collection.
 * <p>
 * For example, {@code @Where} could be used to hide entity instances which
 * have been soft-deleted, either for the entity class itself:
 * <pre>{@code
 * @Entity
 * @Where(clause = "status <> 'DELETED'")
 * class Document {
 *     ...
 *     @Enumerated(STRING)
 *     Status status;
 *     ...
 * }
 * }</pre>
 * or, at the level of an association to the entity:
 * <pre>{@code
 * @OneToMany(mappedBy = "owner")
 * @Where(clause = "status <> 'DELETED'")
 * List<Document> documents;
 * }</pre>
 * By default, {@code @Where} restrictions declared for an entity are not
 * applied when loading a collection of that entity type. This behavior is
 * controlled by:
 * <ol>
 * <li>the annotation member {@link #applyInToManyFetch()}, and
 * <li>the configuration property
 *     {@value org.hibernate.cfg.AvailableSettings#USE_ENTITY_WHERE_CLAUSE_FOR_COLLECTIONS}.
 * </ol>
 * Note that {@code @Where} restrictions are always applied and cannot be
 * disabled. Nor may they be parameterized. They're therefore <em>much</em>
 * less flexible than {@linkplain Filter filters}.
 *
 * @see Filter
 * @see DialectOverride.Where
 * @see org.hibernate.cfg.AvailableSettings#USE_ENTITY_WHERE_CLAUSE_FOR_COLLECTIONS
 *
 * @author Emmanuel Bernard
 */
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface Where {
	/**
	 * A predicate, written in native SQL.
	 */
	String clause();

	/**
	 * If this restriction applies to an entity type, should it also be
	 * applied when fetching a {@link jakarta.persistence.OneToMany} or
	 * {@link jakarta.persistence.ManyToOne} association that targets
	 * the entity type?
	 * <p>
	 * By default, the restriction is not applied unless the property
	 * {@value org.hibernate.cfg.AvailableSettings#USE_ENTITY_WHERE_CLAUSE_FOR_COLLECTIONS}
	 * is explicitly enabled.
	 *
	 * @return {@code true} if the restriction should be applied even
	 *         if the configuration property is not enabled
	 *
	 * @since 6.2
	 */
	boolean applyInToManyFetch() default false;
}
