/*
 * This file is part of bitdep.
 *
 * Copyright (c) 2020 Lehrstuhl fuer Informatik 2,
 * Friedrich-Alexander-Universität Erlangen-Nürnberg (FAU)
 *
 * bitdep is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bitdep is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with bitdep.  If not, see <http://www.gnu.org/licenses/>.
 */
package synth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Pattern;

import smt.BitVector;
import smt.BoolAst;
import smt.BVAst;
import smt.BVConst;



public class SynthesizerShell {

	private static class ShellState {
		Library library;
		Specification specification;
		List<BitVector> inputs;
		SynthesizerSettings settings;
		boolean logRunTime;
		boolean printStatistics;
		int bitWidth = 32;


		ShellState() {
			this.settings = SynthesizerSettings.getDefault();
			this.logRunTime = false;
			this.printStatistics = false;
		}
	}



	private static interface Command {
		boolean execute(ShellState state);
	}



	private static class HelpCommand implements Command {
		@Override
		public boolean execute(final ShellState state) {
			System.out.println("Available commands:");
			System.out.println("  algo list         Sets the synthesizer algorithm");
			System.out.println("  bitwidth num      Sets the default bit width");
			System.out.println("  inputs i1,i2,...  Sets the initial inputs");
			System.out.println("  help              Prints this help message");
			System.out.println("  lib l1,l2,...     Sets the library functions");
			System.out.println("  precond pf        Adds a precondition");
			System.out.println("  quit              Quits this shell");
			System.out.println("  solver yices      Sets the underlying solver");
			System.out.println("  spec ni [ns] sf   Sets the specification");
			System.out.println("  stats             Gather statistics");
			System.out.println("  synth             Starts the synthesis");
			System.out.println("  timeout num       Sets the timeout (in milliseconds)");
			System.out.println("  timing            Gather running time information");
			return true;
		}
	}



	private static class InvalidCommand implements Command {

		private final String output;


		InvalidCommand(final String output) {
			this.output = output;
		}


		@Override
		public boolean execute(final ShellState state) {
			System.out.println(output);
			return true;
		}
	}



	private static class PreconditionCommand implements Command {

		private final String argument;


		PreconditionCommand(final String argument) {
			this.argument = argument;
		}


		@Override
		public boolean execute(final ShellState state) {
			state.specification.addPrecondition(FunctionParser.parseBool(this.argument));
			return true;
		}
	}



	private static class QuitCommand implements Command {
		@Override
		public boolean execute(final ShellState state) {
			return false;
		}
	}



	private static class SetAlgorithmCommand implements Command {

		private final String argument;


		SetAlgorithmCommand(final String argument) {
			this.argument = argument;
		}


		@Override
		public boolean execute(final ShellState state) {
			switch (this.argument) {
				case "list":
					// Do nothing
					break;
				default:
					System.out.println("Unexpected argument " + this.argument + ". Expected list");
			}
			return true;
		}
	}



	private static class SetBitWidthCommand implements Command {
		
		private final String argument;

		SetBitWidthCommand(final String argument) {
			this.argument = argument;
		}


		@Override
		public boolean execute(final ShellState state) {
			try {
				final int bitWidth = Integer.parseInt(this.argument);

				if (bitWidth != state.bitWidth) {
					if (state.inputs != null) {
						if (bitWidth < state.bitWidth) {
							for (int i = 0; i < state.inputs.size(); ++i) {
								state.inputs.set(i, state.inputs.get(i).extract(0, bitWidth - 1));
							}
						} else {
							final BitVector zero = new BitVector(state.bitWidth, 0);
							final BitVector fillZero = new BitVector(bitWidth - state.bitWidth, 0);
							final BitVector fillOne = new BitVector(bitWidth - state.bitWidth, -1);
							for (int i = 0; i < state.inputs.size(); ++i) {
								if (state.inputs.get(i).slt(zero)) {
									state.inputs.set(i, fillOne.concat(state.inputs.get(i)));
								} else {
									state.inputs.set(i, fillZero.concat(state.inputs.get(i)));
								}
							}
						}
					}

					state.bitWidth = bitWidth;

					if (state.specification != null) {
						System.out.println("Warning: Specification already set!");
					}
				}
			} catch (final NumberFormatException e) {
				System.out.println("Invalid bit width " + this.argument + ". Expected number");
			}
			return true;
		}
	}



	private static class SetInputsCommand implements Command {

		private final String argument;

		private static final Pattern SPLIT_PATTERN = Pattern.compile(",\\s*");

		SetInputsCommand(final String argument) {
			this.argument = argument;
		}


		@Override
		public boolean execute(final ShellState state) {
			final String[] parts = SPLIT_PATTERN.split(this.argument);

			final List<BitVector> result = new ArrayList<>();
			for (int i = 0; i < parts.length; ++i) {
				try {
				result.add(new BitVector(state.bitWidth, Long.parseLong(parts[i])));
				} catch (final NumberFormatException e) {
					System.out.println("Unexpected input number " + parts[i] + ". Expected number");
					return true;
				}
			}

			state.inputs = result;

			return true;
		}
	}



	private static class SetLibraryCommand implements Command {

		private final String argument;

		private static final Pattern SPLIT_PATTERN = Pattern.compile(",\\s*");

		SetLibraryCommand(final String argument) {
			this.argument = argument;
		}


		@Override
		public boolean execute(final ShellState state) {
			final String[] parts = SPLIT_PATTERN.split(this.argument);

			final List<LibraryFunction> result = new ArrayList<>();

			for (int i = 0; i < parts.length; ++i) {
				final String part = parts[i];

				if (part.startsWith("(")) {
					// Custom library function specification

					// Find end
					int end = i + 2;
					for (; end < parts.length && !parts[end].endsWith(")"); ++end) ;

					if (end >= parts.length) {
						System.out.println("Function specification ended unexpectedly");
						return true;
					}

					int numInputs = 0;
					try {
						numInputs = Integer.parseInt(parts[i + 1]);
					} catch (final NumberFormatException e) {
						System.out.println("Unexpected input number " + parts[i + 1] + ". Expected number");
						return true;
					}

					final StringBuilder functionBuilder = new StringBuilder();
					for (int j = i + 2; j < end; ++j) {
						functionBuilder.append(parts[j]).append(',');
					}
					functionBuilder.append(parts[end].substring(0, parts[end].length() - 1));

					try {
						result.add(new LibraryFunction(
								part.substring(1).trim(),
								Collections.nCopies(numInputs, state.bitWidth),
								state.bitWidth,
								FunctionParser.parse(functionBuilder.toString())));
					} catch (final IllegalArgumentException e) {
						System.out.println("Library function invalid: " + e.getMessage());
						return true;
					}

					i += 2;
				} else if (part.startsWith("const ")) {
					// Constant values
					final String[] splitted = part.split(" ");
					int bitWidth = -1;

					if (splitted.length != 2 && splitted.length != 3) {
						System.out.println("Expected 1 or 2 arguments to const, but got " + (splitted.length - 1));
						return true;
					}

					try {
						bitWidth = Integer.parseInt(splitted[1]);
					} catch (final NumberFormatException e) {
						System.out.println("Unexpected bit width " + splitted[1] + ". Expected number");
						return true;
					}

					if (bitWidth <= 0) {
						System.out.println("Bit width must be positive");
						return true;
					}

					if (splitted.length == 2) {
						result.add(LibraryFunction.getArbitraryConst(bitWidth));
					} else {
						long constValue = 0;
						try {
							if (splitted[2].startsWith("0x")) {
								constValue = Long.parseLong(splitted[2].substring(2), 16);
							} else {
								constValue = Long.parseLong(splitted[2]);
							}
						} catch (final NumberFormatException e) {
							System.out.println("Unexpected constant value " + splitted[2] + ". Expected number");
							return true;
						}

						result.add(LibraryFunction.getConst(bitWidth, constValue));
					}
				} else {
					// Predefined library functions
					switch (part) {
						case "add":
							result.add(LibraryFunction.getAdd(state.bitWidth));
							break;
						case "sub":
							result.add(LibraryFunction.getSub(state.bitWidth));
							break;
						case "mul":
							result.add(LibraryFunction.getMul(state.bitWidth));
							break;
						case "sdiv":
							result.add(LibraryFunction.getSDiv(state.bitWidth));
							break;
						case "udiv":
							result.add(LibraryFunction.getUDiv(state.bitWidth));
							break;
						case "srem":
							result.add(LibraryFunction.getSRem(state.bitWidth));
							break;
						case "urem":
							result.add(LibraryFunction.getURem(state.bitWidth));
							break;
						case "smod":
							result.add(LibraryFunction.getSMod(state.bitWidth));
							break;
						case "umod":
							result.add(LibraryFunction.getUMod(state.bitWidth));
							break;
						case "and":
							result.add(LibraryFunction.getAnd(state.bitWidth));
							break;
						case "or":
							result.add(LibraryFunction.getOr(state.bitWidth));
							break;
						case "xor":
							result.add(LibraryFunction.getXor(state.bitWidth));
							break;
						case "shl":
							result.add(LibraryFunction.getShl(state.bitWidth));
							break;
						case "ashr":
							result.add(LibraryFunction.getAshr(state.bitWidth));
							break;
						case "lshr":
							result.add(LibraryFunction.getLshr(state.bitWidth));
							break;
						case "shljava":
							result.add(LibraryFunction.getShlJava(state.bitWidth));
							break;
						case "ashrjava":
							result.add(LibraryFunction.getAshrJava(state.bitWidth));
							break;
						case "lshrjava":
							result.add(LibraryFunction.getLshrJava(state.bitWidth));
							break;
						case "not":
							result.add(LibraryFunction.getNot(state.bitWidth));
							break;
						case "neg":
							result.add(LibraryFunction.getNeg(state.bitWidth));
							break;
						case "eq":
							result.add(LibraryFunction.getEqBV(state.bitWidth));
							break;
						case "neq":
							result.add(LibraryFunction.getNeqBV(state.bitWidth));
							break;
						case "ugt":
							result.add(LibraryFunction.getUGtBV(state.bitWidth));
							break;
						case "uge":
							result.add(LibraryFunction.getUGeBV(state.bitWidth));
							break;
						case "ult":
							result.add(LibraryFunction.getULtBV(state.bitWidth));
							break;
						case "ule":
							result.add(LibraryFunction.getULeBV(state.bitWidth));
							break;
						case "sgt":
							result.add(LibraryFunction.getSGtBV(state.bitWidth));
							break;
						case "sge":
							result.add(LibraryFunction.getSGeBV(state.bitWidth));
							break;
						case "slt":
							result.add(LibraryFunction.getSLtBV(state.bitWidth));
							break;
						case "sle":
							result.add(LibraryFunction.getSLeBV(state.bitWidth));
							break;
						default:
							System.out.println("Unexpected library function " + part);
							return true;
					}
				}
			}

			state.library = Library.of(result);
			return true;
		}
	}



	private static class SetSpecificationCommand implements Command {
		
		private final String numInputsArgument;
		private final String specFuncArgument;


		SetSpecificationCommand(final String numInputsArgument, final String specFuncArgument) {
			this.numInputsArgument = numInputsArgument;
			this.specFuncArgument = specFuncArgument;
		}


		@Override
		public boolean execute(final ShellState state) {
			int numInputs = 0;
			try {
				numInputs = Integer.parseInt(this.numInputsArgument);
			} catch (final NumberFormatException e) {
				System.out.println("Unexpected argument " + this.numInputsArgument + ". Expected number");
				return true;
			}

			if (numInputs < 0) {
				System.out.println("Number of inputs must not be negative");
				return true;
			}

			if (this.specFuncArgument.isEmpty()) {
				System.out.println("No specification function given");
				return true;
			}

			String numStmtsArg = "";
			String specFuncArg = this.specFuncArgument;
			if (Character.isDigit(this.specFuncArgument.charAt(0))) {
				final int sepIndex = this.specFuncArgument.indexOf(' ');
				if (sepIndex < 0) {
					System.out.println("No specification function given");
					return true;
				}

				numStmtsArg = this.specFuncArgument.substring(0, sepIndex);
				specFuncArg = this.specFuncArgument.substring(sepIndex + 1).trim();
			}

			try {
				if (numStmtsArg.isEmpty()) {
					state.specification = new Specification(
							Collections.nCopies(numInputs, state.bitWidth),
							state.bitWidth,
							FunctionParser.parse(specFuncArg));
				} else {
					final int numStmts = Integer.parseInt(numStmtsArg);
					if (numStmts <= 0) {
						System.out.println("Number of statements must be positive");
					} else {
						state.specification = new Specification(
								Collections.nCopies(numInputs, state.bitWidth),
								state.bitWidth,
								numStmts,
								FunctionParser.parse(specFuncArg));
						state.inputs = null;
					}
				}
			} catch (final NumberFormatException e) {
				System.out.println("Unexpected argument " + numStmtsArg + ". Expected number");
			} catch (final IllegalArgumentException e) {
				System.out.println("Specification function invalid: " + e.getMessage());
			}
			return true;
		}
	}



	private static class SetSolverCommand implements Command {

		private final String argument;


		SetSolverCommand(final String argument) {
			this.argument = argument;
		}


		@Override
		public boolean execute(final ShellState state) {
			switch (this.argument) {
				case "yices":
					state.settings.setGenerateSolverType(SolverType.YICES);
					state.settings.setVerifySolverType(SolverType.YICES);
					break;

				default: {
					System.out.println("Unexpected argument " + this.argument + ". Expected yices");
				}
			}
			return true;
		}
	}



	private static class SetStatisticsCommand implements Command {
		@Override
		public boolean execute(final ShellState state) {
			state.printStatistics = true;
			return true;
		}
	}



	private static class SetTimeoutCommand implements Command {

		private final String argument;


		SetTimeoutCommand(final String argument) {
			this.argument = argument;
		}


		@Override
		public boolean execute(final ShellState state) {
			try {
				final int timeout = Integer.parseInt(this.argument);
				if (timeout > 0) {
					state.settings.setTimeout(timeout);
				} else {
					System.out.println("Timeout must be positive");
				}
			} catch (final NumberFormatException e) {
				System.out.println("Unexpected argument " + this.argument + ". Expected number");
			}
			return true;
		}
	}



	private static class SetTimingCommand implements Command {
		@Override
		public boolean execute(final ShellState state) {
			state.logRunTime = true;
			return true;
		}
	}



	private static class SynthesizeCommand implements Command {
		@Override
		public boolean execute(final ShellState state) {
			if (state.specification == null) {
				System.out.println("Specification not set");
			} else if (state.library == null) {
				System.out.println("Library not set");
			} else {
				final long startTime = System.nanoTime();

				final AbstractBVSynthesizer synthesizer = new ListBasedSynthesizer();

				try {
					final Optional<Program> result;
					
					if (state.inputs == null) {
						for (final Function<List<BVAst>, BoolAst> p : state.specification.getPreconditions()) {
							if (!checkPrecond(p, Collections.nCopies(state.specification.getNumberOfInputs(),
										new BitVector(state.bitWidth, 0)))) {
								System.out.println("Precondition incompatible with inputs");
								return true;
							}
						}

						result = synthesizer.synthesizeProgram(
								state.specification,
								state.library,
								state.settings);
					} else {
						for (final Function<List<BVAst>, BoolAst> p : state.specification.getPreconditions()) {
							if (!checkPrecond(p, state.inputs)) {
								System.out.println("Precondition incompatible with inputs");
								return true;
							}
						}

						result = synthesizer.synthesizeProgramWith(
								state.specification,
								state.library,
								Collections.singletonList(state.inputs),
								state.settings);
					}
					
					if (result.isPresent()) {
						System.out.println("sat");
						System.out.println(result.get());
					} else {
						System.out.println("unsat");
					}
				} catch (final TimeoutException e) {
					System.out.println("timeout");
				}
				if (state.printStatistics) {
					System.out.println(synthesizer.getStatistics().get());
				}

				if (state.logRunTime) {
					System.out.printf("(%.2f ms)%n", (System.nanoTime() - startTime) / 1000000.0);
				}
			}
			return true;
		}


		private static boolean checkPrecond(final Function<List<BVAst>, BoolAst> precond,
				final List<BitVector> inputs) {

			final List<BVAst> inputASTs = new ArrayList<>();
			for (final BitVector input : inputs) {
				inputASTs.add(new BVConst(input));
			}

			return precond.apply(inputASTs).eval(Collections.emptyMap());
		}
	}



	private static Command parseCommand(final Scanner scanner) {
		System.out.print("> ");

		try {
			Command result = null;

			boolean searchForTrailingArguments = true;

			final String commandName = scanner.next();
			switch (commandName) {
				case "algo":
					result = new SetAlgorithmCommand(scanner.next());
					break;

				case "bitwidth":
					result = new SetBitWidthCommand(scanner.next());
					break;

				case "help":
					result = new HelpCommand();
					break;

				case "inputs":
					result = new SetInputsCommand(scanner.nextLine().trim());
					searchForTrailingArguments = false;
					break;

				case "lib":
					result = new SetLibraryCommand(scanner.nextLine().trim());
					searchForTrailingArguments = false;
					break;

				case "precond":
					result = new PreconditionCommand(scanner.nextLine().trim());
					searchForTrailingArguments = false;
					break;

				case "quit":
					result = new QuitCommand();
					break;

				case "solver":
					result = new SetSolverCommand(scanner.next());
					break;

				case "spec":
					result = new SetSpecificationCommand(scanner.next(), scanner.nextLine().trim());
					searchForTrailingArguments = false;
					break;

				case "stats":
					result = new SetStatisticsCommand();
					break;

				case "synth":
					result = new SynthesizeCommand();
					break;

				case "timeout":
					result = new SetTimeoutCommand(scanner.next());
					break;

				case "timing":
					result = new SetTimingCommand();
					break;

				default:
					scanner.nextLine();
					return new InvalidCommand("Invalid command " + commandName);
			}

			if (searchForTrailingArguments && !scanner.nextLine().isEmpty()) {
				return new InvalidCommand("Too many arguments given");
			}

			return result;
		} catch (final NoSuchElementException e) {
			return new QuitCommand();
		}
	}



	public static void main(String[] args) {
		final ShellState state = new ShellState();

		final Scanner scanner = new Scanner(System.in);

		System.out.println(); // Skip the line containing gradle output

		boolean quit = false;
		while (!quit) {
			quit = !parseCommand(scanner).execute(state);
		}
	}
}

