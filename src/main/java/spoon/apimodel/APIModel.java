package spoon.apimodel;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIModel {
	private CtModel usingModel;

	private CtModel perceivedModel;
	private Launcher apiLauncher;
	private Map<CtType, UsedType> usedTypes;

	public APIModel(CtModel srcModel, String apiPrefix) {
		this.usingModel = srcModel;
		this.usedTypes = new HashMap<>();

		//Get all reference to API
		List<CtReference> refs = usingModel.getRootPackage().filterChildren(
				new AbstractFilter<CtReference>(CtReference.class) {
					@Override
					public boolean matches(CtReference reference) {
						if (reference instanceof CtTypeReference) {
							return ((CtTypeReference) reference).getQualifiedName().startsWith(apiPrefix);
						}
						if (reference instanceof CtExecutableReference) {
							return ((CtExecutableReference) reference).getDeclaringType().getQualifiedName().startsWith(apiPrefix);
						}
						if (reference instanceof CtFieldReference) {
							return ((CtFieldReference) reference).getDeclaringType().getQualifiedName().startsWith(apiPrefix);
						}
						return false;
					}
				}
		).list();

		//Store/Classify references per CtType
		for (CtReference reference : refs) {
			UsedType u;
			CtType type;
			if (reference instanceof CtTypeReference) {
				type = ((CtTypeReference) reference).getTypeDeclaration();
				u = usedTypes.get(type);
				if (u == null) {
					u = new UsedType(type);
					usedTypes.put(type, u);
				}
				u.usages.add(reference);
			} else if (reference instanceof CtExecutableReference) {
				CtExecutableReference ref = ((CtExecutableReference) reference);
				CtTypeMember tm;
				if (ref.getExecutableDeclaration() instanceof CtMethod) {
					tm = (CtMethod) ref.getExecutableDeclaration();
				} else if (ref.getExecutableDeclaration() instanceof CtConstructor) {
					tm = (CtConstructor) ref.getExecutableDeclaration();
				} else {
					continue;
				}
				type = ref.getDeclaringType().getTypeDeclaration();
				u = usedTypes.get(type);
				if (u == null) {
					u = new UsedType(type);
					usedTypes.put(type, u);
				}
				List<CtReference> l = u.memberUsages.computeIfAbsent(tm, k -> new ArrayList<>());
				l.add(reference);

			} else if (reference instanceof CtFieldReference) {
				CtFieldReference ref = ((CtFieldReference) reference);
				type = ref.getDeclaringType().getTypeDeclaration();
				u = usedTypes.get(type);
				if (u == null) {
					u = new UsedType(type);
					usedTypes.put(type, u);
				}
				List<CtReference> l = u.memberUsages.computeIfAbsent(ref.getFieldDeclaration(), k -> new ArrayList<>());
				l.add(reference);
			}
		}

		//Create new Model
		apiLauncher = new Launcher();
		apiLauncher.getEnvironment().setSourceClasspath(usingModel.getRootPackage().getFactory().getEnvironment().getSourceClasspath());
		apiLauncher.getEnvironment().setAutoImports(true);
		perceivedModel = apiLauncher.getModel();
		Factory factory = apiLauncher.getFactory();

		//Get the list of packages containing used types
		Map<String, CtPackage> packages = new HashMap<>();
		for (CtType type : usedTypes.keySet()) {
			packages.put(type.getPackage().getQualifiedName(), null);
		}

		//Adding them to the API model
		for (String p: packages.keySet()) {
			packages.put(p, factory.Package().create(perceivedModel.getRootPackage(), p));
		}

		//Add used types to the API model, and clean unused type member.
		for (CtType type : usedTypes.keySet()) {
			CtPackage ctPackage = packages.get(type.getPackage().getQualifiedName()).addType(type.clone());
			CtType t = ctPackage.getType(type.getSimpleName());
			List<CtTypeMember> members = t.getTypeMembers();
			List<CtTypeMember> membersToRemove = new ArrayList<>();
			for (CtTypeMember member : members) {
				if (!usedTypes.get(t).memberUsages.containsKey(member)) {
					membersToRemove.add(member);
				}
			}
			for (CtTypeMember member : membersToRemove) {
				t.removeTypeMember(member);
			}
		}


		apiLauncher.getEnvironment().setPrettyPrinterCreator(
			() -> new APIPrettyPrinter(apiLauncher.getEnvironment())
		);
	}

	public CtModel getPerceivedModel() {
		return perceivedModel;
	}

	public Map<CtType, UsedType> getUsedTypes() {
		return usedTypes;
	}

	//API size: #Packages, #Types (#Interfaces, #AbstractClasses, #Classes), #ClassMember (#Methods, #Fields)

	/*public void printJSON(String outputDir) {
		TODO
	}*/

	public void printJava(String outputDir) {
		apiLauncher.setSourceOutputDirectory(outputDir);
		apiLauncher.prettyprint();
	}
}
