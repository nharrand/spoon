package spoon.test.spoonifier;

import org.junit.Test;

import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.experimental.SpoonifyVisitor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


import static org.junit.Assert.assertEquals;

public class SpoonifierTest {

	@Test
	public void testSpoonifier() throws ClassNotFoundException, MalformedURLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		int i = 0;
		testSpoonifierWith("src/test/java/spoon/test/prettyprinter/testclasses/A.java", i++);
		testSpoonifierWith("src/test/java/spoon/test/prettyprinter/testclasses/AClass.java", i++);
		testSpoonifierWith("src/test/java/spoon/test/spoonifier/testclasses/ArrayRealVector.java", i++);
		testSpoonifierWith("src/test/java/spoon/test/prettyprinter/testclasses/FooCasper.java", i++);
		testSpoonifierWith("src/test/java/spoon/test/prettyprinter/testclasses/Rule.java", i++);

	}

	/**
	 * @param pathToClass path to the class that will be Spoonified
	 * @param i an integer to avoid duplicated classes
	 *
	 * This method verifies that SpoonifyVisitor can generate code that replicates a class
	 */
	public void testSpoonifierWith(String pathToClass, int i)
			throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		//Build the model of the given class
		Launcher launcher = new Launcher();
		launcher.addInputResource(pathToClass);
		CtModel model = launcher.buildModel();
		CtType targetType = model.getElements(new TypeFilter<CtType>(CtType.class)).get(0);

		//Spoonify
		String wrapper = generateSpoonifiyWrapper(targetType, i);

		//System.out.println(wrapper);

		//Output launcher containing a class wrapping the generated code
		Launcher oLauncher = new Launcher();
		File outputBinDir = new File("./spooned-classes/");
		oLauncher.setBinaryOutputDirectory(outputBinDir);

		CtClass wrapperClass = oLauncher.parseClass(wrapper);
		CtModel oModel = oLauncher.buildModel();
		oLauncher.getEnvironment().disableConsistencyChecks();
		oModel.getRootPackage().addType(wrapperClass);
		oLauncher.getModelBuilder().compile(SpoonModelBuilder.InputType.CTTYPES);

		//Invoke the code generated
		URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{outputBinDir.toURI().toURL()});
		Class rtWrapper = urlClassLoader.loadClass("SpoonifierWrapper" + i);
		Method get = rtWrapper.getMethod("get", Factory.class);
		CtElement generatedElement = (CtElement) get.invoke(null, targetType.getFactory());

		//The element created by the code generated by SpoonifyVisitor is equivalent to the visited element
		assertEquals(targetType, generatedElement);
	}

	/*
	 * Generates a class with a single static method:
	 * public static CtElement get(Factory factory);
	 * This method calls the code generated by SpoonifyVisitor to
	 * recreate a CtElement equaled to the one visited.
	 */
	public String generateSpoonifiyWrapper(CtElement element, int i) {
		String elementClass = element.getClass().getSimpleName();
		if(elementClass.endsWith("Impl")) {
			elementClass = elementClass.replace("Impl","");
		}
		String variableName = elementClass.substring(0, 1).toLowerCase() + elementClass.substring(1) + "0";

		SpoonifyVisitor spoonifier = new SpoonifyVisitor();
		element.accept(spoonifier);
		StringBuffer buf = new StringBuffer();
		buf.append("import java.util.*;\n");
		buf.append("import spoon.reflect.code.*;\n");
		buf.append("import spoon.reflect.declaration.*;\n");
		buf.append("import spoon.reflect.factory.Factory;\n");
		buf.append("import spoon.reflect.path.CtRole;\n");
		buf.append("import spoon.reflect.reference.*;\n");
		buf.append("public class SpoonifierWrapper" + i + "{\n");
		buf.append("\tpublic static CtElement get(Factory factory) {\n");
		buf.append(spoonifier.getResult());
		buf.append("\treturn " + variableName + ";\n");
		buf.append("\t}\n");
		buf.append("}\n");

		return buf.toString();
	}


}
