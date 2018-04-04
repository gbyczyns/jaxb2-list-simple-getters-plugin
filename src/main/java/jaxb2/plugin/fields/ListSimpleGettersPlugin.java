package jaxb2.plugin.fields;

import java.util.Arrays;
import java.util.List;

import org.jvnet.jaxb2_commons.plugin.AbstractParameterizablePlugin;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class ListSimpleGettersPlugin extends AbstractParameterizablePlugin {

	private static final String OPTION_NAME = "Xlist-simple-getters";

	@Override
	public String getOptionName() {
		return OPTION_NAME;
	}

	@Override
	public String getUsage() {
		return "-" + OPTION_NAME + ": Generate simple getters for List<?> fields";
	}

	/**
	 * Plugin entry-point
	 */
	@Override
	public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) throws SAXException {

		for (ClassOutline classOutline : outline.getClasses()) {
			JCodeModel codeModel = classOutline.implClass.owner();
			JClass refList = codeModel.ref(List.class);

			Arrays.stream(classOutline.getDeclaredFields()) //
				.filter(fieldOutline -> fieldOutline.getRawType().erasure() instanceof JClass) //
				.filter(fieldOutline -> refList.isAssignableFrom((JClass) fieldOutline.getRawType().erasure())) //
				.map(fieldOutline -> fieldOutline.getPropertyInfo().getName(false)) //
				.map(fieldName -> classOutline.implClass.fields().get(fieldName)) //
				.forEach(jFieldVar -> createSimpleGetter(jFieldVar, classOutline));
		}
		return true;
	}

	private void createSimpleGetter(JFieldVar jFieldVar, ClassOutline classOutline) {
		String getterName = generateGetterName(jFieldVar);
		classOutline.implClass.methods().removeIf(jMethod -> getterName.equals(jMethod.name()) && jMethod.params().isEmpty());

		JMethod jMethod = classOutline.implClass.method(JMod.PUBLIC, jFieldVar.type(), getterName);
		jMethod.body()._return(jFieldVar);
	}

	private String generateGetterName(JFieldVar jFieldVar) {
		String name = jFieldVar.name();
		return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
	}
}
