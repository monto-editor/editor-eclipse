package de.tudarmstadt.stg.monto.message;

import java.util.function.Function;

public interface Dependency {
	public <A> A match(Function<VersionDependency,A> v, Function<ProductDependency,A> p);	
}
