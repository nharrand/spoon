package spoon.apimodel;

import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.reference.CtReference;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class UsedType {
	public CtType type;
	public List<CtReference> usages;
	public Map<CtTypeMember, List<CtReference>> memberUsages;

	public UsedType(CtType type) {
		this.type = type;
		usages = new ArrayList<>();
		memberUsages = new HashMap<>();
	}
}
