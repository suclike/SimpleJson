package com.github.wrdlbrnft.codebuilder.utils;


import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.impl.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 30/11/14
 */
public class Utils {

    /**
     * Tests wheter the given {@link javax.lang.model.element.Element} is a class.
     *
     * @param element The {@link javax.lang.model.element.Element} in question.
     * @return true if the {@link javax.lang.model.element.Element} is a class, otherwise false.
     */
    public static boolean isClass(Element element) {
        return element.getKind() == ElementKind.CLASS;
    }

    /**
     * Tests wheter the given {@link javax.lang.model.element.Element} is a method.
     *
     * @param element The {@link javax.lang.model.element.Element} in question.
     * @return true if the {@link javax.lang.model.element.Element} is a method, otherwise false.
     */
    public static boolean isMethod(Element element) {
        return element.getKind() == ElementKind.METHOD;
    }

    /**
     * Tests wheter the given {@link javax.lang.model.element.Element} is a interface.
     *
     * @param element The {@link javax.lang.model.element.Element} in question.
     * @return true if the {@link javax.lang.model.element.Element} is a interface, otherwise false.
     */
    public static boolean isInterface(Element element) {
        return element.getKind() == ElementKind.INTERFACE;
    }

    /**
     * Tests wheter the given {@link javax.lang.model.element.Element} is a field.
     *
     * @param element The {@link javax.lang.model.element.Element} in question.
     * @return true if the {@link javax.lang.model.element.Element} is a field, otherwise false.
     */
    public static boolean isField(Element element) {
        return element.getKind() == ElementKind.FIELD;
    }

    /**
     * Returns the fully qualified class name of the supplied {@link javax.lang.model.element.Element}.
     *
     * @param element The {@link javax.lang.model.element.Element} in question
     * @return The fully qualified class name as {@link String}
     */
    public static String getFullClassName(TypeElement element) {
        final String packageName = getPackageName(element);
        final String className = element.getSimpleName().toString();
        return String.format("%s.%s", packageName, className);
    }

    /**
     * Returns the package name of the supplied {@link javax.lang.model.element.Element}.
     *
     * @param element The {@link javax.lang.model.element.Element} in question
     * @return The package name of the {@link javax.lang.model.element.Element}
     */
    public static String getPackageName(Element element) {
        final Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement) {
            final PackageElement packageElement = (PackageElement) enclosingElement;
            return packageElement.getQualifiedName().toString();
        } else {
            return getPackageName(enclosingElement) + "." + enclosingElement.getSimpleName().toString();
        }
    }

    /**
     * Raises a compile error and indicates a supplied {@link javax.lang.model.element.Element} as source of the error.
     *
     * @param environment The current {@link javax.annotation.processing.ProcessingEnvironment} in which the error will be raised
     * @param message     The message to go along with the error
     * @param element     The reference {@link javax.lang.model.element.Element} for the error.
     */
    public static void error(ProcessingEnvironment environment, String message, Element element) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    /**
     * Raises a compile error
     *
     * @param environment The current {@link javax.annotation.processing.ProcessingEnvironment} in which the error will be raised
     * @param message     The message to go along with the error
     */
    public static void error(ProcessingEnvironment environment, String message) {
        environment.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    public static TypeElement getSuperElement(TypeElement element) {
        final TypeMirror superClassMirror = element.getSuperclass();
        if (superClassMirror == null || superClassMirror instanceof NoType) {
            return null;
        }

        final DeclaredType declaredType = (DeclaredType) superClassMirror;
        return (TypeElement) declaredType.asElement();
    }

    public static String getClassName(Class<?> cls, Class<?> genericType) {
        return String.format("%s<%s>", cls.getName(), genericType.getName());
    }

    public static String getClassName(Class<?> cls) {
        return cls.getName();
    }


    public static AnnotationValue getAnnotationValue(Element element, String annotationClass, String name) {

        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals(annotationClass)) {
                final Map<? extends ExecutableElement, ? extends AnnotationValue> map = mirror.getElementValues();
                for (ExecutableElement key : map.keySet()) {
                    if (key.getSimpleName().toString().equals(name)) {
                        return map.get(key);
                    }
                }
                return null;
            }
        }

        return null;
    }

    public static boolean hasAnnotation(Element element, String annotationClass) {
        final List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : mirrors) {
            if (mirror.getAnnotationType().toString().equals(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    public static List<Type> getTypeParameters(TypeMirror mirror) {
        final List<Type> parameters = new ArrayList<>();
        final DeclaredType declaredType = (DeclaredType) mirror;
        for (TypeMirror parameter : declaredType.getTypeArguments()) {
            parameters.add(Types.create(parameter));
        }
        return parameters;
    }

    public static List<Type> getTypeParameters(TypeMirror mirror, Type targetType) {
        final List<Type> parameters = new ArrayList<>();

        final DeclaredType declaredType = (DeclaredType) mirror;
        final TypeElement implementedType = (TypeElement) declaredType.asElement();
        final DeclaredType foundType = findType(implementedType, targetType);
        if (foundType != null) {
            for (TypeMirror a : foundType.getTypeArguments()) {
                parameters.add(Types.create(a));
            }
        }

        return parameters;
    }

    public static DeclaredType findType(TypeElement implementedType, Type targetType) {
        final Type currentType = Types.create(implementedType);
        if (currentType.equals(targetType)) {
            return (DeclaredType) implementedType.asType();
        }

        for (TypeMirror interfaceType : implementedType.getInterfaces()) {
            final DeclaredType declaredType = (DeclaredType) interfaceType;

            final Type type = Types.create(declaredType);
            if (targetType.equals(type)) {
                return declaredType;
            }
        }

        final TypeElement superElement = Utils.getSuperElement(implementedType);
        if (superElement != null) {
            return findType(superElement, targetType);
        }

        return null;
    }
}
