package spoon.modelapi;

import spoon.compiler.Environment;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.reflect.visitor.PrintingContext;
import spoon.reflect.visitor.printer.CommentOffset;

import java.util.List;

public class APIPrettyPrinter extends DefaultJavaPrettyPrinter {
	/**
	 * Creates a new code generator visitor.
	 *
	 * @param env
	 */
	public APIPrettyPrinter(Environment env) {
		super(env);
	}

	@Override
	public <T> void visitCtClass(CtClass<T> ctClass) {
		context.pushCurrentThis(ctClass);
		if (ctClass.getSimpleName() != null && !CtType.NAME_UNKNOWN.equals(ctClass.getSimpleName()) && !ctClass.isAnonymous()) {
			visitCtType(ctClass);
			if (ctClass.isLocalType()) {
				printer.writeKeyword("class").writeSpace().writeIdentifier(ctClass.getSimpleName().replaceAll("^[0-9]*", ""));
			} else {
				printer.writeKeyword("class").writeSpace().writeIdentifier(ctClass.getSimpleName());
			}

			elementPrinterHelper.writeFormalTypeParameters(ctClass);
			elementPrinterHelper.writeExtendsClause(ctClass);
			elementPrinterHelper.writeImplementsClause(ctClass);
		}
		printer.writeSpace().writeSeparator("{").incTab();
		writeElementList(ctClass.getTypeMembers());
		getPrinterHelper().adjustEndPosition(ctClass);
		printer.decTab().writeSeparator("}");
		context.popCurrentThis();
	}


	public void writeElementList(List<CtTypeMember> elements) {
		for (CtTypeMember element : elements) {
			if (element instanceof CtConstructor && element.isImplicit()
					|| !element.isPublic()) {
				continue;
			}
			printer.writeln();
			scan(element);
			if (!env.isPreserveLineNumbers()) {
				printer.writeln();
			}
		}
	}

	@Override
	public <T> void visitCtConstructor(CtConstructor<T> constructor) {
		elementPrinterHelper.writeComment(constructor);
		elementPrinterHelper.visitCtNamedElement(constructor, sourceCompilationUnit);
		elementPrinterHelper.writeModifiers(constructor);
		elementPrinterHelper.writeFormalTypeParameters(constructor);
		if (!constructor.getFormalCtTypeParameters().isEmpty()) {
			printer.writeSpace();
		}
		if (constructor.getDeclaringType() != null) {
			if (constructor.getDeclaringType().isLocalType()) {
				printer.writeIdentifier(constructor.getDeclaringType().getSimpleName().replaceAll("^[0-9]*", ""));
			} else {
				printer.writeIdentifier(constructor.getDeclaringType().getSimpleName());
			}
		}
		elementPrinterHelper.writeExecutableParameters(constructor);
		elementPrinterHelper.writeThrowsClause(constructor);
		printer.writeSpace();
		//scan(constructor.getBody());
		printer.writeSpace().writeSeparator("{").incTab();
		printer.decTab().writeSeparator("}");
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> m) {
		elementPrinterHelper.writeComment(m, CommentOffset.BEFORE);
		elementPrinterHelper.visitCtNamedElement(m, sourceCompilationUnit);
		elementPrinterHelper.writeModifiers(m);
		elementPrinterHelper.writeFormalTypeParameters(m);
		if (!m.getFormalCtTypeParameters().isEmpty()) {
			printer.writeSpace();
		}
		try (PrintingContext.Writable _context = context.modify().ignoreGenerics(false)) {
			scan(m.getType());
		}
		printer.writeSpace();
		printer.writeIdentifier(m.getSimpleName());
		elementPrinterHelper.writeExecutableParameters(m);
		elementPrinterHelper.writeThrowsClause(m);
		if (m.getBody() != null) {
			printer.writeSpace();
			/*scan(m.getBody());
			if (m.getBody().getPosition().isValidPosition()) {
				if (m.getBody().getPosition().getCompilationUnit() == sourceCompilationUnit) {
					if (m.getBody().getStatements().isEmpty() || !(m.getBody().getStatements().get(m.getBody().getStatements().size() - 1) instanceof CtReturn)) {
						getPrinterHelper().putLineNumberMapping(m.getBody().getPosition().getEndLine());
					}
				} else {
					getPrinterHelper().undefineLine();
				}
			} else {
				getPrinterHelper().undefineLine();
			}*/
			printer.writeSpace().writeSeparator("{").incTab();
			printer.decTab().writeSeparator("}");
		} else {
			printer.writeSeparator(";");
		}
		elementPrinterHelper.writeComment(m, CommentOffset.AFTER);
	}

	@Override
	public <T> void visitCtField(CtField<T> f) {
		elementPrinterHelper.writeComment(f, CommentOffset.BEFORE);
		elementPrinterHelper.visitCtNamedElement(f, sourceCompilationUnit);
		elementPrinterHelper.writeModifiers(f);
		scan(f.getType());
		printer.writeSpace();
		printer.writeIdentifier(f.getSimpleName());

		/*if (f.getDefaultExpression() != null) {
			printer.writeSpace().writeOperator("=").writeSpace();
			scan(f.getDefaultExpression());
		}*/
		printer.writeSeparator(";");
		elementPrinterHelper.writeComment(f, CommentOffset.AFTER);
	}
}
