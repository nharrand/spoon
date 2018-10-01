package spoon.apimodel;

import org.junit.Ignore;
import org.junit.Test;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;

public class APIModelTest {

	@Ignore
	@Test
	public void testAPIPrettyPrinter() {
		Launcher userLauncher = new MavenLauncher("/home/nharrand/Documents/dependence/uselib", MavenLauncher.SOURCE_TYPE.APP_SOURCE);
		CtModel model = userLauncher.buildModel();
		//lib.packages
		//emptyModel.getRootPackage().addPackage()
		APIModel api = new APIModel(model,"se.kth.castor.types");
		api.printJava("/home/nharrand/Documents/dependence/output");
	}

}