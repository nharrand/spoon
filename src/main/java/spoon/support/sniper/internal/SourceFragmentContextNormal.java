/**
 * Copyright (C) 2006-2018 INRIA and contributors
 * Spoon - http://spoon.gforge.inria.fr/
 *
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package spoon.support.sniper.internal;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

/**
 * Knows how to print modified {@link CtElement} by the way that origin formatting is kept as much as possible.
 * There are two streams of source fragments
 * 1) stream of origin source fragments
 * 2) stream of tokens produced by {@link DefaultJavaPrettyPrinter} in spaces between printing of child {@link CtElement}s
 *
 * Note: some fragments are optional and some are mandatory. For example:
 * &lt;T&gt; void method(T p);
 * and after remove of type parameter and method parameter....
 * void method();
 * The type parameter brackets were removed, while parameter brackets are kept.
 * Only DJPP knows whether it has to be displayed or not. So algorithm is like this:
 * <ul>
 * <li> print origin source token only after this token is printed by DJPP
 * <li> if DJPP doesn't prints some token then the same token must be ignored in origin source code too
 * </ul>
 *
 * Handling of spaces before and after tokens:
 * <ul>
 * <li> spaces belong to both surrounding elements. So they are printed only if both elements are printed
 * <li> if the elements are moved, then spaces are moved together with the follow up element
 * <li> if the elements E1 and E2 are separated by sequence of E1, Spaces1, Separator, Spaces2, E2
 * then Spaces1 belongs to E1 and Spaces2 belongs to element E2.
 * </ul>
 */
public class SourceFragmentContextNormal extends AbstractSourceFragmentContext {
	/**
	 * @param mutableTokenWriter {@link MutableTokenWriter}, which is used for printing
	 * @param rootFragment the {@link ElementSourceFragment}, which represents whole elements. E.g. whole type or method
	 * @param changeResolver
	 */
	public SourceFragmentContextNormal(MutableTokenWriter mutableTokenWriter, ElementSourceFragment rootFragment, ChangeResolver changeResolver) {
		super(mutableTokenWriter, changeResolver, rootFragment.getGroupedChildrenFragments());
	}

	@Override
	public boolean matchesPrinterEvent(PrinterEvent event) {
		return true;
	}


	@Override
	public void onFinished() {
		//we are at the end of this element. Printer just tries to print something out of this context.
		if (mutableTokenWriter.isMuted() == false) {
			//print fragment suffix
			printSpaces(childFragments.size());
		}
	}
}
