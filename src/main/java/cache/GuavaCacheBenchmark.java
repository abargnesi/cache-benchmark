package cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Fork(value = 2, warmups = 1)
public class GuavaCacheBenchmark {

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void store(EmptyGuavaCacheState state) {
		final String key = state.preallocated[EmptyGuavaCacheState.rand2.nextInt(EmptyGuavaCacheState.MAX_SIZE)];
		state.cache.put(key, Boolean.TRUE);
	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Boolean storeThenRetrieve(EmptyGuavaCacheState state) {
		final String key = state.preallocated[EmptyGuavaCacheState.rand3.nextInt(EmptyGuavaCacheState.MAX_SIZE)];
		state.cache.put(key, Boolean.TRUE);
		return state.cache.getIfPresent(key);
	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Boolean retrieve(ReadGuavaCacheState state) {
		final String key = state.preallocated[EmptyGuavaCacheState.rand1.nextInt(EmptyGuavaCacheState.MAX_SIZE)];
		return state.cache.getIfPresent(key);
	}

	@Benchmark
	@BenchmarkMode(Mode.All)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Boolean retrieveMissing(ReadGuavaCacheState state) {
		return state.cache.getIfPresent("This_Key_Is_Missing");
	}

//	@Benchmark
//	@BenchmarkMode(Mode.All)
//	@OutputTimeUnit(TimeUnit.MILLISECONDS)
//	public long e_size(EmptyGuavaCacheState state) {
//		return state.cache.size();
//	}

	@State(value = Scope.Benchmark)
	public static class ReadGuavaCacheState extends EmptyGuavaCacheState {

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
	public static class EmptyGuavaCacheState {

		static final Random rand1     = new Random(1001001L);
		static final Random rand2     = new Random(10101010101L);
		static final Random rand3     = new Random(10101010101010101L);

		static final int MAX_SIZE     = 5_000_000;

		Cache<String, Boolean> cache;
		String[] preallocated = new String[MAX_SIZE];

		@Setup
		public void createCache() {
			System.out.println(format("Preallocate %d strings.", MAX_SIZE));
			cache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE).build();
			for (int i = 0; i < MAX_SIZE; i++) {
				final String key = RandomStringUtils.random(60, true, true);
				preallocated[i] = key;
			}
		}

		@TearDown
		public void clearCache() {
			System.out.println(format("Clearing cache, size at %d strings.", cache.size()));
			cache.invalidateAll();
			System.out.println(format("Cleared cache, size at %d strings.", cache.size()));
		}
	}
}
