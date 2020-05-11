package yices;

import static org.junit.Assert.assertEquals;

import java.util.function.BinaryOperator;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import smt.Builder;
import smt.BVAst;
import smt.SatResult;



@RunWith(DataProviderRunner.class)
public class YicesTest {

	@DataProvider(format = "%i: %p[0]")
	public static Object[][] binaryOperators() {
		return new Object[][] {
			{ "add",  (BinaryOperator<BVAst>) Builder::mkAdd },
			{ "sub",  (BinaryOperator<BVAst>) Builder::mkSub },
			{ "mul",  (BinaryOperator<BVAst>) Builder::mkMul },
			{ "sdiv", (BinaryOperator<BVAst>) Builder::mkSDiv },
			{ "udiv", (BinaryOperator<BVAst>) Builder::mkUDiv },
			{ "srem", (BinaryOperator<BVAst>) Builder::mkSRem },
			{ "urem", (BinaryOperator<BVAst>) Builder::mkURem },
			{ "smod", (BinaryOperator<BVAst>) Builder::mkSMod },
			{ "umod", (BinaryOperator<BVAst>) Builder::mkUMod },
			{ "and",  (BinaryOperator<BVAst>) Builder::mkAnd },
			{ "or",   (BinaryOperator<BVAst>) Builder::mkOr },
			{ "xor",  (BinaryOperator<BVAst>) Builder::mkXor },
			{ "shl",  (BinaryOperator<BVAst>) Builder::mkShl },
			{ "ashr", (BinaryOperator<BVAst>) Builder::mkAshr },
			{ "lshr", (BinaryOperator<BVAst>) Builder::mkLshr }
		};
	}



	@Test(timeout = 1500)
	@UseDataProvider("binaryOperators")
	public void testBinaryOperators(final String operatorName,
			final BinaryOperator<BVAst> operator) {
		
		final BVAst varX = Builder.mkBVVar(32, "x");
		final BVAst val = Builder.mkBVConst(32, 410);
		final BVAst varR = Builder.mkBVVar(32, "r");

		final YicesSolver solver = new YicesSolver();
		solver.add(Builder.mkEq(operator.apply(varX, val), varR));

		assertEquals("Constraint should be satisfiable", SatResult.SAT, solver.checkSat());
	}
}

