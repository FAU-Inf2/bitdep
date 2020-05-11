package synth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviders;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import smt.BitVector;
import smt.Builder;
import smt.BVAst;
import smt.BVConst;



@RunWith(DataProviderRunner.class)
public class SynthesizerTest {

	private static final int TEST_REPETITIONS = 100;




	private static Object[][] synthesizers() {
		return new Object[][] {
			{ "GJTV", new ListBasedSynthesizer() }
		};
	}



	private static Object[][] fromTestCases(final List<TestCase> testCases) {
		final Object[][] result = new Object[testCases.size()][3];
		for (int i = 0; i < testCases.size(); ++i) {
			result[i][0] = testCases.get(i).getName();
			result[i][1] = testCases.get(i).getSpecification();
			result[i][2] = testCases.get(i).getLibrary();
		}
		return result;
	}



	@DataProvider(format = "%p[2] with %p[0]")
	public static Object[][] fastSat() {
		return DataProviders.crossProduct(
				synthesizers(),
				fromTestCases(TestData.getFastSatTestCases()));
	}



	@DataProvider(format = "%p[2] with %p[0]")
	public static Object[][] fastUnsat() {
		return DataProviders.crossProduct(
				synthesizers(),
				fromTestCases(TestData.getFastUnsatTestCases()));
	}




	@Test(timeout = 15000)
	@UseDataProvider("fastSat")
	public void testFastSatTestCases(final String algorithmName, final Synthesizer synthesizer,
			final String problemName, final Specification specification,
			final Library library) throws TimeoutException {

		final Optional<Program> result = synthesizer.synthesizeProgram(specification, library);

		assertTrue(algorithmName + " could not find a solution for " + problemName,
				result.isPresent());

		final Random rng = new Random();
		for (int i = 0; i < TEST_REPETITIONS; ++i) {
			final List<BitVector> arguments = new ArrayList<>();
			final List<BVAst> specArguments = new ArrayList<>();
			for (int j = 0; j < specification.getNumberOfInputs(); ++j) {
				final int value = rng.nextInt();
				arguments.add(new BitVector(32, value));
				specArguments.add(Builder.mkBVConst(32, value));
			}

			assertEquals("Program " + result.get() + " produced by " + algorithmName + " for " + problemName + " failed "
						+ "for arguments " + arguments,
					specification.getFunction().apply(specArguments).eval(Collections.emptyMap()),
					result.get().execute(arguments));
		}
	}




	@Test(timeout = 15000)
	@UseDataProvider("fastUnsat")
	public void testFastUnsatTestCases(final String algorithmName, final Synthesizer synthesizer,
			final String problemName, final Specification specification,
			final Library library) throws TimeoutException {

		assertFalse(algorithmName + " found a solution for unsatisfiable problem " + problemName,
				synthesizer.synthesizeProgram(specification, library).isPresent());
	}
}

