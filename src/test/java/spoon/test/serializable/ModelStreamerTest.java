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
package spoon.test.serializable;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import spoon.Launcher;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.Filter;
import spoon.support.CompressionType;
import spoon.support.SerializationModelStreamer;

public class ModelStreamerTest {

	private final String filename = "./src/test/resources/serialization/factory.ser";

	private Factory factory;

	private File file;

	@Before
    public void setUp() {
		Launcher launcher = new Launcher();
		launcher.addInputResource("./src/main/java/spoon/reflect/declaration");
		launcher.buildModel();
		this.factory = launcher.getFactory();
		file = new File(filename);
		file.deleteOnExit();
    }

	@Test
	public void testDefaultCompressionType() throws IOException {
		new SerializationModelStreamer().save(factory, new FileOutputStream(file));
		Factory factoryFromFile = new SerializationModelStreamer().load(new FileInputStream(file));
		compareFactoryModels(factory, factoryFromFile);
	}
	
	@Test
	public void testGZipCompressionType() throws IOException {
		factory.getEnvironment().setCompressionType(CompressionType.GZIP);
		new SerializationModelStreamer().save(factory, new FileOutputStream(file));
		Factory factoryFromFile = new SerializationModelStreamer().load(new FileInputStream(file));
		compareFactoryModels(factory, factoryFromFile);
	}

	@Test
	public void testNoneCompressionType() throws IOException {
		factory.getEnvironment().setCompressionType(CompressionType.NONE);
		new SerializationModelStreamer().save(factory, new FileOutputStream(file));
		Factory factoryFromFile = new SerializationModelStreamer().load(new FileInputStream(file));
		compareFactoryModels(factory, factoryFromFile);
	}

	private void compareFactoryModels(Factory factory, Factory factoryFromFile) {
		Filter<CtElement> filter = new Filter<CtElement>() {
			public boolean matches(CtElement element) {
				return true;
			}
		};

		List<CtElement> elementsFactory = factory.getModel().getRootPackage().filterChildren(filter).list();
		List<CtElement> elementsFactoryFromFile = factoryFromFile.getModel().getRootPackage().filterChildren(filter).list();

		assertTrue("Model before & after serialization must have the same number of elements", 
				elementsFactory.size() == elementsFactoryFromFile.size());

		List<String> st1 = elementsFactory.stream().map(CtElement::toString).collect(Collectors.toList());
		List<String> st2 = elementsFactoryFromFile.stream().map(CtElement::toString).collect(Collectors.toList());
		Collections.sort(st1);
		Collections.sort(st2);

		for (int i = 0; i < st1.size(); i++) {
			assertTrue("All CtElement of the model should be striclty identical before & after serialization",
					st1.get(i).equals(st2.get(i)));
		}
	}
}
