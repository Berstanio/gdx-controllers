import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JavaCodeGenerator {
	/** This would allow us to keep exact formatting, but it's broken for FieldDeclarations for some reason. Potential related to
	 * https://github.com/javaparser/javaparser/issues/1601 */
	private static boolean LEXICAL_PRESERVING_PRINTER = false;

	private static File BASE_PROJECT_SRC = new File("../gdx-controllers-ios/src");

	private static Map<String, String> IMPORT_REPLACEMENTS = new HashMap<>();
	private static Map<String, String> METHOD_REPLACEMENTS = new HashMap<>();
	private static Map<String, String> CLASS_REPLACEMENTS = new HashMap<>();

	static {
		IMPORT_REPLACEMENTS.put("apple.corehaptics.CHHapticPatternPlayer", "apple.corehaptics.protocol.CHHapticPatternPlayer");
		IMPORT_REPLACEMENTS.put("apple.gamecontroller.GCControllerPlayerIndex", "apple.gamecontroller.enums.GCControllerPlayerIndex");
		IMPORT_REPLACEMENTS.put("apple.uikit.UIKeyModifierFlags", "apple.uikit.enums.UIKeyModifierFlags");
		IMPORT_REPLACEMENTS.put("org.robovm.objc.Selector", "org.moe.natj.objc.SEL");


		for (Entry<String, String> e : IMPORT_REPLACEMENTS.entrySet()) {
			String o = e.getKey().substring(e.getKey().lastIndexOf('.') + 1);
			String n = e.getValue().substring(e.getValue().lastIndexOf('.') + 1);
			CLASS_REPLACEMENTS.put(o, n);
		}

		addMOEMethodReplace("getExtendedGamepad");
		addMOEMethodReplace("getGamepad");
		addMOEMethodReplace("getVendorName");
		addMOEMethodReplace("getHaptics");
		addMOEMethodReplace("getButtonA");
		addMOEMethodReplace("getButtonB");
		addMOEMethodReplace("getButtonX");
		addMOEMethodReplace("getButtonY");
		addMOEMethodReplace("getLeftShoulder");
		addMOEMethodReplace("getRightShoulder");
		addMOEMethodReplace("getLeftTrigger");
		addMOEMethodReplace("getRightTrigger");
		addMOEMethodReplace("getButtonOptions");
		addMOEMethodReplace("getButtonMenu");
		addMOEMethodReplace("getLeftThumbstickButton");
		addMOEMethodReplace("getRightThumbstickButton");
		addMOEMethodReplace("getDpad");
		addMOEMethodReplace("getUp");
		addMOEMethodReplace("getDown");
		addMOEMethodReplace("getLeft");
		addMOEMethodReplace("getRight");
		addMOEMethodReplace("getBattery");
		addMOEMethodReplace("getBatteryState");
		addMOEMethodReplace("getBatteryLevel");
		addMOEMethodReplace("getPlayerIndex");
		addMOEMethodReplace("getLeftThumbstick");
		addMOEMethodReplace("getRightThumbstick");
		addMOEMethodReplace("getXAxis");
		addMOEMethodReplace("getYAxis");
		addMOEMethodReplace("getValue");
		addMOEMethodReplace("getInput");
		addMOEMethodReplace("getControllers");

		METHOD_REPLACEMENTS.put("createEngine", "createEngineWithLocality");

		//METHOD_REPLACEMENTS.put("start", "startAndReturnError");
	}

	private static void addMOEMethodReplace(String roboString) {
		String moe = roboString.replace("get", "");
		moe = Character.toLowerCase(moe.charAt(0)) + moe.substring(1);
		METHOD_REPLACEMENTS.put(roboString, moe);
	}

	public static void main (String[] args) throws Exception {
		search(BASE_PROJECT_SRC);
	}

	private static void search (File f) throws Exception {
		if (f.isDirectory()) {
			for (File child : f.listFiles())
				search(child);
			return;
		}

		if (!f.getName().endsWith(".java")) {
			return;
		}

		parse(f);
	}

	private static void parse (File f) throws Exception {
		CompilationUnit cu = StaticJavaParser.parse(f);

		if (LEXICAL_PRESERVING_PRINTER) cu = LexicalPreservingPrinter.setup(cu);

		cu.setBlockComment("DO NOT EDIT THIS FILE - it is machine generated");
		if (f.getName().equals("IosController.java")) {
			cu.addImport("apple.gamecontroller.enums.GCDeviceBatteryState", true, true);
			cu.addImport("apple.gamecontroller.c.GameController");
			cu.addImport("apple.corehaptics.c.CoreHaptics");
			cu.addImport("org.moe.natj.objc.ObjCRuntime");
			cu.addImport("apple.uikit.UIDevice");
		}

		if (f.getName().equals("IosControllerManager.java")) {
			cu.addImport("apple.uikit.UIDevice");
		}

		final JavaParser javaParser = new JavaParser();

		ModifierVisitor<Object> visitor = new ModifierVisitor() {

			@Override
			public Visitable visit(MethodDeclaration n, Object arg) {
				if (n.getNameAsString().equals("getPlayerIndex")) {
					NameExpr nameExpr = new NameExpr("controller");
					MethodCallExpr methodCallExpr = new MethodCallExpr(nameExpr, "playerIndex");
					CastExpr castExpr = new CastExpr(PrimitiveType.intType(), methodCallExpr);
					ReturnStmt returnStmt = new ReturnStmt(castExpr);
					BlockStmt blockStmt = new BlockStmt();
					blockStmt.addStatement(returnStmt);
					n.setBody(blockStmt);
				}
				if (n.getNameAsString().equals("constructRumbleEvent")) {
					n.setThrownExceptions(new NodeList<>());
				}
				return super.visit(n, arg);
			}

			@Override
			public Visitable visit(SwitchStmt n, Object arg) {
				if (n.getSelector() instanceof MethodCallExpr) {
					MethodCallExpr methodCallExpr = n.getSelector().asMethodCallExpr();
					if (methodCallExpr.getNameAsString().equals("getBatteryState")) {
						CastExpr castExpr = new CastExpr(PrimitiveType.intType(), methodCallExpr);
						n.setSelector(castExpr);
						n.getEntries().forEach(e -> {
							if (e.getLabels().size() == 0) return;
							Expression expression = e.getLabels().get(0);
							CastExpr cast = new CastExpr(PrimitiveType.intType(), expression);
							e.getLabels().set(0, cast);
						});
					}
				}
				return super.visit(n, arg);
			}

			@Override
			public Visitable visit(MethodCallExpr n, Object arg) {
				if (METHOD_REPLACEMENTS.containsKey(n.getNameAsString()) && n.getScope().isPresent() && !n.getScope().get().isSuperExpr()) {
					n.setName(METHOD_REPLACEMENTS.get(n.getNameAsString()));
				}

				if (n.getNameAsString().equals("getMajorSystemVersion")) {
					n.setScope(null);
				}

				if (n.getNameAsString().equals("release")) {
					NameExpr objCRuntime = new NameExpr("ObjCRuntime");
					MethodCallExpr release = new MethodCallExpr(objCRuntime, "releaseObject");
					MethodCallExpr pointerPeer = new MethodCallExpr(n.getScope().get(), "getPeerPointer");
					release.addArgument(pointerPeer);
					return super.visit(release, arg);
				}

				if (n.getNameAsString().equals("retain")) {
					NameExpr objCRuntime = new NameExpr("ObjCRuntime");
					MethodCallExpr release = new MethodCallExpr(objCRuntime, "retainObject");
					MethodCallExpr pointerPeer = new MethodCallExpr(n.getScope().get(), "getPeerPointer");
					release.addArgument(pointerPeer);
					return super.visit(release, arg);
				}

				if (n.getNameAsString().equals("createPlayer")) {
					n.setName("createPlayerWithPatternError");
					n.addArgument(new NullLiteralExpr());
				}

				if (n.getNameAsString().equals("start")) {
					if (n.getArguments().size() == 1) {
						n.setName("startAndReturnError");
					} else if (n.getArguments().size() == 2) {
						n.setName("startAtTimeError");
					}
				}

				if (n.getNameAsString().equals("valueOf")) {
					if (n.getScope().get().asNameExpr().getNameAsString().equals("GCControllerPlayerIndex")) {
						return new NameExpr("index");
					}
				}
				return super.visit(n, arg);
			}

			@Override
			public Visitable visit(ObjectCreationExpr n, Object arg) {
				if (n.getType().getNameAsString().equals("CHHapticEventParameter")) {
					NameExpr nameExpr = new NameExpr("CHHapticEventParameter");
					MethodCallExpr alloc = new MethodCallExpr(nameExpr, "alloc");
					MethodCallExpr init = new MethodCallExpr(alloc, "initWithParameterIDValue");
					init.setArguments(n.getArguments());
					return super.visit(init, arg);
				} else if (n.getType().getNameAsString().equals("CHHapticEvent")) {
					NameExpr nameExpr = new NameExpr("CHHapticEvent");
					MethodCallExpr alloc = new MethodCallExpr(nameExpr, "alloc");
					MethodCallExpr init = new MethodCallExpr(alloc, "initWithEventTypeParametersRelativeTimeDuration");
					init.setArguments(n.getArguments());
					return super.visit(init, arg);
				} else if (n.getType().getNameAsString().equals("CHHapticPattern")) {
					NameExpr nameExpr = new NameExpr("CHHapticPattern");
					MethodCallExpr alloc = new MethodCallExpr(nameExpr, "alloc");
					MethodCallExpr init = new MethodCallExpr(alloc, "initWithEventsParameterCurvesError");
					init.setArguments(n.getArguments());
					init.addArgument(new NullLiteralExpr());
					return super.visit(init, arg);
				} else if (n.getType().getNameAsString().equals("NSArray")) {
					NameExpr nameExpr = new NameExpr("NSArray");
					MethodCallExpr expr;
					if (n.getArguments().size() == 0) {
						expr = new MethodCallExpr(nameExpr, "array");
					} else if (n.getArguments().size() == 1) {
						expr = new MethodCallExpr(nameExpr, "arrayWithObject");
					} else {
						expr = new MethodCallExpr(nameExpr, "arrayWithObjects");
					}
					expr.setArguments(n.getArguments());
					return super.visit(expr, arg);
				}
				return super.visit(n, arg);
			}

			@Override
			public Visitable visit(FieldAccessExpr n, Object arg) {
				if (n.getScope() instanceof NameExpr) {
					// Can be improved
					if (n.getScope().asNameExpr().getNameAsString().equals("GCHapticsLocality")) {
						NameExpr nameExpr = new NameExpr("GameController");
						MethodCallExpr methodCallExpr = new MethodCallExpr(nameExpr, "GCHapticsLocality" + n.getNameAsString());
						return super.visit(methodCallExpr, arg);
					} else if (n.getScope().asNameExpr().getNameAsString().equals("CHHapticEventParameterID")) {
						NameExpr nameExpr = new NameExpr("CoreHaptics");
						MethodCallExpr methodCallExpr = new MethodCallExpr(nameExpr, "CHHapticEventParameterID" + n.getNameAsString());
						return super.visit(methodCallExpr, arg);
					} else if (n.getScope().asNameExpr().getNameAsString().equals("CHHapticEventType")) {
						NameExpr nameExpr = new NameExpr("CoreHaptics");
						MethodCallExpr methodCallExpr = new MethodCallExpr(nameExpr, "CHHapticEventType" + n.getNameAsString());
						return super.visit(methodCallExpr, arg);
					}
				}
				return super.visit(n, arg);
			}

			@Override
			public Name visit (Name n, final Object arg) {
				n = (Name)super.visit(n, arg);

				n = new Name(n.asString().replace("org.robovm.apple", "apple"));
				n = new Name(n.asString().replace("apple.corehaptic.", "apple.corehaptics."));

				// Test
				if (IMPORT_REPLACEMENTS.containsKey(n.asString())) {
					n = new Name(IMPORT_REPLACEMENTS.get(n.asString()));
				}
				return n;
			}

			@Override
			public Visitable visit (SimpleName n, Object arg) {
				n = (SimpleName)super.visit(n, arg);
				if (CLASS_REPLACEMENTS.containsKey(n.asString())) {
					n = new SimpleName(CLASS_REPLACEMENTS.get(n.asString()));
				}
				return n;
			}

			@Override
			public Visitable visit (ClassOrInterfaceType n, final Object arg) {
				n = (ClassOrInterfaceType)super.visit(n, arg);

				if (CLASS_REPLACEMENTS.containsKey(n.asString())) {
					n = javaParser.parseClassOrInterfaceType(CLASS_REPLACEMENTS.get(n.asString())).getResult().get();
				}
				return n;
			}

			@Override
			public Visitable visit (ClassOrInterfaceDeclaration n, Object arg) {
				n = (ClassOrInterfaceDeclaration)super.visit(n, arg);

				String base = n.getJavadocComment().isPresent() ? n.getJavadocComment().get().getContent() : "";

				n.setJavadocComment("DO NOT EDIT THIS FILE - it is machine generated\n" + base);

				if (n.getNameAsString().equals("IosController") || n.getNameAsString().equals("IosControllerManager")) {
					MethodDeclaration declaration = n.addMethod("getMajorSystemVersion", Keyword.PRIVATE, Keyword.STATIC);
					declaration.setType(PrimitiveType.intType());

					NameExpr nameExpr = new NameExpr("UIDevice");
					MethodCallExpr current = new MethodCallExpr(nameExpr, "currentDevice");
					MethodCallExpr version = new MethodCallExpr(current, "systemVersion");
					MethodCallExpr split = new MethodCallExpr(version, "split");
					split.addArgument(new StringLiteralExpr("\\\\."));

					ArrayAccessExpr accessExpr = new ArrayAccessExpr(split, new IntegerLiteralExpr(0));

					NameExpr intName = new NameExpr("Integer");
					MethodCallExpr parseInt = new MethodCallExpr(intName, "parseInt");
					parseInt.addArgument(accessExpr);

					ReturnStmt returnStmt = new ReturnStmt(parseInt);
					BlockStmt blockStmt = new BlockStmt();
					blockStmt.addStatement(returnStmt);
					declaration.setBody(blockStmt);
				}


				return n;
			}
		};

		cu.accept(visitor, null);

		File out = new File("src", f.getCanonicalPath().substring(BASE_PROJECT_SRC.getCanonicalPath().length()));
		out.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(out)) {
			if (LEXICAL_PRESERVING_PRINTER)
				LexicalPreservingPrinter.print(cu, writer);
			else
				writer.append(cu.toString());
		}
	}
}
