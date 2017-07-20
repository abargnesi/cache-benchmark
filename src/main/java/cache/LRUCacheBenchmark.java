package cache;

import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Fork(value = 5, warmups = 2)
public class LRUCacheBenchmark {

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void store(LRUState state) {
		final String key = state.preallocated[LRUState.rand2.nextInt(LRUState.MAX_SIZE)];
		state.cache.put(key, Boolean.TRUE);
	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Boolean storeThenRetrieve(LRUState state) {
		final String key = state.preallocated[LRUState.rand3.nextInt(LRUState.MAX_SIZE)];
		state.cache.put(key, Boolean.TRUE);
		return state.cache.get(key);
	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Boolean retrieve(ReadLRUState state) {
		final String key = state.preallocated[LRUState.rand1.nextInt(LRUState.MAX_SIZE)];
		return state.cache.get(key);
	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Boolean retrieveMissing(ReadLRUState state) {
		return state.cache.get("This_Key_Is_Missing");
	}

//	@Benchmark
//	@BenchmarkMode(Mode.All)
//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	public int e_size(LRUState state) {
//		return state.cache.usedEntries();
//	}

	@State(value = Scope.Benchmark)
	public static class ReadLRUState extends LRUState {

		@Override
		public void createCache() {
			super.createCache();

			System.out.println(format("Preloading %d entries into map.", MAX_SIZE));
			for (int i = 0; i < MAX_SIZE; i++) {
				cache.put(preallocated[i], Boolean.TRUE);
			}
		}
	}

	@State(value = Scope.Benchmark)
	public static class LRUState {

		static final Random rand1     = new Random(1001001L);
		static final Random rand2     = new Random(10101010101L);
		static final Random rand3     = new Random(10101010101010101L);
		static final int MAX_SIZE     = 5_000_000;

		LRUCache<String, Boolean> cache;
		String[] preallocated = new String[MAX_SIZE];

		@Setup
		public void createCache() {
			System.out.println(format("Preallocate %d strings.", MAX_SIZE));
			cache = new LRUCache<>(MAX_SIZE, false);
			for (int i = 0; i < MAX_SIZE; i++) {
				final String key = RandomStringUtils.random(60, true, true);
				preallocated[i] = key;
			}
		}

		@TearDown
		public void clearCache() {
			System.out.println(format("Clearing cache, size at %d strings.", cache.usedEntries()));
			cache.clear();
			System.out.println(format("Cleared cache, size at %d strings.", cache.usedEntries()));
		}
	}
}
