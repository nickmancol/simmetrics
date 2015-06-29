package org.simmetrics.metrics;

import static java.lang.String.format;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.simmetrics.utils.Math.max3;

import org.junit.Before;
import org.junit.Test;
import org.simmetrics.Metric;

public abstract class MetricTest<K> {

	protected static final class T<K> {
		protected final K a;
		protected final K b;
		protected final float similarity;

		public T(float similarity, K a, K b) {
			this.a = a;
			this.b = b;
			this.similarity = similarity;
		}
	}

	private static final float DEFAULT_DELTA = 0.0001f;
	private static <K> void testCoincidence(Metric<K> metric, K a, K b) {
		if (metric.compare(a, b) == 1.0f) {
			assertTrue(format("coincidence did not hold for %s and %s", a, b),
					a.equals(b));
		}
	}
	private static <K> void testNullPointerException(Metric<K> metric, K a, K b) {
		try {
			metric.compare(null, b);
			fail("Metric should have thrown a null pointer exception for the first argument");
		} catch (NullPointerException ignored) {
			// Ignored
		}

		try {
			metric.compare(a, null);
			fail("Metric should have thrown a null pointer exception for the second argument");
		} catch (NullPointerException ignored) {
			// Ignored
		}

		try {
			metric.compare(null, null);
			fail("Metric should have thrown a null pointer exception for either argument");
		} catch (NullPointerException ignored) {
			// Ignored
		}
	}
	private static <K> void testRange(Metric<K> metric, K a, K b) {
		float similarity = metric.compare(a, b);
		String message1 = String.format(
				"Similarity %s-%s %f must fall within [0.0 - 1.0] range", a, b,
				similarity);
		assertTrue(message1, 0.0f <= similarity && similarity <= 1.0f);
	}

	private static <K> void testReflexive(Metric<K> metric, K a, float delta) {
		assertEquals(1.0f, metric.compare(a, a), delta);
	}

	private static <K> void testSimilarity(Metric<K> metric, K a, K b,
			float expected, float delta) {
		float similarity = metric.compare(a, b);
		String message = String.format("\"%s\" vs \"%s\"", a, b);
		assertEquals(message, expected, similarity, delta);
	}

	private static <K> void testSubadditivity(Metric<K> metric, K a, K b, K c) {

		float ab = 1.0f - metric.compare(a, b);
		float ac = 1.0f - metric.compare(a, c);
		float bc = 1.0f - metric.compare(b, c);

		assertTrue(
				format("triangle ineqaulity must hold for %s, %s, %s with %s, %s, %s",
						a, b, c, ab, ac, bc), ab == 0.0f || ac == 0.0f
						|| bc == 0.0f || 2 * max3(ab, ac, bc) <= ab + ac + bc);
	}

	private static <K> void testSymmetric(Metric<K> metric, K a, K b,
			float delta) {
		float similarity = metric.compare(a, b);
		float similarityReversed = metric.compare(b, a);

		String message = String.format(
				"Similarity relation \"%s\" vs \"%s\" must be symmetric", a, b);
		assertEquals(message, similarityReversed, similarity, delta);

	}

	protected float delta;

	protected Metric<K> metric;

	protected T<K>[] tests;

	@Test
	public final void empty() {
		assertEquals(1.0f, metric.compare(getEmpty(), getEmpty()), delta);
	}

	protected float getDelta() {
		return DEFAULT_DELTA;
	}

	protected abstract K getEmpty();

	protected abstract Metric<K> getMetric();

	protected abstract T<K>[] getTests();
	
	protected boolean satisfiesCoincidence() {
		return true;
	}

	protected boolean satisfiesSubadditivity() {
		return true;
	}
	@Test
	public final void nullPointerException() {
		for (T<K> t : tests) {
			testNullPointerException(metric, t.a, t.b);
		}
	}

	@Test
	public final void optionalCoincidence() {
		if (!satisfiesCoincidence()) {
			return;
		}

		for (T<K> t : tests) {
			testCoincidence(metric, t.a, t.b);
		}
	}

	@Test
	public final void optionalSubadditivity() {
		if (!satisfiesSubadditivity()) {
			return;
		}

		for (T<K> n : tests) {
			for (T<K> m : tests) {
				testSubadditivity(metric, n.a, n.b, m.a);
				testSubadditivity(metric, n.a, n.b, m.b);
			}
		}
	}

	@Test
	public final void range() {
		for (T<K> t : tests) {
			testRange(metric, t.a, t.b);
		}
	}

	@Test
	public final void reflexive() {
		for (T<K> t : tests) {
			testReflexive(metric, t.a, delta);
			testReflexive(metric, t.b, delta);
		}
	}

	@Before
	public final void setUp() throws Exception {
		this.delta = getDelta();
		this.metric = getMetric();
		this.tests = getTests();
	}

	@Test
	public final void similarity() {
		for (T<K> t : tests) {
			testSimilarity(metric, t.a, t.b, t.similarity, delta);
		}
	}

	@Test
	public final void symmetric() {
		for (T<K> t : tests) {
			testSymmetric(metric, t.a, t.b, delta);
		}
	}

	@Test
	public final void implementsToString() {

		String metricToString = metric.toString();
		String defaultToString = metric.getClass().getName() + "@" + Integer.toHexString(metric.hashCode());

		assertFalse(
				"toString() was not implemented "
						+ metric.toString(), defaultToString.equals(metricToString));

		String metricName = metric.getClass().getSimpleName();

		assertTrue(format("%s must contain %s ", metricToString,
				metricName), metricToString.contains(metricName));
	}

}
