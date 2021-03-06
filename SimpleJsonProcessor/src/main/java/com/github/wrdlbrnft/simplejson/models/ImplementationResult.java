package com.github.wrdlbrnft.simplejson.models;

import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Type;

import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 21/04/15.
 */
public class ImplementationResult {

    private final TypeElement mInterfaceType;
    private final Implementation mImplType;
    private final List<MappedValue> mMappedValues;

    public ImplementationResult(Implementation implType, TypeElement interfaceType, List<MappedValue> mappedValues) {
        mInterfaceType = interfaceType;
        mImplType = implType;
        mMappedValues = mappedValues;
    }

    public Implementation getImplType() {
        return mImplType;
    }

    public List<MappedValue> getMappedValues() {
        return mMappedValues;
    }

    public TypeElement getInterfaceType() {
        return mInterfaceType;
    }
}
