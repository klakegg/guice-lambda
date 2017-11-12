package net.klakegg.guice.lambda.processor;

import net.klakegg.guice.lambda.views.main;
import org.kohsuke.MetaInfServices;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author erlend
 */
@SupportedAnnotationTypes("net.klakegg.guice.lambda.annotation.Entry")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@MetaInfServices(Processor.class)
@SuppressWarnings("unused")
public class EntryProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

            Map<Name, List<Element>> classes = annotatedElements.stream()
                    .collect(Collectors.groupingBy(this::extractParent, HashMap::new, Collectors.toCollection(ArrayList::new)));

            classes.forEach(this::createClass);
        }

        return true;
    }

    private void createClass(Name name, List<Element> methods) {
        try {
            String pkgName = name.toString().substring(0, name.toString().lastIndexOf("."));
            String clsName = name.toString().substring(pkgName.length() + 1);

            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(name.toString() + "Entry");

            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                out.println(main
                        .template(pkgName, clsName, clsName + "Entry", methods.stream()
                                .collect(Collectors.toMap(Element::getSimpleName, e -> (ExecutableType) e.asType())))
                        .render()
                        .toString());
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private Name extractParent(Element e) {
        return ((TypeElement) e.getEnclosingElement()).getQualifiedName();
    }
}
