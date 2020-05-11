package synth;

import java.util.List;



public class TestCase {

	private final String name;
	private final Specification spec;
	private final List<LibraryFunction> library;



	public TestCase(final String name, final Specification spec,
			final List<LibraryFunction> library) {
		this.name = name;
		this.spec = spec;
		this.library = library;
	}



	public String getName() {
		return this.name;
	}



	public Specification getSpecification() {
		return this.spec;
	}



	public List<LibraryFunction> getLibrary() {
		return this.library;
	}
}

