package io.datatok.djobi.utils.templating;

import com.github.mustachejava.Code;
import com.github.mustachejava.ObjectHandler;
import com.github.mustachejava.TemplateContext;
import com.github.mustachejava.reflect.GuardedBinding;
import com.github.mustachejava.reflect.MissingWrapper;
import com.github.mustachejava.util.Wrapper;

import java.util.List;

public class MyGuardedBinding extends GuardedBinding {

    MyGuardedBinding(ObjectHandler oh, String name, TemplateContext tc, Code code) {
        super(oh, name, tc, code);
    }

    protected synchronized Wrapper getWrapper(String name, List<Object> scopes) {
        final Wrapper foundWrapper = super.getWrapper(name, scopes);

        if (foundWrapper instanceof MissingWrapper) {
            throw new IllegalStateException(String.format("'%s' is missing!", name));
        }

        return foundWrapper;
    }

}
