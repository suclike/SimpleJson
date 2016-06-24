package com.github.wrdlbrnft.simplejson.builder.retrofit;

import com.github.wrdlbrnft.codebuilder.annotations.Annotations;
import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.BlockWriter;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.code.SourceFile;
import com.github.wrdlbrnft.codebuilder.elements.ifs.If;
import com.github.wrdlbrnft.codebuilder.elements.values.Values;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Operators;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.ParserBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 21/06/16.
 */

public class RetrofitConverterBuilder {

    private static final String CONVERTER_FACTORY_NAME = "SimpleJsonConverterFactory";

    private static final Method METHOD_GET_RAW_TYPE = Methods.stub("getRawType");
    private static final Method METHOD_TYPE_TOKEN_OF = Methods.stub("of");
    private static final Method METHOD_GET_ACTUAL_TYPE_ARGUMENTS = Methods.stub("getActualTypeArguments");

    private final ProcessingEnvironment mProcessingEnvironment;

    private final ConverterBuilder mSingleItemResponseConverterBuilder;
    private final ConverterBuilder mSingleItemRequestConverterBuilder;
    private final ConverterBuilder mSingleItemBitmapResponseConverterBuilder;
    private final ConverterBuilder mMultiItemResponseConverterBuilder;
    private final ConverterBuilder mMultiItemRequestConverterBuilder;

    private final boolean mRetrofitFound;
    private final Type mRetrofitType;
    private final Type mConverterType;
    private final Type mConverterFactoryType;
    private final Type mMediaTypeType;
    private final Type mBitmapType;
    private final Type mBitmapFactoryType;
    private final Type mResponseBodyType;
    private final Type mRequestBodyType;
    private final Type mTypeType;
    private final Type mTypeTokenType;
    private final Type mAnnotationArrayType;
    private final Type mParameterizedTypeType;

    public RetrofitConverterBuilder(ProcessingEnvironment processingEnvironment) {
        mProcessingEnvironment = processingEnvironment;

        final TypeElement retrofitElement = getTypeElement("retrofit2.Retrofit");
        final TypeElement converterElement = getTypeElement("retrofit2.Converter");
        final TypeElement converterFactoryElement = getTypeElement("retrofit2.Converter.Factory");
        final TypeElement mediaTypeElement = getTypeElement("okhttp3.MediaType");
        final TypeElement bitmapElement = getTypeElement("android.graphics.Bitmap");
        final TypeElement bitmapFactoryElement = getTypeElement("android.graphics.BitmapFactory");
        final TypeElement responseBodyElement = getTypeElement("okhttp3.ResponseBody");
        final TypeElement requestBodyElement = getTypeElement("okhttp3.RequestBody");
        final TypeElement typeTokenElement = getTypeElement("com.google.common.reflect.TypeToken");

        mRetrofitFound = !anyNull(retrofitElement, converterElement, converterFactoryElement, mediaTypeElement, bitmapElement, bitmapFactoryElement, responseBodyElement, requestBodyElement, typeTokenElement);

        mRetrofitType = mRetrofitFound ? Types.of(retrofitElement) : null;
        mConverterType = mRetrofitFound ? Types.of(converterElement) : null;
        mConverterFactoryType = mRetrofitFound ? Types.of(converterFactoryElement) : null;
        mMediaTypeType = mRetrofitFound ? Types.of(mediaTypeElement) : null;
        mBitmapType = mRetrofitFound ? Types.of(bitmapElement) : null;
        mBitmapFactoryType = mRetrofitFound ? Types.of(bitmapFactoryElement) : null;
        mResponseBodyType = mRetrofitFound ? Types.of(responseBodyElement) : null;
        mRequestBodyType = mRetrofitFound ? Types.of(requestBodyElement) : null;
        mTypeType = mRetrofitFound ? Types.of(java.lang.reflect.Type.class) : null;
        mTypeTokenType = mRetrofitFound ? Types.of(typeTokenElement) : null;
        mAnnotationArrayType = mRetrofitFound ? Types.arrayOf(Types.of(Annotation.class)) : null;
        mParameterizedTypeType = mRetrofitFound ? Types.of(ParameterizedType.class) : null;

        mSingleItemResponseConverterBuilder = new SingleItemResponseConverterBuilder(processingEnvironment, mConverterType, mResponseBodyType);
        mSingleItemRequestConverterBuilder = new SingleItemRequestConverterBuilder(processingEnvironment, mConverterType, mMediaTypeType, mRequestBodyType);
        mSingleItemBitmapResponseConverterBuilder = new SingleBitmapResponseConverterBuilder(processingEnvironment, mConverterType, mBitmapType, mBitmapFactoryType, mResponseBodyType);
        mMultiItemResponseConverterBuilder = new MultiItemResponseConverterBuilder(processingEnvironment, mConverterType, mResponseBodyType);
        mMultiItemRequestConverterBuilder = new MultiItemRequestConverterBuilder(processingEnvironment, mConverterType, mMediaTypeType, mRequestBodyType);
    }

    public void build(ParserBuilder.ParserCollection collection) throws IOException {
        if (!mRetrofitFound) {
            return;
        }

        final Map<TypeElement, Type> entityToParserMap = collection.getEntityToParserMap();
        if (entityToParserMap.isEmpty()) {
            return;
        }

        final String packageName = determinePackageName(entityToParserMap);
        if (packageName == null) {
            return;
        }

        final Type bitmapResponseBodyConverterType = mSingleItemBitmapResponseConverterBuilder.build(packageName);
        final Type requestBodyConverterType = mSingleItemRequestConverterBuilder.build(packageName);
        final Type responseBodyConverterType = mSingleItemResponseConverterBuilder.build(packageName);
        final Type listRequestBodyConverterType = mMultiItemRequestConverterBuilder.build(packageName);
        final Type listResponseBodyConverterType = mMultiItemResponseConverterBuilder.build(packageName);

        final Method getParserMethod = new Method.Builder()
                .setModifiers(EnumSet.of(Modifier.PRIVATE, Modifier.STATIC))
                .setReturnType(Types.generic(SimpleJsonTypes.PARSER, Types.wildCard()))
                .setCode(new ExecutableBuilder() {

                    private Variable paramClass;

                    @Override
                    protected List<Variable> createParameters() {
                        final List<Variable> parameters = new ArrayList<>();
                        parameters.add(paramClass = Variables.of(Types.generic(Types.CLASS, Types.wildCard())));
                        return parameters;
                    }

                    @Override
                    protected void write(Block block) {
                        final If.Builder builder = new If.Builder().setElse(new BlockWriter() {
                            @Override
                            protected void write(Block block) {
                                block.append("throw ").append(Types.Exceptions.ILLEGAL_STATE_EXCEPTION.newInstance(
                                        Operators.operate(
                                                Values.of("Cannot find parser for "),
                                                "+",
                                                paramClass
                                        )));
                                block.append(";");
                            }
                        });

                        for (TypeElement entityElement : entityToParserMap.keySet()) {
                            final Type parser = entityToParserMap.get(entityElement);
                            builder.add(compare(entityElement), new BlockWriter() {
                                @Override
                                protected void write(Block block) {
                                    block.append("return ").append(parser.newInstance()).append(";");
                                }
                            });
                        }

                        block.append(builder.build());
                    }

                    private CodeElement compare(TypeElement typeElement) {
                        return Methods.EQUALS.callOnTarget(Types.of(typeElement).classObject(), paramClass);
                    }
                })
                .build();

        final Implementation converterFactoryImplementation = new Implementation.Builder()
                .setName(CONVERTER_FACTORY_NAME)
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL))
                .setExtendedType(mConverterFactoryType)
                .addMethod(getParserMethod)
                .addMethod(new Method.Builder()
                        .setName("responseBodyConverter")
                        .setModifiers(EnumSet.of(Modifier.PUBLIC))
                        .addAnnotation(Annotations.forType(Override.class))
                        .setReturnType(Types.generic(mConverterType, mResponseBodyType, Types.wildCard()))
                        .setCode(new ExecutableBuilder() {

                            private Variable paramType;
                            private Variable paramAnnotationArray;
                            private Variable paramRetrofit;

                            @Override
                            protected List<Variable> createParameters() {
                                final List<Variable> parameters = new ArrayList<>();
                                parameters.add(paramType = Variables.of(mTypeType));
                                parameters.add(paramAnnotationArray = Variables.of(mAnnotationArrayType));
                                parameters.add(paramRetrofit = Variables.of(mRetrofitType));
                                return parameters;
                            }

                            @Override
                            protected void write(Block block) {
                                final Variable varTypeToken = Variables.of(mTypeTokenType, Modifier.FINAL);
                                block.set(varTypeToken, METHOD_TYPE_TOKEN_OF.callOnTarget(mTypeTokenType, paramType)).append(";").newLine();

                                final Variable varClass = Variables.of(Types.generic(Types.CLASS, Types.wildCard()), Modifier.FINAL);
                                block.set(varClass, METHOD_GET_RAW_TYPE.callOnTarget(varTypeToken)).append(";").newLine();

                                block.append(new If.Builder()
                                        .add(Methods.EQUALS.callOnTarget(mBitmapType.classObject(), varClass), new BlockWriter() {
                                            @Override
                                            protected void write(Block block) {
                                                block.append("return ").append(bitmapResponseBodyConverterType.newInstance()).append(";");
                                            }
                                        })
                                        .add(Methods.EQUALS.callOnTarget(Types.LIST.classObject(), varClass), new BlockWriter() {
                                            @Override
                                            protected void write(Block block) {
                                                final Variable varParameterizeType = Variables.of(mParameterizedTypeType, Modifier.FINAL);
                                                block.set(varParameterizeType,
                                                        new Block().append(Types.asCast(mParameterizedTypeType)).append(paramType))
                                                        .append(";").newLine();

                                                final Variable varItemType = Variables.of(Types.generic(Types.CLASS, Types.wildCard()), Modifier.FINAL);
                                                block.set(varItemType,
                                                        METHOD_GET_RAW_TYPE.callOnTarget(
                                                                METHOD_TYPE_TOKEN_OF.callOnTarget(mTypeTokenType,
                                                                        new Block().append(METHOD_GET_ACTUAL_TYPE_ARGUMENTS.callOnTarget(varParameterizeType)).append("[0]")
                                                                )
                                                        )
                                                ).append(";").newLine();

                                                final Variable varParser = Variables.of(Types.generic(SimpleJsonTypes.PARSER, Types.wildCard()), Modifier.FINAL);
                                                block.set(varParser, getParserMethod.call(varItemType)).append(";").newLine();
                                                block.append("return ").append(listResponseBodyConverterType.newInstance(varParser)).append(";");
                                            }
                                        })
                                        .build())
                                        .newLine();


                                final Variable varParser = Variables.of(Types.generic(SimpleJsonTypes.PARSER, Types.wildCard()), Modifier.FINAL);
                                block.set(varParser, getParserMethod.call(varClass)).append(";").newLine();
                                block.append("return ").append(responseBodyConverterType.newInstance(varParser)).append(";");
                            }
                        })
                        .build())
                .addMethod(new Method.Builder()
                        .setName("requestBodyConverter")
                        .setModifiers(EnumSet.of(Modifier.PUBLIC))
                        .addAnnotation(Annotations.forType(Override.class))
                        .setReturnType(Types.generic(mConverterType, Types.wildCard(), mRequestBodyType))
                        .setCode(new ExecutableBuilder() {

                            private Variable paramType;
                            private Variable paramParameterAnnotationArray;
                            private Variable paramMethodAnnotationArray;
                            private Variable paramRetrofit;

                            @Override
                            protected List<Variable> createParameters() {
                                final List<Variable> parameters = new ArrayList<>();
                                parameters.add(paramType = Variables.of(mTypeType));
                                parameters.add(paramParameterAnnotationArray = Variables.of(mAnnotationArrayType));
                                parameters.add(paramMethodAnnotationArray = Variables.of(mAnnotationArrayType));
                                parameters.add(paramRetrofit = Variables.of(mRetrofitType));
                                return parameters;
                            }

                            @Override
                            protected void write(Block block) {
                                final Variable varTypeToken = Variables.of(mTypeTokenType, Modifier.FINAL);
                                block.set(varTypeToken, METHOD_TYPE_TOKEN_OF.callOnTarget(mTypeTokenType, paramType)).append(";").newLine();

                                final Variable varClass = Variables.of(Types.generic(Types.CLASS, Types.wildCard()), Modifier.FINAL);
                                block.set(varClass, METHOD_GET_RAW_TYPE.callOnTarget(varTypeToken)).append(";").newLine();

                                block.append(new If.Builder()
                                        .add(Methods.EQUALS.callOnTarget(Types.LIST.classObject(), varClass), new BlockWriter() {
                                            @Override
                                            protected void write(Block block) {
                                                final Variable varParameterizeType = Variables.of(mParameterizedTypeType, Modifier.FINAL);
                                                block.set(varParameterizeType,
                                                        new Block().append(Types.asCast(mParameterizedTypeType)).append(paramType))
                                                        .append(";").newLine();

                                                final Variable varItemType = Variables.of(Types.generic(Types.CLASS, Types.wildCard()), Modifier.FINAL);
                                                block.set(varItemType,
                                                        METHOD_GET_RAW_TYPE.callOnTarget(
                                                                METHOD_TYPE_TOKEN_OF.callOnTarget(mTypeTokenType,
                                                                        new Block().append(METHOD_GET_ACTUAL_TYPE_ARGUMENTS.callOnTarget(varParameterizeType)).append("[0]")
                                                                )
                                                        )
                                                ).append(";").newLine();

                                                final Variable varParser = Variables.of(Types.generic(SimpleJsonTypes.PARSER, Types.wildCard()), Modifier.FINAL);
                                                block.set(varParser, getParserMethod.call(varItemType)).append(";").newLine();
                                                block.append("return ").append(listRequestBodyConverterType.newInstance(varParser)).append(";");
                                            }
                                        })
                                        .build())
                                        .newLine();

                                final Variable varParser = Variables.of(Types.generic(SimpleJsonTypes.PARSER, Types.wildCard()), Modifier.FINAL);
                                block.set(varParser, getParserMethod.call(varClass)).append(";").newLine();
                                block.append("return ").append(requestBodyConverterType.newInstance(varParser)).append(";");
                            }
                        })
                        .build())
                .build();
        final SourceFile sourceFile = SourceFile.create(mProcessingEnvironment, packageName);
        sourceFile.write(converterFactoryImplementation);
        sourceFile.flushAndClose();
    }

    private String determinePackageName(Map<TypeElement, Type> entityToParserMap) {
        String chosenPackageName = null;
        for (TypeElement element : entityToParserMap.keySet()) {
            final String elementPackageName = Utils.getPackageName(element);
            if (chosenPackageName == null) {
                chosenPackageName = elementPackageName;
                continue;
            }

            if (chosenPackageName.contains(elementPackageName)) {
                chosenPackageName = elementPackageName;
            }
        }
        return chosenPackageName;
    }

    private static boolean anyNull(Object... items) {
        for (Object item : items) {
            if (item == null) {
                return true;
            }
        }
        return false;
    }

    private TypeElement getTypeElement(String charSequence) {
        return mProcessingEnvironment.getElementUtils().getTypeElement(charSequence);
    }
}